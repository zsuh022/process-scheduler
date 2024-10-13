package scheduler.schedulers;

import java.io.IOException;
import java.util.*;

import scheduler.models.*;
import scheduler.parsers.Arguments;
import scheduler.parsers.InputOutputParser;

import static scheduler.constants.Constants.INFINITY_32;

/**
 * This abstract class is used for scheduling algorithms. There are subclasses such as DFSScheduler and
 * SequentialScheduler implement specific algorithms.
 */
public abstract class Scheduler {
    protected GraphModel graph;

    protected MetricsModel metrics;

    protected NodeModel[] nodes;

    protected volatile StateModel bestState;
    protected volatile StateModel currentState;

    protected Set<StateModel> closedStates;

    protected byte processors;
    protected byte numberOfNodes;

    protected int criticalPathLength;

    protected int[] bottomLevelPathLengths;

    /**
     * Constructor for the Scheduler class. Initialises the graph, number of processors, number of
     * nodes, computes topological sorting and bottom level path lengths.
     *
     * @param graph represents the graph model.
     * @param processors represents the number of processors for scheduling.
     */
    protected Scheduler(GraphModel graph, byte processors) {
        this.graph = graph;

        this.processors = processors;
        this.numberOfNodes = (byte) graph.getNumberOfNodes();

        this.criticalPathLength = 0;

        this.metrics = new MetricsModel();

        this.nodes = getSortedNodes(graph.getNodes());

        this.closedStates = new HashSet<>();

        setNodeByteIds();

        this.bottomLevelPathLengths = getBottomLevelPathLengths();
    }

    public abstract void schedule();

    /**
     * Method assigns byte id to each node.
     */
    protected void setNodeByteIds() {
        for (int i = 0; i < this.nodes.length; i++) {
            this.nodes[i].setByteId((byte) i);
        }
    }

    /**
     * Method returns the edge between a source node and destination node.
     *
     * @param source represents the source node of the edge.
     * @param destination represents the destination node of the edge.
     * @return the edge between the source and destination nodes.
     */
    protected EdgeModel getEdge(NodeModel source, NodeModel destination) {
        String edgeId = source.getId().concat(destination.getId());

        return this.graph.getEdge(edgeId);
    }

    /**
     * Method returns the sorted order of tasks. Uses topological sorting of the nodes in the graph.
     *
     * @param nodes represents the graph's nodes in a map.
     * @return an array of sorted nodes.
     */
    protected NodeModel[] getSortedNodes(Map<String, NodeModel> nodes) {
        int numberOfNodes = nodes.size();

        int nodeIndex = 0;
        int sortedIndex = 0;

        int[] inDegrees = new int[numberOfNodes];
        NodeModel[] sortedNodes = new NodeModel[numberOfNodes];

        Deque<NodeModel> deque = new ArrayDeque<>();
        Map<NodeModel, Integer> nodeMap = new HashMap<>();

        for (NodeModel node : nodes.values()) {
            nodeMap.put(node, nodeIndex);
            inDegrees[nodeIndex] = node.getInDegree();

            if (inDegrees[nodeIndex] == 0) {
                deque.add(node);
            }

            ++nodeIndex;
        }

        while (!deque.isEmpty()) {
            int dequeSize = deque.size();

            for (int i = 0; i < dequeSize; i++) {
                NodeModel node = deque.poll();

                if (node == null) {
                    continue;
                }

                sortedNodes[sortedIndex++] = node;

                for (NodeModel successor : node.getSuccessors()) {
                    int index = nodeMap.get(successor);
                    --inDegrees[index];

                    if (inDegrees[index] == 0) {
                        deque.add(successor);
                    }
                }
            }
        }

        return sortedNodes;
    }

    /**
     * Method calculates the bottom level path lengths for each node. Start from the bottom and compute
     * way up. Bottom level path is the longest path from each node to the end of the graph.
     *
     * @return integer array of bottom level path lengths for each node.
     */
    protected int[] getBottomLevelPathLengths() {
        int[] bottomLevelPathLengths = new int[this.numberOfNodes];

        for (NodeModel node : this.nodes) {
            bottomLevelPathLengths[node.getByteId()] = (node.getOutDegree() == 0) ? node.getWeight() : -INFINITY_32;
        }

        for (int i = this.numberOfNodes - 1; i >= 0; i--) {
            NodeModel node = this.nodes[i];

            for (NodeModel predecessor : node.getPredecessors()) {
                setRelaxation(bottomLevelPathLengths, node, predecessor);
            }
        }

        findCriticalPathLength(bottomLevelPathLengths);

        return bottomLevelPathLengths;
    }

    private void findCriticalPathLength(int[] bottomLevelPathLengths) {
        for (int bottomLevelPathLength : bottomLevelPathLengths) {
            this.criticalPathLength = Math.max(this.criticalPathLength, bottomLevelPathLength);
        }
    }

    protected int getCriticalPathLength() {
        return this.criticalPathLength;
    }

    /**
     * Method updates distance array by relaxing edge between the source and destination nodes.
     *
     * @param distances represents the distances in an integer array.
     * @param source represents the source node.
     * @param destination represents the destination node.
     */
    private void setRelaxation(int[] distances, NodeModel source, NodeModel destination) {
        byte sourceId = source.getByteId();
        byte destinationId = destination.getByteId();

        int cost = distances[sourceId] + destination.getWeight();

        if (distances[destinationId] < cost) {
            distances[destinationId] = cost;
        }
    }

    /**
     * Method calculates the earliest start time for a node on a given processor.
     *
     * @param state represents the current state of the schedule.
     * @param node represents the node to schedule.
     * @param processor represents the processor to schedule the node on.
     * @return the earliest start time for a node on a given processor.
     */
    protected int getEarliestStartTime(StateModel state, NodeModel node, byte processor) {
        if (state.isEmpty()) {
            return 0;
        }

        // Get the earliest start time for the current processor
        int earliestStartTime = state.getFinishTime(processor);

        for (NodeModel predecessor : node.getPredecessors()) {
            int finishTime = state.getNodeStartTime(predecessor) + predecessor.getWeight();

            if (state.getNodeProcessor(predecessor) == processor) {
                earliestStartTime = Math.max(earliestStartTime, finishTime);
            } else {
                EdgeModel edge = getEdge(predecessor, node);
                earliestStartTime = Math.max(earliestStartTime, finishTime + edge.getWeight());
            }
        }

        return earliestStartTime;
    }

    protected int getEarliestStartTime(StateModel state, byte nodeId, int[] nodeStartTimes, byte processor, int startTime) {
        NodeModel node = this.nodes[nodeId];

        int earliestStartTime = startTime;

        for (NodeModel predecessor : node.getPredecessors()) {
            int finishTime = nodeStartTimes[predecessor.getByteId()] + predecessor.getWeight();

            if (state.getNodeProcessor(predecessor) == processor) {
                earliestStartTime = Math.max(earliestStartTime, finishTime);
            } else {
                EdgeModel edge = getEdge(predecessor, node);
                earliestStartTime = Math.max(earliestStartTime, finishTime + edge.getWeight());
            }
        }

        return earliestStartTime;
    }

    /**
     * Method returns a list of available nodes that can be scheduled. Available nodes all have their
     * predecessors processed and is not visited already.
     *
     * @param state represents the current state of the schedule.
     * @return a list of available nodes that can be scheduled.
     */
    protected List<NodeModel> getAvailableNodes(StateModel state) {
        List<NodeModel> availableNodes = new ArrayList<>();

        for (NodeModel node : this.nodes) {
            if (!state.isNodeScheduled(node) && arePredecessorsScheduled(state, node)) {
                availableNodes.add(node);
            }
        }

        return availableNodes;
    }

    protected List<NodeModel> getScheduledNodes(StateModel state) {
        List<NodeModel> scheduledNodes = new ArrayList<>();

        for (NodeModel node : this.nodes) {
            if (state.isNodeScheduled(node)) {
                scheduledNodes.add(node);
            }
        }

        return scheduledNodes;
    }

    /**
     * Method checks if all predecessors have been scheduled.
     *
     * @param state represents the current state of the schedule.
     * @param node represents the node to check.
     * @return boolean (true) for if all predecessors have been scheduled (otherwise false).
     */
    protected boolean arePredecessorsScheduled(StateModel state, NodeModel node) {
        for (NodeModel predecessor : node.getPredecessors()) {
            if (!state.isNodeScheduled(predecessor)) {
                return false;
            }
        }

        return true;
    }

    public StateModel getCurrentState() {
        return this.currentState;
    }

    protected void setCurrentState(StateModel state) {
        this.currentState = state;
    }

    public MetricsModel getMetrics() {
        return this.metrics;
    }

    public NodeModel[] getNodes() {
        return this.nodes;
    }

    public StateModel getBestState() {
        return this.bestState;
    }

    public void saveBestState(Arguments arguments) throws IOException {
        this.graph.setNodesAndEdgesForState(this.bestState);

        InputOutputParser.outputDOTFile(this.graph, arguments.getOutputDOTFilePath());

        arguments.displayOutputDOTFilePath();
    }
}
