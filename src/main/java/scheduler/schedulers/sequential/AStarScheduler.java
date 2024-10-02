package scheduler.schedulers.sequential;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.Scheduler;

public class AStarScheduler extends Scheduler {
    public AStarScheduler(GraphModel graph, int processors) {
        super(graph, processors);
    }

    @Override
    public void schedule() {
        PriorityQueue<StateModel> openedStates = new PriorityQueue<>(Comparator.comparingInt(this::f));
        Set<StateModel> closedStates = new HashSet<>();

        openedStates.add(new StateModel(this.processors, this.numberOfNodes));

        while (!openedStates.isEmpty()) {
            StateModel currentState = openedStates.poll();
            System.out.println(currentState.getMaximumFinishTime());

            if (currentState.areAllNodesScheduled()) {
                setBestState(currentState);
                return;
            }

            closedStates.add(currentState);

            for (NodeModel node : getAvailableNodes(currentState)) {
                for (int processor = 0; processor < processors; processor++) {
                    StateModel nextState = currentState.clone();

                    int earliestStartTime = getEarliestStartTime(currentState, node, processor);

                    nextState.addNode(node, processor, earliestStartTime);

                    if (!closedStates.contains(nextState)) {
                        openedStates.add(nextState);
                    }
                }
            }
        }
    }

    public int f(StateModel state) {
        if (state.isEmptyState()) {
            return getLowerBound();
        }

        int idleTime = getIdleTime(state);
        int maximumDataReadyTime = getMaximumDataReadyTime(state);
        int maximumBottomLevelPathLength = getMaximumBottomLevelPathLength(state);

        return Math.max(idleTime, Math.max(maximumBottomLevelPathLength, maximumDataReadyTime));
    }

    public int getLowerBound() {
        double loadBalancedTime = (double) graph.getTotalNodeWeight() / this.processors;

        return (int) Math.max(Math.ceil(loadBalancedTime), getCriticalPathLength());
    }

    public int getIdleTime(StateModel state) {
        double totalWeight = (double) graph.getTotalNodeWeight() + state.getTotalIdleTime();

        return (int) Math.ceil(totalWeight / this.processors);
    }

    // V2
    public int getMaximumBottomLevelPathLength(StateModel state) {
        int maximumBottomLevelPathLength = 0;

        for (NodeModel node : getScheduledNodes(state)) {
            int cost = state.getNodeStartTime(node) + bottomLevelPathLengths[node.getByteId()];
            maximumBottomLevelPathLength = Math.max(maximumBottomLevelPathLength, cost);
        }

        return maximumBottomLevelPathLength;
    }

    public int getMaximumDataReadyTime(StateModel state) {
        int maximumDataReadyTime = 0;

        for (NodeModel node : getAvailableNodes(state)) {
            int cost = getMinimumDataReadyTime(state, node) + bottomLevelPathLengths[node.getByteId()];
            maximumDataReadyTime = Math.max(maximumDataReadyTime, cost);
        }

        return maximumDataReadyTime;
    }

    public int getMinimumDataReadyTime(StateModel state, NodeModel node) {
        int minimumDataReadyTime = Integer.MAX_VALUE;

        for (int processor = 0; processor < this.processors; processor++) {
            int dataReadyTime = getEarliestStartTime(state, node, processor);
            minimumDataReadyTime = Math.min(minimumDataReadyTime, dataReadyTime);
        }

        return minimumDataReadyTime;
    }
}
