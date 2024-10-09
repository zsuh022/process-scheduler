package scheduler.schedulers.sequential;

import java.util.*;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.Scheduler;

import static scheduler.constants.Constants.INFINITY_32;

public class AStarScheduler extends Scheduler {
    private final PriorityQueue<StateModel> openedStates;

    protected volatile StateModel bestState;

    public AStarScheduler(GraphModel graph, byte processors) {
        super(graph, processors);

        this.openedStates = new PriorityQueue<>(Comparator.comparingInt(this::getFCost));

        this.bestState = this.getValidSchedule();
    }

    @Override
    public void schedule() {
        this.openedStates.add(new StateModel(processors, numberOfNodes));

        while (!this.openedStates.isEmpty()) {
            StateModel state = this.openedStates.poll();

            if (state.areAllNodesScheduled()) {
                System.out.println(state.getMaximumFinishTime());
                this.bestState = state;

                break;
            }

            for (NodeModel node : getAvailableNodes(state)) {
                for (int processor = 0; processor < this.processors; processor++) {
                    expandState(state, node, processor);
                }
            }
        }

        metrics.setBestState(this.bestState);
        metrics.setNumberOfClosedStates(closedStates.size());
    }

    private void expandState(StateModel state, NodeModel node, int processor) {
        if (!isFirstAvailableNode(state, node)) {
            return;
        }

        StateModel nextState = state.clone();

        int earliestStartTime = getEarliestStartTime(state, node, processor);

        nextState.addNode(node, processor, earliestStartTime);

        if (!canPruneState(nextState)) {
            this.openedStates.add(nextState);

            metrics.incrementNumberOfOpenedStates();
        }
    }

    protected boolean isFirstAvailableNode(StateModel state, NodeModel node) {
        List<NodeModel> equivalentNodeGroup = graph.getEquivalentNodeGroup(node.getGroupId());

        for (NodeModel equivalentNode : equivalentNodeGroup) {
            // both tasks are both the same, so we can continue with scheduling it
            if (equivalentNode.equals(node)) {
                return true;
            }

            // if there is an earlier equivalent task that is not scheduled
            if (!state.isNodeScheduled(equivalentNode)) {
                return false;
            }
        }

        return true;
    }

    protected StateModel getValidSchedule() {
        StateModel state = new StateModel(processors, numberOfNodes);

        for (NodeModel node : nodes) {
            int bestStartTime = INFINITY_32;
            int processorWithBestStartTime = -1;

            for (int processor = 0; processor < processors; processor++) {
                int earliestStartTime = getEarliestStartTime(state, node, processor);

                if (earliestStartTime < bestStartTime) {
                    bestStartTime = earliestStartTime;
                    processorWithBestStartTime = processor;
                }
            }

            state.addNode(node, processorWithBestStartTime, bestStartTime);
        }

        return state;
    }

    protected boolean canPruneState(StateModel state) {
        if (!closedStates.add(state)) {
            return true;
        }

        return state.getMaximumFinishTime() >= this.bestState.getMaximumFinishTime();
//        return state.getFCost() >= this.bestState.getMaximumFinishTime();
    }

    protected int getFCost(StateModel state) {
        if (state.isEmptyState()) {
            return getLowerBound();
        }

        int idleTime = getIdleTime(state);
        int maximumDataReadyTime = getMaximumDataReadyTime(state);
        int maximumBottomLevelPathLength = getMaximumBottomLevelPathLength(state);

        int fCost = Math.max(idleTime, Math.max(maximumBottomLevelPathLength, maximumDataReadyTime));

        state.setFCost(fCost);

        return fCost;
    }

    protected int getLowerBound() {
        double loadBalancedTime = (double) graph.getTotalNodeWeight() / processors;

        return (int) Math.max(Math.ceil(loadBalancedTime), getCriticalPathLength());
    }

    protected int getIdleTime(StateModel state) {
        double totalWeight = (double) graph.getTotalNodeWeight() + state.getTotalIdleTime();

        return (int) Math.ceil(totalWeight / processors);
    }

    // V2- can be reduced to O(1) but with increased memory? I'll see if it is worth it
    // note: fbl(s)=max(fbl(s_parent),ts(last)+bl(last))
    protected int getMaximumBottomLevelPathLength(StateModel state) {
        int maximumBottomLevelPathLength = 0;

        for (NodeModel node : getScheduledNodes(state)) {
            int cost = state.getNodeStartTime(node) + bottomLevelPathLengths[node.getByteId()];
            maximumBottomLevelPathLength = Math.max(maximumBottomLevelPathLength, cost);
        }

        return maximumBottomLevelPathLength;
    }

    // This is actually amortised O(1) from O(|free(s)| * |P|)
    protected int getMaximumDataReadyTime(StateModel state) {
        int maximumDataReadyTime = 0;

        for (NodeModel node : getAvailableNodes(state)) {
            int cost = getMinimumDataReadyTime(state, node) + bottomLevelPathLengths[node.getByteId()];
            maximumDataReadyTime = Math.max(maximumDataReadyTime, cost);
        }

        return maximumDataReadyTime;
    }

    protected int getMinimumDataReadyTime(StateModel state, NodeModel node) {
        int minimumDataReadyTime = INFINITY_32;

        for (int processor = 0; processor < processors; processor++) {
            int dataReadyTime = getEarliestStartTime(state, node, processor);
            minimumDataReadyTime = Math.min(minimumDataReadyTime, dataReadyTime);
        }

        return minimumDataReadyTime;
    }
}
