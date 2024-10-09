package scheduler.schedulers.parallel;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.sequential.AStarScheduler;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelScheduler extends AStarScheduler {
    private final PriorityBlockingQueue<StateModel> openedStates;

    private final Set<StateModel> closedStates;
    private final Set<StateModel> sharedStates;

    private final ExecutorService threadPool;

    private final StateModel validState;

    private final AtomicInteger activeNodes;

    private final AtomicBoolean isBestStateFound;

    private final byte cores;

    public ParallelScheduler(GraphModel graph, byte processors, byte cores) {
        super(graph, processors);

        this.openedStates = new PriorityBlockingQueue<>(cores, Comparator.comparingInt(this::getFCost));

        this.closedStates = ConcurrentHashMap.newKeySet();
        this.sharedStates = ConcurrentHashMap.newKeySet();

        this.threadPool = Executors.newFixedThreadPool(cores);

        this.validState = getValidSchedule();

        this.activeNodes = new AtomicInteger(0);

        this.isBestStateFound = new AtomicBoolean(false);

        this.cores = cores;
    }

    private void createAndStartWorkerTasks() {
        for (byte i = 0; i < this.cores; i++) {
            this.threadPool.submit(new Worker());
        }
    }

    private void shutdownThreadPool() {
        this.threadPool.shutdown();

        try {
            if (!this.threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                this.threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.threadPool.shutdownNow();

            Thread.currentThread().interrupt();
        }

        if (!this.isBestStateFound.get()) {
            metrics.setBestState(this.validState);
        }

        metrics.setNumberOfClosedStates(this.closedStates.size());
    }

    @Override
    public void schedule() {
        StateModel initialState = new StateModel(this.processors, this.numberOfNodes);

        this.openedStates.add(initialState);
        this.closedStates.add(initialState);

        this.activeNodes.set(1);

        createAndStartWorkerTasks();

        shutdownThreadPool();
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            try {
                processPendingStates();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void processPendingStates() throws InterruptedException {
            while (true) {
                if (isBestStateFound.get()) {
                    break;
                }

                StateModel state = openedStates.poll(1, TimeUnit.SECONDS);
//                StateModel state = openedStates.poll();

                if (state == null) {
                    if (activeNodes.get() == 0 && openedStates.isEmpty()) {
                        break;
                    } else {
                        continue;
                    }
                }

                processState(state);

                activeNodes.decrementAndGet();
            }
        }

        private void processState(StateModel state) {
            if (state.areAllNodesScheduled()) {
                metrics.setBestState(state);

                isBestStateFound.set(true);

                return;
            }

            addStateToClosedStates(state);

            metrics.incrementNumberOfClosedStates();

            for (NodeModel node : getAvailableNodes(state)) {
                if (!isFirstAvailableNode(state, node)) {
                    continue;
                }

                for (int processor = 0; processor < processors; processor++) {
                    expandState(state, node, processor);
                }
            }
        }
    }

    protected void expandState(StateModel state, NodeModel node, int processor) {
        StateModel nextState = state.clone();

        int earliestStartTime = getEarliestStartTime(state, node, processor);

        nextState.addNode(node, processor, earliestStartTime);

        if (canPruneState(nextState)) {
            return;
        }

        boolean isNewState = addStateToSharedStates(nextState);

        if (!isNewState) {
            return;
        }

//        this.openedStates.add(nextState);
        addStateToOpenedStates(nextState);

        this.activeNodes.incrementAndGet();

        metrics.incrementNumberOfOpenedStates();
    }

    private synchronized void addStateToOpenedStates(StateModel state) {
        this.openedStates.add(state);
    }

    private synchronized boolean addStateToSharedStates(StateModel state) {
        return this.sharedStates.add(state);
    }

    private synchronized void addStateToClosedStates(StateModel state) {
        this.closedStates.add(state);
    }
}
