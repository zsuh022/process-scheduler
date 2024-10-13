package scheduler.schedulers.sequential;

import java.util.*;

import scheduler.models.EdgeModel;
import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.Scheduler;

import static scheduler.constants.Constants.INFINITY_32;

public class AStarScheduler extends Scheduler {
    private final PriorityQueue<StateModel> openedStates;

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

            setCurrentState(state);

            if (state.areAllNodesScheduled()) {
                this.bestState = state;

                break;
            }

            expandStates(state);
        }

        setCurrentState(this.bestState);

        metrics.setBestState(this.bestState);
        metrics.setNumberOfClosedStates(closedStates.size());
    }

    private void expandState(StateModel state, NodeModel node, byte processor) {
        if (isFirstAvailableNode(state, node)) {
            return;
        }

        StateModel nextState = state.clone();

        int earliestStartTime = getEarliestStartTime(state, node, processor);

        nextState.addNode(node, processor, earliestStartTime);
        nextState.setParentMaximumBottomLevelPathLength(state.getMaximumBottomLevelPathLength());

        if (isStateEquivalent(nextState, node, processor)) {
            return;
        }

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
                return false;
            }

            // if there is an earlier equivalent task that is not scheduled
            if (!state.isNodeScheduled(equivalentNode)) {
                return true;
            }
        }

        return false;
    }

    protected StateModel getValidSchedule() {
        StateModel state = new StateModel(processors, numberOfNodes);

        for (NodeModel node : nodes) {
            int bestStartTime = INFINITY_32;
            byte processorWithBestStartTime = (byte) -1;

            for (byte processor = 0; processor < processors; processor++) {
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

    private void expandStates(StateModel state) {
        List<NodeModel> availableNodes = getAvailableNodes(state);

        NodeModel fixedNode = getFixedNodeOrder(state, availableNodes);

        if (fixedNode != null) {
            for (byte processor = 0; processor < processors; processor++) {
                expandState(state, fixedNode, processor);
            }
        } else {
            for (NodeModel node : availableNodes) {
                for (byte processor = 0; processor < processors; processor++) {
                    expandState(state, node, processor);
                }
            }
        }
    }

    protected boolean canPruneState(StateModel state) {
        if (!closedStates.add(state)) {
            return true;
        }

        return state.getMaximumFinishTime() >= this.bestState.getMaximumFinishTime();
    }

    // State is the partial state
    protected boolean isStateEquivalent(StateModel state, NodeModel node, byte processor) {
        int maximumFinishTime = state.getNodeStartTime(node) + node.getWeight();

        // get the nodes on the same processor (byte id for optimal memory instead of entire node object)
        List<Byte> nodesOnSameProcessor = state.getNodesOnSameProcessorSortedOnStartTime(processor);

        int[] originalNodeStartTimes = state.getNodeStartTimes();
        int[] copyNodeStartTimes = originalNodeStartTimes.clone();

        for (int nodeIndex = nodesOnSameProcessor.size() - 1; nodeIndex > 0; nodeIndex--) {
            byte nodeAId = nodesOnSameProcessor.get(nodeIndex);
            byte nodeBId = nodesOnSameProcessor.get(nodeIndex - 1);

            // swap node m and node ni
            nodesOnSameProcessor.set(nodeIndex - 1, nodeAId);
            nodesOnSameProcessor.set(nodeIndex, nodeBId);

            int startTime = 0;

            if (nodeIndex > 1) {
                byte nodeId = nodesOnSameProcessor.get(nodeIndex - 2);
                startTime = state.getNodeStartTime(nodeId) + nodes[nodeId].getWeight();
            }

            copyNodeStartTimes[nodeAId] = getEarliestStartTime(state, nodeAId, copyNodeStartTimes, processor, startTime);

            // Schedule m and ni, nl-1 each as early as possible
            for (int index = nodeIndex; index < nodesOnSameProcessor.size(); index++) {
                byte nodeId = nodesOnSameProcessor.get(index);
                byte previousNodeId = nodesOnSameProcessor.get(index - 1);

                int currentStartTime = copyNodeStartTimes[previousNodeId] + nodes[nodeId].getWeight();

                copyNodeStartTimes[nodeId] = getEarliestStartTime(state, nodeId, copyNodeStartTimes, processor, currentStartTime);
            }

            byte lastNodeId = nodesOnSameProcessor.get(nodesOnSameProcessor.size() - 1);

            int lastNodeFinishTime = copyNodeStartTimes[lastNodeId] + nodes[lastNodeId].getWeight();

            if (lastNodeFinishTime <= maximumFinishTime && isOutgoingCommunicationsOk(state, nodeIndex, nodesOnSameProcessor, copyNodeStartTimes, processor)) {
                return true;
            }
        }

        return false;
    }

    public boolean isOutgoingCommunicationsOk(StateModel state, int nodeIndex, List<Byte> nodesOnSameProcessor, int[] nodeStartTimes, byte processor) {
        for (int index = nodeIndex; index < nodesOnSameProcessor.size(); index++) {
            byte nodeId = nodesOnSameProcessor.get(index);

            // Check only if nk starts later
            if (nodeStartTimes[nodeId] > state.getNodeStartTime(nodeId)) {
                NodeModel node = nodes[nodeId];

                // for each child
                for (NodeModel successor : node.getSuccessors()) {
                    // remote data arrival from nk
                    int finishTime = nodeStartTimes[nodeId] + node.getWeight();
                    int edgeCost = getEdge(node, successor).getWeight();

                    int dataArrivalTime = finishTime + edgeCost;

                    if (state.isNodeScheduled(successor)) {
                        // Check if on same processor
                        if (nodeStartTimes[successor.getByteId()] > dataArrivalTime && state.getNodeProcessor(successor) != processor) {
                            return false;
                        }
                    } else { // not scheduled
                        for (byte processorIndex = 0; processorIndex < processors; processorIndex++) {
                            if (processorIndex == processor) {
                                continue;
                            }

                            boolean atLeastOneLater = false;

                            for (NodeModel predecessor : successor.getPredecessors()) {
                                if (predecessor.getByteId() == node.getByteId()) {
                                    continue;
                                }

                                if (nodeStartTimes[predecessor.getByteId()] + predecessor.getWeight() >= dataArrivalTime) {
                                    atLeastOneLater = true;
                                    break;
                                }
                            }

                            if (!atLeastOneLater) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    protected int getFCost(StateModel state) {
        if (state.isEmpty()) {
            return getLowerBound();
        }

        int idleTime = getIdleTime(state);
        int maximumDataReadyTime = getMaximumDataReadyTime(state);
        int maximumBottomLevelPathLength = getMaximumBottomLevelPathLength(state);

        int fCost = Math.max(idleTime, Math.max(maximumBottomLevelPathLength, maximumDataReadyTime));

        state.setFCost(fCost);
        state.setMaximumBottomLevelPathLength(maximumBottomLevelPathLength);

        return fCost;
    }

    private int getLowerBound() {
        double loadBalancedTime = (double) graph.getTotalNodeWeight() / processors;

        return (int) Math.max(Math.ceil(loadBalancedTime), getCriticalPathLength());
    }

    protected int getIdleTime(StateModel state) {
        double totalWeight = (double) graph.getTotalNodeWeight() + state.getTotalIdleTime();

        return (int) Math.ceil(totalWeight / processors);
    }

     protected int getMaximumBottomLevelPathLength(StateModel state) {
        if (state.isEmpty()) {
            return 0;
        }

        byte lastNodeId = state.getLastNode();

        int estimatedFinishTime = state.getNodeStartTime(lastNodeId) + bottomLevelPathLengths[lastNodeId];
        int parentBottomLevelPathLength = state.getParentMaximumBottomLevelPathLength();

        return Math.max(parentBottomLevelPathLength, estimatedFinishTime);
    }

    protected int getMaximumDataReadyTime(StateModel state) {
        int maximumDataReadyTime = 0;

        for (NodeModel node : getAvailableNodes(state)) {
            int cost = getMinimumDataReadyTime(state, node) + bottomLevelPathLengths[node.getByteId()];
            maximumDataReadyTime = Math.max(maximumDataReadyTime, cost);
        }

        return maximumDataReadyTime;
    }

    protected NodeModel getFixedNodeOrder(StateModel state, List<NodeModel> availableNodes) {
        Set<NodeModel> availableSuccessors = new HashSet<>();
        Set<NodeModel> availablePredecessors = new HashSet<>();

        // For each free node
        for (NodeModel node : availableNodes) {
            // Free node must have at most one parent and child
            if (node.getInDegree() > 1 || node.getOutDegree() > 1) {
                return null;
            }

            // get parent and children
            if (node.getInDegree() > 0) {
                availablePredecessors.add(node.getPredecessor(0));
            }

            if (node.getOutDegree() > 0) {
                availableSuccessors.add(node.getSuccessor(0));
            }
        }

        //2. if tasks have children, they all must share the same child
        if (availableSuccessors.size() > 1) {
            return null;
        }

        //3. if tasks have parents, they must be scheduled on the same processor
        if (!availablePredecessors.isEmpty()) {
            Set<Byte> predecessorProcessors = new HashSet<>();

            for (NodeModel predecessor : availablePredecessors) {
                if (!state.isNodeScheduled(predecessor)) {
                    return null;
                }

                predecessorProcessors.add(state.getNodeProcessor(predecessor));
            }

            if (predecessorProcessors.size() > 1) {
                return null;
            }
        }

        // return the fixed order nodes if parents are not scheduled on the same processor
        return getSortedNode(state, availableNodes);
    }

    protected NodeModel getSortedNode(StateModel state, List<NodeModel> availableNodes) {
        List<NodeModel> sortedNodes = new ArrayList<>(availableNodes);

        sortedNodes.sort((nodeA, nodeB) -> {
            int dataReadyTimeA = getDataReadyTime(state, nodeA);
            int dataReadyTimeB = getDataReadyTime(state, nodeB);

            // Non-increasing drt
            if (dataReadyTimeA != dataReadyTimeB) {
                return Integer.compare(dataReadyTimeA, dataReadyTimeB);
            }

            // There is a tie, so we break it by using decreasing child edge cost
            int successorEdgeCostA = getSuccessorEdgeCost(nodeA);
            int successorEdgeCostB = getSuccessorEdgeCost(nodeB);

            return Integer.compare(successorEdgeCostB, successorEdgeCostA);
        });

        // Verify that the tasks are in decreasing child edge cost order
        for (int nodeId = 1; nodeId < sortedNodes.size(); nodeId++) {
            int currentEdgeCost = getSuccessorEdgeCost(sortedNodes.get(nodeId));
            int previousEdgeCost = getSuccessorEdgeCost(sortedNodes.get(nodeId - 1));

            if (previousEdgeCost < currentEdgeCost) {
                return null;
            }
        }

        if (sortedNodes.isEmpty()) {
            return null;
        }

        return sortedNodes.get(0);
    }

    protected int getSuccessorEdgeCost(NodeModel node) {
        if (node.getOutDegree() == 0) {
            return 0;
        }

        return getEdge(node, node.getSuccessor(0)).getWeight();
    }

    protected int getDataReadyTime(StateModel state, NodeModel node) {
        if (node.getInDegree() == 0) {
            return 0;
        }

        NodeModel predecessor = node.getPredecessor(0);

        if (!state.isNodeScheduled(predecessor)) {
            return INFINITY_32;
        }

        int dataReadyTime = state.getNodeStartTime(predecessor) + predecessor.getWeight();

        if (state.getNodeProcessor(predecessor) == state.getNodeProcessor(node)) {
            return dataReadyTime;
        }

        return dataReadyTime + getEdge(predecessor, node).getWeight();
    }

    protected int getMinimumDataReadyTime(StateModel state, NodeModel node) {
        int minimumDataReadyTime = INFINITY_32;

        for (byte processor = 0; processor < processors; processor++) {
            int dataReadyTime = getEarliestStartTime(state, node, processor);
            minimumDataReadyTime = Math.min(minimumDataReadyTime, dataReadyTime);
        }

        return minimumDataReadyTime;
    }
}
