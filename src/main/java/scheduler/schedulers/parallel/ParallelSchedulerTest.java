package scheduler.schedulers.parallel;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.sequential.AStarScheduler;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static scheduler.constants.Constants.INFINITY_32;

public class ParallelSchedulerTest extends AStarScheduler {
    private final ExecutorService threadPool;

    private volatile StateModel bestState;

    private final Set<StateModel> closedStates;

    private final AtomicBoolean isBestStateFound;

    private final byte cores;

    public ParallelSchedulerTest(GraphModel graph, byte processors, byte cores) {
        super(graph, processors);

        this.threadPool = Executors.newFixedThreadPool(cores);

        this.isBestStateFound = new AtomicBoolean(false);

        this.cores = cores;

        this.closedStates = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void schedule() {
        StateModel initialState = new StateModel(processors, numberOfNodes);

        List<Worker> workers = new ArrayList<>();

        for (int i = 0; i < cores; i++) {
            Worker worker = new Worker();
            worker.openedStates.add(initialState);
            workers.add(worker);
        }

        try {
            this.threadPool.invokeAll(workers);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            this.threadPool.shutdown();
        }

        if (this.bestState != null) {
            metrics.setBestState(this.bestState);
        } else {
            metrics.setBestState(getValidSchedule());
        }

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
            while (true) {
                if (isBestStateFound.get()) {
                    break;
                }

                StateModel state = this.openedStates.poll();
                if (state == null) {
                    break;
                }

                processState(state);
            }
        }

        private void processState(StateModel state) {
            if (state.areAllNodesScheduled()) {
                updateBestState(state);
                return;
            }

            closedStates.add(state);

//            metrics.incrementNumberOfClosedStates();

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

            if (closedStates.contains(nextState)) {
                return;
            }

            this.openedStates.add(nextState);

            metrics.incrementNumberOfOpenedStates();
        }

        private int getFCost(StateModel state) {
            if (state.isEmptyState()) {
                return getLowerBound();
            }

            int idleTime = getIdleTime(state);
            int maximumDataReadyTime = getMaximumDataReadyTime(state);
            int maximumBottomLevelPathLength = getMaximumBottomLevelPathLength(state);

            return Math.max(idleTime, Math.max(maximumBottomLevelPathLength, maximumDataReadyTime));
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

    private synchronized void updateBestState(StateModel state) {
        if (this.bestState == null || state.getMaximumFinishTime() < this.bestState.getMaximumFinishTime()) {
            this.bestState = state;
            this.isBestStateFound.set(true);
        }
    }
}