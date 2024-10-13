package scheduler.schedulers.parallel;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.sequential.AStarScheduler;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParallelSchedulerForkJoin extends AStarScheduler {
    private final PriorityBlockingQueue<StateModel> priorityQueue;

    private final ForkJoinPool forkJoinPool;

    private final Set<StateModel> closedStates;

    private final Object bestStateLock;

    private final AtomicBoolean isBestStateFound;

    public ParallelSchedulerForkJoin(GraphModel graph, byte processors, byte cores) {
        super(graph, processors);

        this.priorityQueue = new PriorityBlockingQueue<>(11, Comparator.comparingInt(this::getFCost));

        this.forkJoinPool = new ForkJoinPool(cores);

        this.closedStates = ConcurrentHashMap.newKeySet();

        this.bestStateLock = new Object();

        this.isBestStateFound = new AtomicBoolean(false);
    }

    @Override
    public boolean canPruneState(StateModel currentState) {
        if (!this.closedStates.add(currentState)) {
            // prune if already in closed states
            return true;
        }

        synchronized (bestStateLock) {
            return currentState.getMaximumFinishTime() >= this.bestState.getMaximumFinishTime();
        }
    }

    @Override
    public void schedule() {
        StateModel initialState = new StateModel(this.processors, this.numberOfNodes);

        this.priorityQueue.add(initialState);

        this.forkJoinPool.invoke(new ParallelScheduleTask());

        metrics.setBestState(this.bestState);
        metrics.setNumberOfClosedStates(this.closedStates.size());
    }

    private class ParallelScheduleTask extends RecursiveTask<Void> {

        @Override
        protected Void compute() {
            while (!priorityQueue.isEmpty()) {
                StateModel currentState = priorityQueue.poll();

                if (currentState == null || isBestStateFound.get()) {
                    return null;
                }

                if (currentState.areAllNodesScheduled()) {
                    updateBestState(currentState);
                    isBestStateFound.set(true);
                    return null;
                }

                for (NodeModel node : getAvailableNodes(currentState)) {
                    for (int processor = 0; processor < processors; processor++) {
                        StateModel nextState = expandState(currentState, node, processor);

                        if (nextState != null) {
                            priorityQueue.add(nextState);
                        }
                    }
                }
            }

            return null;
        }

        private void updateBestState(StateModel currentState) {
            synchronized (bestStateLock) {
                if (currentState.getMaximumFinishTime() < bestState.getMaximumFinishTime()) {
                    bestState = currentState;
                }
            }
        }

        private StateModel expandState(StateModel state, NodeModel node, int processor) {
            if (isFirstAvailableNode(state, node)) {
                return null;
            }

            StateModel nextState = state.clone();

            int earliestStartTime = getEarliestStartTime(state, node, (byte) processor);

            nextState.addNode(node, (byte) processor, earliestStartTime);
            nextState.setParentMaximumBottomLevelPathLength(state.getMaximumBottomLevelPathLength());

            if (canPruneState(nextState)) {
                return null;
            }

            metrics.incrementNumberOfOpenedStates();
            return nextState;
        }
    }
}
