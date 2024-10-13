package scheduler.schedulers.parallel;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.sequential.AStarScheduler;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Parallel scheduler using static load distribution.
 */
public class ParallelSchedulerStatic extends AStarScheduler {
    private final ExecutorService threadPool;

    private final PriorityQueue<StateModel> initialStates;

    private final Set<StateModel> closedStates;

    private final Worker[] workers;

    private final Object bestStateLock;

    private final byte cores;

    /**
     * Constructor for ParallelSchedulerStatic class
     *
     * @param graph the input graph
     * @param processors the number of processors
     * @param cores the number of cores/threads
     */
    public ParallelSchedulerStatic(GraphModel graph, byte processors, byte cores) {
        super(graph, processors);

        this.threadPool = Executors.newFixedThreadPool(cores);

        this.initialStates = new PriorityQueue<>(Comparator.comparingInt(this::getFCost));

        this.closedStates = ConcurrentHashMap.newKeySet();

        this.workers = new Worker[cores];

        this.bestStateLock = new Object();

        this.cores = cores;
    }

    /**
     * Runs the A star with heuristic, i.e., we set an upper limit for the number of states in the queue.
     */
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
                    expandState(this.initialStates, state, node, processor);
                }
            }
        }
    }

    /**
     * Assigns initial workload to workers
     */
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

    /**
     * Check if we can prune the current state.
     *
     * @param state the current state
     * @return if we can prune the current state
     */
    @Override
    public boolean canPruneState(StateModel state) {
        if (!this.closedStates.add(state)) {
            return true;
        }

        return state.getMaximumFinishTime() >= this.bestState.getMaximumFinishTime();
    }

    /**
     * Start the parallel scheduling process.
     */
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

    /**
     * Worker class
     */
    private class Worker implements Callable<Void> {
        private final PriorityQueue<StateModel> openedStates = new PriorityQueue<>(Comparator.comparingInt(ParallelSchedulerStatic.this::getFCost));

        /**
         * Start worker thread.
         *
         * @return nothing
         */
        @Override
        public Void call() {
            processPendingStates();

            return null;
        }

        /**
         * Process the pending states for the current worker.
         */
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

        /**
         * Update the best state.
         *
         * @param state the current state to be set as the best state
         */
        private void updateBestState(StateModel state) {
            synchronized (bestStateLock) {
                if (state.getMaximumFinishTime() < bestState.getMaximumFinishTime()) {
                    bestState = state;
                }
            }
        }

        /**
         * Expand the number of states
         *
         * @param state the
         */
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

        /**
         * Expand current state.
         *
         * @param state the current state
         * @param node the current node
         * @param processor the processor
         */
        private void expandState(StateModel state, NodeModel node, byte processor) {
            StateModel nextState = state.clone();

            int earliestStartTime = getEarliestStartTime(state, node, processor);

            nextState.addNode(node, processor, earliestStartTime);
            nextState.setParentMaximumBottomLevelPathLength(state.getMaximumBottomLevelPathLength());

            if (canPruneState(nextState)) {
                return;
            }

            this.openedStates.add(nextState);

            metrics.incrementNumberOfOpenedStates();
        }

        /**
         * Checks if a state can be pruned.
         *
         * @param state the current state
         * @return if a state can be pruned
         */
        private boolean canPruneState(StateModel state) {
            if (!closedStates.add(state)) {
                return true;
            }

            return state.getMaximumFinishTime() >= getBestStateFinishTime();
        }

        /**
         * Get the best state finish time
         *
         * @return the best state finish time
         */
        private int getBestStateFinishTime() {
            synchronized (bestStateLock) {
                return bestState.getMaximumFinishTime();
            }
        }
    }
}