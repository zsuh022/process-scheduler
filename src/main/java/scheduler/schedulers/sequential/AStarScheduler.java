package scheduler.schedulers.sequential;

import java.util.*;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.Scheduler;

import static scheduler.constants.Constants.INFINITY_32;

/**
 * The AStarScheduler class contains all the necessary logic for finding an optimal schedule.
 */
public class AStarScheduler extends Scheduler {
    private final PriorityQueue<StateModel> openedStates;

    /**
     * The AStarScheduler constructor
     *
     * @param graph the input graph
     * @param processors the number of processors
     */
    public AStarScheduler(GraphModel graph, byte processors) {
        super(graph, processors);

        this.openedStates = new PriorityQueue<>(Comparator.comparingInt(this::getFCost));

        this.bestState = this.getValidSchedule();
    }

    /**
     * Perform the A star scheduling.
     */
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

    /**
     * Expand the current state. Pruning techniques are applied here to ensure that redundant states are not
     * added to the queue of opened states.
     *
     * @param state the current state
     * @param node the current node
     * @param processor the processor which the node will be added to
     */
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

    /**
     * Checks if the current node is the first available node to be scheduled.
     *
     * @param state the current state
     * @param node the current node
     * @return if the current node is the first available node
     */
    protected boolean isFirstAvailableNode(StateModel state, NodeModel node) {
        List<NodeModel> equivalentNodeGroup = graph.getEquivalentNodeGroup(node.getGroupId());

        for (NodeModel equivalentNode : equivalentNodeGroup) {
            if (equivalentNode.equals(node)) {
                return false;
            }

            if (!state.isNodeScheduled(equivalentNode)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a feasible schedule based on the greedy algorithm in Oliver Sinnen's research paper.
     *
     * @return a feasible schedule
     */
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

    /**
     * Expand the possible states. Fixed task ordering ensures that redundant states are pruned.
     *
     * @param state the current state
     */
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

    /**
     * Checks whether a state can be pruned or not
     *
     * @param state the current state
     * @return if a state can be pruned
     */
    protected boolean canPruneState(StateModel state) {
        if (!closedStates.add(state)) {
            return true;
        }

        return state.getMaximumFinishTime() >= this.bestState.getMaximumFinishTime();
    }

    /**
     * Checks if the current state is equivalent to any other state.
     *
     * @param state the current state
     * @param node the current node
     * @param processor the processor
     * @return if the current state is equivalent to any other state
     */
    protected boolean isStateEquivalent(StateModel state, NodeModel node, byte processor) {
        List<Byte> nodesOnSameProcessor = state.getNodesOnSameProcessorSortedOnStartTime(processor);

        int maximumFinishTime = state.getNodeStartTime(node) + node.getWeight();

        int[] originalNodeStartTimes = state.getNodeStartTimes();
        int[] copyNodeStartTimes = originalNodeStartTimes.clone();

        for (int nodeIndex = nodesOnSameProcessor.size() - 1; nodeIndex > 0; nodeIndex--) {
            byte nodeAId = nodesOnSameProcessor.get(nodeIndex);
            byte nodeBId = nodesOnSameProcessor.get(nodeIndex - 1);

            if (nodeAId >= nodeBId) {
                break;
            }

            nodesOnSameProcessor.set(nodeIndex - 1, nodeAId);
            nodesOnSameProcessor.set(nodeIndex, nodeBId);

            int startTime = 0;

            if (nodeIndex > 1) {
                byte nodeId = nodesOnSameProcessor.get(nodeIndex - 2);
                startTime = state.getNodeStartTime(nodeId) + nodes[nodeId].getWeight();
            }

            copyNodeStartTimes[nodeAId] = getEarliestStartTime(state, nodeAId, copyNodeStartTimes, processor, startTime);

            updateNodeStartTimes(state, nodeIndex, nodesOnSameProcessor, copyNodeStartTimes, processor);

            if (isValidFinishTime(state, nodeIndex,nodesOnSameProcessor, copyNodeStartTimes, maximumFinishTime, processor)) {
                return true;
            }

        }

        return false;
    }

    /**
     * Updates the node start times for the equivalent schedule pruning
     *
     * @param state the current state
     * @param nodeIndex the current node index
     * @param nodesOnSameProcessor the list of nodes on the same processor
     * @param nodeStartTimes the node start times
     * @param processor the processor
     */
    private void updateNodeStartTimes(StateModel state, int nodeIndex, List<Byte> nodesOnSameProcessor, int[] nodeStartTimes, byte processor) {
        for (int index = nodeIndex; index < nodesOnSameProcessor.size(); index++) {
            byte nodeId = nodesOnSameProcessor.get(index);
            byte previousNodeId = nodesOnSameProcessor.get(index - 1);

            int currentStartTime = nodeStartTimes[previousNodeId] + nodes[nodeId].getWeight();

            nodeStartTimes[nodeId] = getEarliestStartTime(state, nodeId, nodeStartTimes, processor, currentStartTime);
        }
    }

    /**
     * Checks if the finish time is valid for equivalent scheduling.
     *
     * @param state the current state
     * @param nodeIndex the current node index
     * @param nodesOnSameProcessor the list of nodes on the same processor
     * @param nodeStartTimes array of node start times
     * @param maximumFinishTime the maximum finish time
     * @param processor the processor
     * @return if the finish time is valid
     */
    private boolean isValidFinishTime(StateModel state, int nodeIndex, List<Byte> nodesOnSameProcessor, int[] nodeStartTimes, int maximumFinishTime, byte processor) {
        byte lastNodeId = nodesOnSameProcessor.get(nodesOnSameProcessor.size() - 1);

        int lastNodeFinishTime = nodeStartTimes[lastNodeId] + nodes[lastNodeId].getWeight();

        return lastNodeFinishTime <= maximumFinishTime && isOutgoingCommunicationsOk(state, nodeIndex, nodesOnSameProcessor, nodeStartTimes, processor);
    }

    /**
     * Checks if the outgoing communication is fine.
     *
     * @param state the current state
     * @param nodeIndex the current node index
     * @param nodesOnSameProcessor the list of nodes on the same processor
     * @param nodeStartTimes array of node start times
     * @param processor the processor
     * @return if the outgoing communication is fine
     */
    protected boolean isOutgoingCommunicationsOk(StateModel state, int nodeIndex, List<Byte> nodesOnSameProcessor, int[] nodeStartTimes, byte processor) {
        for (int index = nodeIndex; index < nodesOnSameProcessor.size(); index++) {
            byte nodeId = nodesOnSameProcessor.get(index);

            NodeModel node = nodes[nodeId];

            if (nodeStartTimes[nodeId] <= state.getNodeStartTime(nodeId)) {
                continue;
            }

            if (isSuccessorDelayed(state, node, nodeStartTimes, processor)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if the successor node is delayed due to swapping
     *
     * @param state the current state
     * @param node the current node
     * @param nodeStartTimes array of node start times
     * @param processor the processor
     * @return if the successor node is delayed
     */
    private boolean isSuccessorDelayed(StateModel state, NodeModel node, int[] nodeStartTimes, byte processor) {
        for (NodeModel successor : node.getSuccessors()) {
            int dataArrivalTime = getDataArrivalTime(node, successor, nodeStartTimes);

            if (state.isNodeScheduled(successor)) {
                boolean isSuccessorStartTimeValid = (nodeStartTimes[successor.getByteId()] <= dataArrivalTime);
                boolean isSuccessorScheduledOnSameProcessor = (state.getNodeProcessor(successor) != processor);

                if (!(isSuccessorStartTimeValid || isSuccessorScheduledOnSameProcessor)) {
                    return true;
                }
            } else {
                if (!isUnscheduledNodeSwappable(node, successor, nodeStartTimes, dataArrivalTime, processor)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks whether a node that is not scheduled yet is swappable with its successor node.
     *
     * @param node the current node
     * @param successor the node's successor
     * @param nodeStartTimes array of node start times
     * @param dataArrivalTime the data arrival time
     * @param processor the processor
     * @return if the unscheduled node is swappable
     */
    private boolean isUnscheduledNodeSwappable(NodeModel node, NodeModel successor, int[] nodeStartTimes, int dataArrivalTime, byte processor) {
        for (byte processorIndex = 0; processorIndex < processors; processorIndex++) {
            if (processorIndex == processor) {
                continue;
            }

            boolean canNodeBeScheduledLater = false;

            for (NodeModel predecessor : successor.getPredecessors()) {
                if (predecessor.getByteId() == node.getByteId()) {
                    continue;
                }

                if (getDataArrivalTime(predecessor, successor, nodeStartTimes) >= dataArrivalTime) {
                    canNodeBeScheduledLater = true;
                    break;
                }
            }

            if (!canNodeBeScheduledLater) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the data arrival time of a node.
     *
     * @param nodeA the first node (source)
     * @param nodeB the second node (destination)
     * @param nodeStartTimes array of node start times
     * @return the data arrival time
     */
    private int getDataArrivalTime(NodeModel nodeA, NodeModel nodeB, int[] nodeStartTimes) {
        int finishTime = nodeStartTimes[nodeA.getByteId()] + nodeA.getWeight();

        return finishTime + getEdge(nodeA, nodeB).weight();
    }

    /**
     * Returns the f-cost of the current state and is used in the A star.
     *
     * @param state the current state
     * @return the f-cost of the current state
     */
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

    /**
     * Returns the lower bound of graph, which is the load balanced time.
     *
     * @return the lower bound value
     */
    private int getLowerBound() {
        double loadBalancedTime = (double) graph.getTotalNodeWeight() / processors;

        return (int) Math.max(Math.ceil(loadBalancedTime), getCriticalPathLength());
    }

    /**
     * Returns the idle time of the current state. Used in h-cost calculation.
     *
     * @param state the current state
     * @return the idle time
     */
    protected int getIdleTime(StateModel state) {
        double totalWeight = (double) graph.getTotalNodeWeight() + state.getTotalIdleTime();

        return (int) Math.ceil(totalWeight / processors);
    }

    /**
     * Returns the maximum bottom level path length for the current state. Used for the h-cost.
     *
     * @param state the current state
     * @return the maximum bottom level path length
     */
     protected int getMaximumBottomLevelPathLength(StateModel state) {
        if (state.isEmpty()) {
            return 0;
        }

        byte lastNodeId = state.getLastNode();

        int estimatedFinishTime = state.getNodeStartTime(lastNodeId) + bottomLevelPathLengths[lastNodeId];
        int parentBottomLevelPathLength = state.getParentMaximumBottomLevelPathLength();

        return Math.max(parentBottomLevelPathLength, estimatedFinishTime);
    }

    /**
     * Returns the maximum data ready time used for the h-cost.
     *
     * @param state the current state
     * @return the maximum data ready time
     */
    protected int getMaximumDataReadyTime(StateModel state) {
        int maximumDataReadyTime = 0;

        for (NodeModel node : getAvailableNodes(state)) {
            int cost = getMinimumDataReadyTime(state, node) + bottomLevelPathLengths[node.getByteId()];
            maximumDataReadyTime = Math.max(maximumDataReadyTime, cost);
        }

        return maximumDataReadyTime;
    }

    /**
     * Returns a single node based on the fixed task ordering conditions
     *
     * @param state the current state
     * @param availableNodes the list of available nodes
     * @return a single node
     */
    protected NodeModel getFixedNodeOrder(StateModel state, List<NodeModel> availableNodes) {
        Set<NodeModel> availableSuccessors = new HashSet<>();
        Set<NodeModel> availablePredecessors = new HashSet<>();

        for (NodeModel node : availableNodes) {
            if (node.getInDegree() > 1 || node.getOutDegree() > 1) {
                return null;
            }

            if (node.getInDegree() > 0) {
                availablePredecessors.add(node.getPredecessor(0));
            }

            if (node.getOutDegree() > 0) {
                availableSuccessors.add(node.getSuccessor(0));
            }
        }

        if (availableSuccessors.size() > 1) {
            return null;
        }

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

        return getSortedNode(state, availableNodes);
    }

    /**
     * Get a single sorted node from the fixed task ordering. This is inefficient, however, due to time constraints
     * we were unable to provide a better approach.
     *
     * @param state the current state
     * @param availableNodes the list of available nodes
     * @return a single sorted node
     */
    protected NodeModel getSortedNode(StateModel state, List<NodeModel> availableNodes) {
        List<NodeModel> sortedNodes = getSortedNodes(state, availableNodes);

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

    /**
     * Get a list of sorted nodes based on fixed task ordering condition. Ties are broken by comparing the
     * node-successor edge cost.
     *
     * @param state the current state
     * @param availableNodes the list of available nodes
     * @return the list of sorted nodes
     */
    private List<NodeModel> getSortedNodes(StateModel state, List<NodeModel> availableNodes) {
        List<NodeModel> sortedNodes = new ArrayList<>(availableNodes);

        sortedNodes.sort((nodeA, nodeB) -> {
            int dataReadyTimeA = getDataReadyTime(state, nodeA);
            int dataReadyTimeB = getDataReadyTime(state, nodeB);

            if (dataReadyTimeA != dataReadyTimeB) {
                return Integer.compare(dataReadyTimeA, dataReadyTimeB);
            }

            int successorEdgeCostA = getSuccessorEdgeCost(nodeA);
            int successorEdgeCostB = getSuccessorEdgeCost(nodeB);

            return Integer.compare(successorEdgeCostB, successorEdgeCostA);
        });

        return sortedNodes;
    }

    /**
     * Returns the successor and parent edge cost.
     *
     * @param node the parent
     * @return the edge cost between successor and parent node
     */
    protected int getSuccessorEdgeCost(NodeModel node) {
        if (node.getOutDegree() == 0) {
            return 0;
        }

        return getEdge(node, node.getSuccessor(0)).weight();
    }

    /**
     * Returns the data ready time (or earliest start time) for the current state and node.
     *
     * @param state the current state
     * @param node the current node
     * @return the data ready time
     */
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

        return dataReadyTime + getEdge(predecessor, node).weight();
    }

    /**
     * Returns the minimum data ready time for the current state and node/task.
     *
     * @param state the current state
     * @param node the current node
     * @return the minimum data ready time
     */
    protected int getMinimumDataReadyTime(StateModel state, NodeModel node) {
        int minimumDataReadyTime = INFINITY_32;

        for (byte processor = 0; processor < processors; processor++) {
            int dataReadyTime = getEarliestStartTime(state, node, processor);
            minimumDataReadyTime = Math.min(minimumDataReadyTime, dataReadyTime);
        }

        return minimumDataReadyTime;
    }
}
