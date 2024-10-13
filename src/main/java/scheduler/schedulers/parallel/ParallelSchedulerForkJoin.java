package scheduler.schedulers.parallel;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.sequential.AStarScheduler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParallelSchedulerForkJoin extends AStarScheduler {
    private final ForkJoinPool forkJoinPool;

    private final Set<StateModel> closedStates;

    private final Object bestStateLock;

    private final AtomicBoolean isBestStateFound;

    public ParallelSchedulerForkJoin(GraphModel graph, byte processors, byte cores) {
        super(graph, processors);

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

        this.forkJoinPool.invoke(new ParallelScheduleTask(initialState));

        metrics.setBestState(this.bestState);
        metrics.setNumberOfClosedStates(this.closedStates.size());
    }

    private class ParallelScheduleTask extends RecursiveTask<Void> {
        private final StateModel currentState;

        public ParallelScheduleTask(StateModel state) {
            this.currentState = state;
        }

        @Override
        protected Void compute() {
            if (currentState.areAllNodesScheduled()) {
                updateBestState(currentState);
                isBestStateFound.set(true);
                return null;
            }

            if (isBestStateFound.get()) {
                return null;
            }

            for (NodeModel node : getAvailableNodes(currentState)) {
                for (int processor = 0; processor < processors; processor++) {
                    StateModel nextState = expandState(currentState, node, processor);

                    if (nextState == null) {
                        continue;
                    }

                    ParallelScheduleTask task = new ParallelScheduleTask(nextState);
                    task.fork();
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

            int earliestStartTime = getEarliestStartTime(state, node, processor);

            nextState.addNode(node, processor, earliestStartTime);
            nextState.setParentMaximumBottomLevelPathLength(state.getMaximumBottomLevelPathLength());

            if (canPruneState(nextState)) {
                return null;
            }

            metrics.incrementNumberOfOpenedStates();
            return nextState;
        }
    }
}