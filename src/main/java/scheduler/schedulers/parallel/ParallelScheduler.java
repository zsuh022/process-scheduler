package scheduler.schedulers.parallel;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.Scheduler;
import scheduler.schedulers.sequential.AStarScheduler;

import java.util.*;
import java.util.concurrent.*;

import static scheduler.constants.Constants.INFINITY_32;

public class ParallelScheduler extends AStarScheduler {
    private final PriorityBlockingQueue<StateModel> openedStates;

    private final Set<StateModel> closedStates;

    private final ExecutorService executorService;
    private final StateModel validState;

    public ParallelScheduler(GraphModel graph, byte processors, byte cores) {
        super(graph, processors);

        this.openedStates = new PriorityBlockingQueue<>(cores, Comparator.comparingInt(this::getFCost));

        this.closedStates = ConcurrentHashMap.newKeySet();

        this.executorService = Executors.newFixedThreadPool(cores);

        this.validState = getValidSchedule();
    }

    @Override
    public void schedule() {
        boolean isBestStateFound = false;

        this.openedStates.add(new StateModel(processors, numberOfNodes));

        try {
            while (!this.openedStates.isEmpty()) {
                StateModel state = this.openedStates.poll();

                if (state == null) {
                    continue;
                }

                if (state.areAllNodesScheduled()) {
                    metrics.setBestState(state);
                    isBestStateFound = true;
                    break;
                }

                closedStates.add(state);

                List<NodeModel> availableNodes = getAvailableNodes(state);

                List<Future<?>> futures = new ArrayList<>();

                for (NodeModel node : availableNodes) {
                    if (!isFirstAvailableNode(state, node)) {
                        continue;
                    }

                    for (int processor = 0; processor < processors; processor++) {
                        final int proc = processor;
                        Future<?> future = executorService.submit(() -> expandState(state, node, proc));
                        futures.add(future);
                    }
                }

                // Wait for all expansions to complete before proceeding
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException ex) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (!isBestStateFound) {
            metrics.setBestState(this.validState);
        }

        metrics.setNumberOfClosedStates(closedStates.size());
    }

    private void expandState(StateModel state, NodeModel node, int processor) {
        StateModel nextState = state.clone();

        int earliestStartTime = getEarliestStartTime(state, node, processor);

        nextState.addNode(node, processor, earliestStartTime);

        if (canPruneState(nextState)) {
            return;
        }

        boolean isNewState = closedStates.add(nextState);
        if (!isNewState) {
            return;
        }

        openedStates.put(nextState);
        metrics.incrementNumberOfOpenedStates();
    }
}
