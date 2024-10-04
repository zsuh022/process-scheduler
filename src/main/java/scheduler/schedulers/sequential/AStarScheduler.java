package scheduler.schedulers.sequential;

import java.util.*;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.Scheduler;

public class AStarScheduler extends Scheduler {
    private PriorityQueue<StateModel> openedStates;

    public AStarScheduler(GraphModel graph, int processors) {
        super(graph, processors);

        this.openedStates = new PriorityQueue<>(Comparator.comparingInt(this::getFCost));
    }

    @Override
    public void schedule() {
        this.openedStates.add(new StateModel(this.processors, this.numberOfNodes));

        while (!this.openedStates.isEmpty()) {
            StateModel currentState = this.openedStates.poll();
            System.out.println(currentState.getMaximumFinishTime());

            if (currentState.areAllNodesScheduled()) {
                setBestState(currentState);
                return;
            }

            closedStates.add(currentState);

            for (NodeModel node : getAvailableNodes(currentState)) {
                for (int processor = 0; processor < this.processors; processor++) {
                    expandState(currentState, node, processor);
                }
            }
        }
    }

    private void expandState(StateModel state, NodeModel node, int processor) {
        // We only need to schedule the first available free task
        if (isFirstAvailableNode(state, node, processor)) {

        }

        StateModel nextState = state.clone();

        int earliestStartTime = getEarliestStartTime(state, node, processor);

        nextState.addNode(node, processor, earliestStartTime);

        if (!isStatePruned(nextState)) {
            this.openedStates.add(nextState);
        }
    }

    private boolean isFirstAvailableNode(StateModel state, NodeModel node, int processor) {
        List<NodeModel> equivalentNodeGroup = this.graph.getEquivalentNodeGroup(node.getGroupId());

        for (NodeModel equivalentNode : equivalentNodeGroup) {
            // They are both the same
            if (equivalentNode.equals(node)) {
                return true;
            }

            // if there is an equivalent node that is not scheduled
            if (!state.isNodeScheduled(equivalentNode)) {
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    private void getValidSchedule() {

    }

    private boolean isStatePruned(StateModel state) {
        if (closedStates.contains(state)) {
            return true;
        }

        return false;
    }

    public int getFCost(StateModel state) {
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
