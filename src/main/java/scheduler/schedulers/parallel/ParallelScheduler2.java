package scheduler.schedulers.parallel;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.sequential.AStarScheduler;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static scheduler.constants.Constants.INFINITY_32;

public class ParallelScheduler2 extends AStarScheduler {
    private final ExecutorService threadPool;

    private final PriorityQueue<StateModel> initialStates;

    private final Set<StateModel> closedStates;

    private final AtomicBoolean isBestStateFound;

    private final Worker[] workers;

    private final Object bestStateLock;

    private final byte cores;

    public ParallelScheduler2(GraphModel graph, byte processors, byte cores) {
        super(graph, processors);

        this.threadPool = Executors.newFixedThreadPool(cores);

        this.initialStates = new PriorityQueue<>(Comparator.comparingInt(this::getFCost));

        this.closedStates = ConcurrentHashMap.newKeySet();

        this.isBestStateFound = new AtomicBoolean(false);

        this.workers = new Worker[cores];

        this.bestStateLock = new Object();

        this.cores = cores;
    }

    private void createWorkers() {
        for (byte workerId = 0; workerId < this.cores; workerId++) {
            this.workers[workerId] = new Worker();
        }
    }

    private void runAStarScheduleWithHeuristic() {
        this.initialStates.add(new StateModel(this.processors, this.numberOfNodes));

        while (!this.initialStates.isEmpty() && this.initialStates.size() < this.cores) {
            StateModel state = this.initialStates.poll();

            if (state.areAllNodesScheduled()) {
                this.bestState = state;

                break;
            }

            for (NodeModel node : getAvailableNodes(state)) {
                for (int processor = 0; processor < processors; processor++) {
                    expandState(state, node, processor);
                }
            }
        }
    }

    private void expandState(StateModel state, NodeModel node, int processor) {
        // Skip tasks that are not in the fixed order defined
        if (!isFirstAvailableNode(state, node)) {
            return;
        }

        StateModel nextState = state.clone();

        int earliestStartTime = getEarliestStartTime(state, node, processor);

        nextState.addNode(node, processor, earliestStartTime);

        if (!canPruneState(nextState)) {
            this.initialStates.add(nextState);

            metrics.incrementNumberOfOpenedStates();
        }
    }

    public void assignWorkToWorkers() {
        for (byte workerId = 0; workerId < this.cores; workerId++) {
            this.workers[workerId] = new Worker();
        }

        int workerId = 0;

        while (!this.initialStates.isEmpty()) {
            this.workers[workerId].openedStates.add(this.initialStates.poll());
            workerId = (workerId + 1) % this.cores;
        }
    }

    @Override
    public boolean canPruneState(StateModel state) {
        if (!this.closedStates.add(state)) {
            return true;
        }

        return state.getMaximumFinishTime() >= this.bestState.getMaximumFinishTime();
    }

    @Override
    public void schedule() {
        createWorkers();

        runAStarScheduleWithHeuristic();

        assignWorkToWorkers();

        try {
            this.threadPool.invokeAll(List.of(workers));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            this.threadPool.shutdown();
        }

        metrics.setBestState(this.bestState);
        metrics.setNumberOfClosedStates(this.closedStates.size());
    }

    private class Worker implements Callable<Void> {
        private final PriorityQueue<StateModel> openedStates = new PriorityQueue<>(Comparator.comparingInt(this::getFCost));

        @Override
        public Void call() {
            processPendingStates();

            return null;
        }

        private void processPendingStates() {
            while (!this.openedStates.isEmpty()) {
                StateModel state = this.openedStates.poll();

                if (state.areAllNodesScheduled()) {
                    updateBestState(state);

                    break;
                }

                expandStates(state);
            }
        }

        private void updateBestState(StateModel state) {
            synchronized (bestStateLock) {
                if (state.getMaximumFinishTime() < bestState.getMaximumFinishTime()) {
                    bestState = state;
                }
            }
        }

        private void expandStates(StateModel state) {
            for (NodeModel node : getAvailableNodes(state)) {
                if (!isFirstAvailableNode(state, node)) {
                    continue;
                }

                for (int processor = 0; processor < processors; processor++) {
                    expandState(state, node, processor);
                }
            }
        }

        private void expandState(StateModel state, NodeModel node, int processor) {
            StateModel nextState = state.clone();

            int earliestStartTime = getEarliestStartTime(state, node, processor);

            nextState.addNode(node, processor, earliestStartTime);

            if (canPruneState(nextState)) {
                return;
            }

            this.openedStates.add(nextState);

            metrics.incrementNumberOfOpenedStates();
        }

        private boolean canPruneState(StateModel state) {
            if (!closedStates.add(state)) {
                return true;
            }

            return state.getMaximumFinishTime() >= getBestStateFinishTime();
        }

        private int getBestStateFinishTime() {
            synchronized (bestStateLock) {
                return bestState.getMaximumFinishTime();
            }
        }

        private int getFCost(StateModel state) {
            if (state.isEmptyState()) {
                return getLowerBound();
            }

            int idleTime = getIdleTime(state);
            int maximumDataReadyTime = getMaximumDataReadyTime(state);
            int maximumBottomLevelPathLength = getMaximumBottomLevelPathLength(state);

            int fCost = Math.max(idleTime, Math.max(maximumBottomLevelPathLength, maximumDataReadyTime));

            state.setFCost(fCost);

            return fCost;
        }

        private int getLowerBound() {
            double loadBalancedTime = (double) graph.getTotalNodeWeight() / processors;

            return (int) Math.max(Math.ceil(loadBalancedTime), getCriticalPathLength());
        }

        private int getIdleTime(StateModel state) {
            double totalWeight = (double) graph.getTotalNodeWeight() + state.getTotalIdleTime();

            return (int) Math.ceil(totalWeight / processors);
        }

        // V2- can be reduced to O(1) but with increased memory? I'll see if it is worth it
        // note: fbl(s)=max(fbl(s_parent),ts(last)+bl(last))
        private int getMaximumBottomLevelPathLength(StateModel state) {
            int maximumBottomLevelPathLength = 0;

            for (NodeModel node : getScheduledNodes(state)) {
                int cost = state.getNodeStartTime(node) + bottomLevelPathLengths[node.getByteId()];
                maximumBottomLevelPathLength = Math.max(maximumBottomLevelPathLength, cost);
            }

            return maximumBottomLevelPathLength;
        }

        // This is actually amortised O(1) from O(|free(s)| * |P|)
        private int getMaximumDataReadyTime(StateModel state) {
            int maximumDataReadyTime = 0;

            for (NodeModel node : getAvailableNodes(state)) {
                int cost = getMinimumDataReadyTime(state, node) + bottomLevelPathLengths[node.getByteId()];
                maximumDataReadyTime = Math.max(maximumDataReadyTime, cost);
            }

            return maximumDataReadyTime;
        }

        private int getMinimumDataReadyTime(StateModel state, NodeModel node) {
            int minimumDataReadyTime = INFINITY_32;

            for (int processor = 0; processor < processors; processor++) {
                int dataReadyTime = getEarliestStartTime(state, node, processor);
                minimumDataReadyTime = Math.min(minimumDataReadyTime, dataReadyTime);
            }

            return minimumDataReadyTime;
        }
    }
}