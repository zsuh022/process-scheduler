package scheduler.schedulers.parallel;

import scheduler.models.GraphModel;
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
    public boolean canPruneState(StateModel state) {
        if (!this.closedStates.add(state)) {
            return true;
        }

        return state.getMaximumFinishTime() >= this.bestState.getMaximumFinishTime();
    }

    @Override
    public void schedule() {
        StateModel initialState = new StateModel(this.processors, this.numberOfNodes);

        this.forkJoinPool.invoke(new ParallelScheduler(initialState));

        metrics.setBestState(this.bestState);
        metrics.setNumberOfClosedStates(this.closedStates.size());
    }

    private class ParallelScheduler extends RecursiveTask<Void> {
        private final StateModel state;

        public ParallelScheduler(StateModel state) {
            this.state = state;
        }

        @Override
        protected Void compute() {
            return null;
        }
    }
}
