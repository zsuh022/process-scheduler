package scheduler.schedulers.parallel;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.sequential.AStarScheduler;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelSchedulerDynamic extends AStarScheduler {
    private final ExecutorService threadPool;

    private final PriorityQueue<StateModel> initialStates;

    private final Set<StateModel> closedStates;

    private final Worker[] workers;

    private final Object bestStateLock;

    private final AtomicInteger workerId;

    private final byte cores;

    public ParallelSchedulerDynamic(GraphModel graph, byte processors, byte cores) {
        super(graph, processors);

        this.threadPool = Executors.newFixedThreadPool(cores);

        this.initialStates = new PriorityQueue<>(Comparator.comparingInt(this::getFCost));

        this.closedStates = ConcurrentHashMap.newKeySet();

        this.workers = new Worker[cores];

        this.bestStateLock = new Object();

        this.workerId = new AtomicInteger(0);

        this.cores = cores;
    }

    private void runAStarScheduleWithHeuristic() {
        this.initialStates.add(new StateModel(this.processors, this.numberOfNodes));

        while (!this.initialStates.isEmpty() && this.initialStates.size() < this.numberOfNodes * this.cores) {
            StateModel state = this.initialStates.poll();

            if (state.areAllNodesScheduled()) {
                this.bestState = state;

                break;
            }

            for (NodeModel node : getAvailableNodes(state)) {
                for (byte processor = 0; processor < processors; processor++) {
                    expandState(state, node, processor);
                }
            }
        }
    }

    private void expandState(StateModel state, NodeModel node, byte processor) {
        if (isFirstAvailableNode(state, node)) {
            return;
        }

        StateModel nextState = state.clone();

        int earliestStartTime = getEarliestStartTime(state, node, processor);

        nextState.addNode(node, processor, earliestStartTime);
        nextState.setParentMaximumBottomLevelPathLength(state.getMaximumBottomLevelPathLength());

        if (!canPruneState(nextState)) {
            this.initialStates.add(nextState);

            metrics.incrementNumberOfOpenedStates();
        }
    }

    public void assignWorkToWorkers() {
        for (byte workerId = 0; workerId < this.cores; workerId++) {
            this.workers[workerId] = new Worker(workerId);
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

    private void setWorkerId(int workerId) {
        this.workerId.set(workerId);
    }

    private int getWorkerId() {
        return this.workerId.get();
    }

    private void incrementWorkerId() {
        int workerId = this.workerId.incrementAndGet();

        if (workerId == this.cores) {
            this.workerId.set(0);
        }
    }

    private class Worker implements Callable<Void> {
        private final PriorityBlockingQueue<StateModel> openedStates;

        private final int workerId;

        public Worker(byte workerId) {
            this.openedStates = new PriorityBlockingQueue<>(cores, Comparator.comparingInt(ParallelSchedulerDynamic.this::getFCost));

            this.workerId = workerId;
        }

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

                setWorkerId(this.workerId);

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
                if (isFirstAvailableNode(state, node)) {
                    continue;
                }

                for (byte processor = 0; processor < processors; processor++) {
                    expandState(state, node, processor);
                }
            }
        }

        private void expandState(StateModel state, NodeModel node, byte processor) {
            StateModel nextState = state.clone();

            int earliestStartTime = getEarliestStartTime(state, node, processor);

            nextState.addNode(node, processor, earliestStartTime);
            nextState.setParentMaximumBottomLevelPathLength(state.getMaximumBottomLevelPathLength());

            if (canPruneState(nextState)) {
                return;
            }

            distributeWork(nextState);

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

        private void distributeWork(StateModel state) {
            workers[getWorkerId()].openedStates.add(state);
            incrementWorkerId();
        }
    }
}