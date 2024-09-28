package scheduler.schedulers;

import java.util.*;

import scheduler.models.EdgeModel;
import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;

import static scheduler.constants.Constants.INF_32;

public abstract class Scheduler {
    protected GraphModel graph;

    protected int processors;
    protected int numberOfNodes;

    protected int[] bottomLevelPathLengths;
    protected NodeModel[] nodes;

    protected Scheduler(GraphModel graph, int processors) {
        this.graph = graph;

        this.processors = processors;
        this.numberOfNodes = graph.getNumberOfNodes();

        this.nodes = getSortedNodes(graph.getNodes());
        setNodeByteIds();
        this.bottomLevelPathLengths = getBottomLevelPathLengths();
    }

    public StateModel getAStarSchedule() {
        return null;
    }

    public void getDFSSchedule(StateModel state, Set<StateModel> closedStates) {
    }

    protected void setNodeByteIds() {
        for (int i = 0; i < this.nodes.length; i++) {
            this.nodes[i].setByteId((byte) i);
        }
    }

    protected EdgeModel getEdge(NodeModel source, NodeModel destination) {
        String edgeId = source.getId().concat(destination.getId());

        return this.graph.getEdge(edgeId);
    }

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

            nodeIndex++;
        }

        while (!deque.isEmpty()) {
            int dequeSize = deque.size();

            for (int i = 0; i < dequeSize; i++) {
                NodeModel node = deque.poll();
                sortedNodes[sortedIndex++] = node;

                for (NodeModel successor : node.getSuccessors()) {
                    int index = nodeMap.get(successor);
                    inDegrees[index]--;

                    if (inDegrees[index] == 0) {
                        deque.add(successor);
                    }
                }
            }
        }

        return sortedNodes;
    }

    // Start from the bottom and compute way up
    protected int[] getBottomLevelPathLengths() {
        int[] bottomLevelPathLengths = new int[this.numberOfNodes];

        for (NodeModel node : this.nodes) {
            bottomLevelPathLengths[node.getByteId()] = node.getSuccessors().size() == 0 ? node.getWeight() : -INF_32;
        }

        for (int i = this.numberOfNodes - 1; i >= 0; i--) {
            NodeModel node = this.nodes[i];

            for (NodeModel predecessor : node.getPredecessors()) {
                setRelaxation(bottomLevelPathLengths, node, predecessor);
            }
        }

        return bottomLevelPathLengths;
    }

    private void setRelaxation(int[] distances, NodeModel source, NodeModel destination) {
        byte sourceId = source.getByteId();
        byte destinationId = destination.getByteId();

        int cost = distances[sourceId] + destination.getWeight();

        if (distances[destinationId] < cost) {
            distances[destinationId] = cost;
        }
    }

    private void setRelaxation(int[] distances, byte sourceId, byte destinationId, int nodeWeight, int edgeWeight) {
        int cost = distances[sourceId] + nodeWeight + edgeWeight;

        if (distances[destinationId] > cost) {
            distances[destinationId] = cost;
        }
    }

    protected int getEarliestStartTime(StateModel state, NodeModel node, int processor) {
        if (state.isEmptyState()) {
            return 0;
        }

        // Get the earliest start time for the current processor
        int earliestStartTime = state.getFinishTime(processor);

        for (NodeModel predecessor : node.getPredecessors()) {
            int finishTime = state.getNodeStartTime(node) + predecessor.getWeight();

            // If we are scheduling on the same processor, we can ignore the communication
            // time, otherwise, we include the communication time, which is the edge weight
            // between predecessor and node
            if (state.getNodeProcessor(predecessor) == processor) {
                earliestStartTime = Math.max(earliestStartTime, finishTime);
            } else {
                EdgeModel edge = getEdge(predecessor, node);
                earliestStartTime = Math.max(earliestStartTime, finishTime + edge.getWeight());
            }
        }

        return earliestStartTime;
    }

    // available nodes all have their predecessors processed and is not visited
    // already
    protected List<NodeModel> getAvailableNodes(StateModel state) {
        List<NodeModel> availableNodes = new ArrayList<>();

        for (NodeModel node : this.graph.getNodes().values()) {
            if (!state.isNodeScheduled(node) && arePredecessorsScheduled(state, node)) {
                availableNodes.add(node);
            }
        }

        return availableNodes;
    }

    // Check for the current state, the current task, if its predecessors were
    // scheduled already
    protected boolean arePredecessorsScheduled(StateModel state, NodeModel node) {
        for (NodeModel predecessor : node.getPredecessors()) {
            if (!state.isNodeScheduled(predecessor)) {
                return false;
            }
        }

        return true;
    }
}
