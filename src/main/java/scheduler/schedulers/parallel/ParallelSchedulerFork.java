package scheduler.schedulers.parallel;

import scheduler.models.GraphModel;
import scheduler.models.StateModel;
import scheduler.schedulers.sequential.AStarScheduler;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class ParallelSchedulerFork extends AStarScheduler {
    private final ForkJoinPool forkJoinPool;

    private final PriorityQueue<StateModel> initialStates;

    private final Set<StateModel> closedStates;

    private final Object bestStateLock;

    public ParallelSchedulerFork(GraphModel graph, byte processors, byte cores) {
        super(graph, processors);

        this.forkJoinPool = new ForkJoinPool(cores);

        this.initialStates = new PriorityQueue<>(Comparator.comparingInt(this::getFCost));

        this.closedStates = ConcurrentHashMap.newKeySet();

        this.bestStateLock = new Object();
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
        // fork join

        metrics.setBestState(this.bestState);
        metrics.setNumberOfClosedStates(this.closedStates.size());
    }
}
