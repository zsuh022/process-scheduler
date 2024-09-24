package scheduler.schedulers;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;

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

    protected void setNodeByteIds() {
        for (int i = 0; i < this.nodes.length; i++) {
            this.nodes[i].setByteId((byte) i);
        }
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

        for (int i = 0; i < this.numberOfNodes; i++) {

            System.out.println(this.nodes[i].getId() + ", " + String.valueOf(bottomLevelPathLengths[i]));
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
}
