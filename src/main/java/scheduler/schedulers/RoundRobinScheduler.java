package scheduler.schedulers;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.EdgeModel;

import java.util.*;

public class RoundRobinScheduler {
    private GraphModel graphModel;
    private int numProcessors;

    public RoundRobinScheduler(GraphModel graphModel, int numProcessors) {
        this.graphModel = graphModel;
        this.numProcessors = numProcessors;
    }

    public void schedule() {
        // Perform topological sort to respect dependencies
        List<NodeModel> sortedNodes = topologicalSort();

        // Initialize processor availability times
        int[] processorAvailability = new int[numProcessors];

        // Assign tasks to processors in round-robin fashion
        int processorIndex = 0;
        for (NodeModel node : sortedNodes) {
            int processor = processorIndex + 1; // Processors are 1-indexed
            node.setProcessor(processor);

            // Calculate earliest start time based on dependencies
            int earliestStart = getEarliestStartTime(node);

            // Start time is the max of processor availability and earliest start
            int startTime = Math.max(processorAvailability[processorIndex], earliestStart);
            node.setStartTime(startTime);

            // Update processor availability
            processorAvailability[processorIndex] = startTime + node.getWeight();

            // Move to next processor
            processorIndex = (processorIndex + 1) % numProcessors;
        }
    }

    private int getEarliestStartTime(NodeModel node) {
        int earliest = 0;
        for (NodeModel predecessor : node.getPredecessors()) {
            int communicationTime = 0;
            if (node.getProcessor() != predecessor.getProcessor()) {
                // Get communication weight
                EdgeModel edge = findEdge(predecessor, node);
                if (edge != null) {
                    communicationTime = edge.getWeight();
                }
            }
            int finishTime = predecessor.getStartTime() + predecessor.getWeight() + communicationTime;
            earliest = Math.max(earliest, finishTime);
        }
        return earliest;
    }

    private EdgeModel findEdge(NodeModel source, NodeModel destination) {
        // Retrieve the edge between source and destination
        for (EdgeModel edge : graphModel.getEdges().values()) {
            if (edge.getSource().equals(source) && edge.getDestination().equals(destination)) {
                return edge;
            }
        }
        return null; // Edge not found
    }

    private List<NodeModel> topologicalSort() {
        List<NodeModel> sortedList = new ArrayList<>();
        Set<NodeModel> visited = new HashSet<>();

        for (NodeModel node : graphModel.getNodes().values()) {
            if (!visited.contains(node)) {
                dfsVisit(node, visited, sortedList);
            }
        }
        Collections.reverse(sortedList); // Reverse to get correct order
        return sortedList;
    }

    private void dfsVisit(NodeModel node, Set<NodeModel> visited, List<NodeModel> sortedList) {
        visited.add(node);
        for (NodeModel successor : node.getSuccessors()) {
            if (!visited.contains(successor)) {
                dfsVisit(successor, visited, sortedList);
            }
        }
        sortedList.add(node);
    }
}
