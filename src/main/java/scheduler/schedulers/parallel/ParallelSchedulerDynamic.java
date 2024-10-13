package scheduler.schedulers.parallel;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.sequential.AStarScheduler;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parallel scheduler using a naive dynamic load distribution technique.
 */
public class ParallelSchedulerDynamic extends AStarScheduler {
    private final ExecutorService threadPool;

    private final PriorityQueue<StateModel> initialStates;

    private final Worker[] workers;

    private final Object bestStateLock;

    private final AtomicInteger workerId;

    private final byte cores;

    /**
     * Constructor for the ParallelSchedulerDynamic class
     *
     * @param graph the input graph
     * @param processors the number of processors
     * @param cores the number of cores
     */
    public ParallelSchedulerDynamic(GraphModel graph, byte processors, byte cores) {
        super(graph, processors);

        this.threadPool = Executors.newFixedThreadPool(cores);

        this.initialStates = new PriorityQueue<>(Comparator.comparingInt(this::getFCost));

        closedStates = ConcurrentHashMap.newKeySet();

        this.workers = new Worker[cores];

        this.bestStateLock = new Object();

        this.workerId = new AtomicInteger(0);

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
                bestState = state;

                break;
            }

            expandStates(this.initialStates, state);
        }
    }

    /**
     * Assign work for the workers.
     */
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

    /**
     * Queue an initial workload for the workers.
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
     * Sets the current worker's id
     *
     * @param workerId the worker id
     */
    private void setWorkerId(int workerId) {
        this.workerId.set(workerId);
    }

    /**
     * Gets the current worker's id
     *
     * @return the current worker's id
     */
    private int getWorkerId() {
        return this.workerId.get();
    }

    /**
     * Increments the current worker's id.
     */
    private void incrementWorkerId() {
        int workerId = this.workerId.incrementAndGet();

        if (workerId == this.cores) {
            this.workerId.set(0);
        }
    }

    /**
     * Worker class which contains the necessary logic for scheduling in parallel with other worker threads.
     */
    private class Worker implements Callable<Void> {
        private final PriorityBlockingQueue<StateModel> openedStates;

        private final int workerId;

        public Worker(byte workerId) {
            this.openedStates = new PriorityBlockingQueue<>(cores, Comparator.comparingInt(ParallelSchedulerDynamic.this::getFCost));

            this.workerId = workerId;
        }

        /**
         * Start the worker thread.
         *
         * @return nothing
         */
        @Override
        public Void call() {
            processPendingStates();

            return null;
        }

        /**
         * Process the pending states.
         */
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

        /**
         * Update the best state
         *
         * @param state the current state
         */
        private void updateBestState(StateModel state) {
            synchronized (bestStateLock) {
                if (state.getMaximumFinishTime() < bestState.getMaximumFinishTime()) {
                    bestState = state;
                }
            }
        }

        /**
         * Expand states
         *
         * @param state the current state
         */
        private void expandStates(StateModel state) {
            List<NodeModel> availableNodes = getAvailableNodes(state);

            NodeModel fixedNode = getFixedNodeOrder(state, availableNodes);

            if (fixedNode != null) {
                for (byte processor = 0; processor < processors; processor++) {
                    expandState(state, fixedNode, processor);
                }
            } else {
                for (NodeModel node : availableNodes) {
                    for (byte processor = 0; processor < processors; processor++) {
                        expandState(state, node, processor);
                    }
                }
            }
        }

        /**
         * Expand the current state.
         *
         * @param state the current state
         * @param node the current node
         * @param processor the processor
         */
        private void expandState(StateModel state, NodeModel node, byte processor) {
            if (isFirstAvailableNode(state, node)) {
                return;
            }

            StateModel nextState = state.clone();

            int earliestStartTime = getEarliestStartTime(state, node, processor);

            nextState.addNode(node, processor, earliestStartTime);
            nextState.setParentMaximumBottomLevelPathLength(state.getMaximumBottomLevelPathLength());

            if (canPruneState(nextState)) {
                return;
            }

            if (isStateEquivalent(nextState, node, processor)) {
                return;
            }

            distributeWork(nextState);

            metrics.incrementNumberOfOpenedStates();
        }

        /**
         * Checks if a state can be pruned.
         *
         * @param state the current state
         * @return if the state can be pruned
         */
        private boolean canPruneState(StateModel state) {
            if (!closedStates.add(state)) {
                return true;
            }

            return state.getMaximumFinishTime() >= getBestStateFinishTime();
        }

        /**
         * Gets the best state finish time
         *
         * @return the best state finish time
         */
        private int getBestStateFinishTime() {
            synchronized (bestStateLock) {
                return bestState.getMaximumFinishTime();
            }
        }

        /**
         * Distribute workload to other worker threads
         *
         * @param state the current state
         */
        private void distributeWork(StateModel state) {
            workers[getWorkerId()].openedStates.add(state);
            incrementWorkerId();
        }
    }
}