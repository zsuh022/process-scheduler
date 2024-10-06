package scheduler.generator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import scheduler.models.GraphModel;

import java.util.*;

import static scheduler.constants.Constants.*;

public class GraphGenerator {
    private static final Random random = new Random();

    private static class GraphInformation {
        protected static int numberOfNodes = 0;
        protected static int numberOfEdges = 0;

        protected static byte numberOfProcessors = 0;
    }

    public static GraphModel getRandomGraph() {
        Graph graph = new SingleGraph("graph");

        graph.setStrict(false);
        graph.setAutoCreate(true);

        GraphInformation.numberOfNodes = getNumberOfRandomNodes();
        GraphInformation.numberOfEdges = getNumberOfRandomEdges();

        generateRandomGraph(graph);

        return new GraphModel(graph);
    }

    private static boolean isCyclic(boolean[][] adjacencyMatrix) {
        boolean[] visited = new boolean[GraphInformation.numberOfNodes];
        boolean[] stack = new boolean[GraphInformation.numberOfNodes];

        for (int sourceId = 0; sourceId < GraphInformation.numberOfNodes; sourceId++) {
            if (isValidNode(sourceId, adjacencyMatrix, visited) && isCyclic(sourceId, adjacencyMatrix, visited, stack)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isCyclic(int sourceId, boolean[][] adjacencyMatrix, boolean[] visited, boolean[] stack) {
        if (!visited[sourceId]) {
            visited[sourceId] = true;
            stack[sourceId] = true;

            for (int destinationId = 0; destinationId < GraphInformation.numberOfNodes; destinationId++) {
                if (isValidEdge(sourceId, destinationId, adjacencyMatrix, visited) && isCyclic(destinationId, adjacencyMatrix, visited, stack)) {
                    return true;
                } else if (stack[destinationId]) {
                    return true;
                }
            }
        }

        stack[sourceId] = false;

        return false;
    }

    private static boolean isValidEdge(int sourceId, int destinationId, boolean[][] adjacencyMatrix, boolean[] visited) {
        return (!visited[destinationId] && adjacencyMatrix[sourceId][destinationId]);
    }

    private static boolean isValidNode(int nodeId, boolean[][] adjacencyMatrix, boolean[] visited) {
        return (!visited[nodeId] && adjacencyMatrix[nodeId][nodeId]);
    }

    private static void addEdge(Graph graph, String sourceId, String destinationId) {
        Edge edge = graph.addEdge(sourceId.concat(destinationId), sourceId, destinationId);
        edge.setAttribute("Weight", getRandomWeight());

        addNodeWeight(edge.getSourceNode());
        addNodeWeight(edge.getTargetNode());
    }

    private static void addNodeWeight(Node node) {
        if (!node.hasAttribute("Weight")) {
            node.setAttribute("Weight", getRandomWeight());
        }
    }

    private static void generateRandomGraph(Graph graph) {
        int edgeId = 0;

        boolean[][] adjacencyMatrix = new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];

        while (edgeId < GraphInformation.numberOfEdges) {
            int sourceId = getRandomInteger(0, GraphInformation.numberOfNodes - 1);
            int destinationId = getRandomInteger(0, GraphInformation.numberOfNodes - 1);

//            int sourceId = 0;
//            int destinationId = 1;

            if (sourceId == destinationId || adjacencyMatrix[sourceId][destinationId]) {
                continue;
            }

            adjacencyMatrix[sourceId][destinationId] = true;

            if (isCyclic(adjacencyMatrix)) {
                adjacencyMatrix[sourceId][destinationId] = false;
            } else {
                ++edgeId;
            }
        }

        createGraphFromAdjacencyMatrix(graph, adjacencyMatrix);
    }

    private static void createGraphFromAdjacencyMatrix(Graph graph, boolean[][] adjacencyMatrix) {
        for (int sourceId = 0; sourceId < GraphInformation.numberOfNodes; sourceId++) {
            for (int destinationId = 0; destinationId < GraphInformation.numberOfNodes; destinationId++) {
                if (adjacencyMatrix[sourceId][destinationId]) {
                    addEdge(graph, String.valueOf(sourceId), String.valueOf(destinationId));
                }
            }
        }
    }

    // Could refactor into utility class later - formatter class?
    public static void displayGraphInformation() {
        System.out.println("\nGenerated Graph Information:");
        System.out.printf("  Number of nodes: %d%n", GraphInformation.numberOfNodes);
        System.out.printf("  Number of edges: %d%n", GraphInformation.numberOfEdges);
        System.out.printf("  Number of processors: %d%n", GraphInformation.numberOfProcessors);
    }

    private static int getNumberOfRandomNodes() {
        return getRandomInteger(NUMBER_OF_NODES_LOWER_BOUND, NUMBER_OF_NODES_UPPER_BOUND);
    }

    private static int getNumberOfRandomEdges() {
        return GraphInformation.numberOfNodes / 2;
    }

    private static double getRandomWeight() {
        return getRandomInteger(WEIGHT_LOWER_BOUND, WEIGHT_UPPER_BOUND);
    }

    private static int getRandomInteger(int minimum, int maximum) {
        return random.nextInt((maximum - minimum) + 1) + minimum;
    }

    public static void setNumberOfProcessors(byte numberOfProcessors) {
        GraphInformation.numberOfProcessors = numberOfProcessors;
    }
}
