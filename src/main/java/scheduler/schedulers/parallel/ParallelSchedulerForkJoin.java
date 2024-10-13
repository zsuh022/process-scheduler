package scheduler.schedulers.parallel;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.sequential.AStarScheduler;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Parallel scheduler using a correct dynamic load distribution technique. We distribute the load using fork-and-join
 * divide and conquer technique.
 */
public class ParallelSchedulerForkJoin extends AStarScheduler {
    private final PriorityBlockingQueue<StateModel> priorityQueue;

    private final ForkJoinPool forkJoinPool;

    private final Object bestStateLock;

    private final AtomicBoolean isBestStateFound;

    /**
     * Constructor for the ParallelSchedulerForkJoin class
     *
     * @param graph the input graph
     * @param processors the number of processors
     * @param cores the number of cores
     */
    public ParallelSchedulerForkJoin(GraphModel graph, byte processors, byte cores) {
        super(graph, processors);

        this.priorityQueue = new PriorityBlockingQueue<>(11, Comparator.comparingInt(this::getFCost));

        this.forkJoinPool = new ForkJoinPool(cores);

        closedStates = ConcurrentHashMap.newKeySet();

        this.bestStateLock = new Object();

        this.isBestStateFound = new AtomicBoolean(false);
    }

    /**
     * Checks if a state can be pruned or not.
     *
     * @param state the current state
     * @return if a state can be pruned or not
     */
    public boolean canPruneState(StateModel state) {
        if (!closedStates.add(state)) {
            return true;
        }

        synchronized (bestStateLock) {
            return state.getMaximumFinishTime() >= bestState.getMaximumFinishTime();
        }
    }

    /**
     * Start the parallel scheduling process.
     */
    @Override
    public void schedule() {
        StateModel initialState = new StateModel(this.processors, this.numberOfNodes);

        this.priorityQueue.add(initialState);

        this.forkJoinPool.invoke(new ParallelScheduleTask());

        setCurrentState(bestState);

        metrics.setBestState(bestState);
        metrics.setNumberOfClosedStates(closedStates.size());
    }

    private class ParallelScheduleTask extends RecursiveTask<Void> {
        /**
         * State the worker thread.
         *
         * @return nothing
         */
        @Override
        protected Void compute() {
            while (!priorityQueue.isEmpty()) {
                StateModel state = priorityQueue.poll();

                setCurrentState(state);

                if (state == null || isBestStateFound.get()) {
                    return null;
                }

                if (state.areAllNodesScheduled()) {
                    updateBestState(state);
                    isBestStateFound.set(true);

                    return null;
                }

                expandStates(state);
            }

            return null;
        }

        /**
         * Expand the current state into many other possible optimal states.
         *
         * @param state the current state
         */
        private void expandStates(StateModel state) {
            List<NodeModel> availableNodes = getAvailableNodes(state);

            NodeModel fixedNode = getFixedNodeOrder(state, availableNodes);

            if (fixedNode != null) {
                expandFixedTaskOrderState(state, fixedNode);
            } else {
                expandNormalState(state, availableNodes);
            }
        }

        /**
         * Expand state due to fixed task ordering.
         *
         * @param state the current state
         * @param node the current node
         */
        private void expandFixedTaskOrderState(StateModel state, NodeModel node) {
            for (byte processor = 0; processor < processors; processor++) {
                StateModel nextState = expandState(state, node, processor);

                if (nextState != null) {
                    priorityQueue.add(nextState);
                }
            }
        }

        /**
         * Perform normal partial schedule expansion, because no valid fixed task order was found.
         *
         * @param state the current state
         * @param availableNodes the list of available nodes
         */
        private void expandNormalState(StateModel state, List<NodeModel> availableNodes) {
            for (NodeModel node : availableNodes) {
                for (byte processor = 0; processor < processors; processor++) {
                    StateModel nextState = expandState(state, node, processor);

                    if (nextState != null) {
                        priorityQueue.add(nextState);
                    }
                }
            }
        }

        /**
         * Updates the best state.
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
         * Expand the current state.
         *
         * @param state the current state
         * @param node the current node
         * @param processor the processor
         * @return the next state
         */
        private StateModel expandState(StateModel state, NodeModel node, byte processor) {
            if (isFirstAvailableNode(state, node)) {
                return null;
            }

            StateModel nextState = state.clone();

            int earliestStartTime = getEarliestStartTime(state, node, processor);

            nextState.addNode(node, processor, earliestStartTime);
            nextState.setParentMaximumBottomLevelPathLength(state.getMaximumBottomLevelPathLength());

            if (canPruneState(nextState)) {
                return null;
            }

            if (isStateEquivalent(nextState, node, processor)) {
                return null;
            }

            metrics.incrementNumberOfOpenedStates();

            return nextState;
        }
    }
}
