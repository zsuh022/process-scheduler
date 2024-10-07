package scheduler.generator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import scheduler.models.GraphModel;
import scheduler.utilities.NumberUtility;

import static scheduler.constants.Constants.*;

public class GraphGenerator {
    private static class GraphInformation {
        protected static int numberOfNodes = 0;
        protected static int numberOfEdges = 0;

        protected static byte numberOfProcessors = 0;
    }

    public static GraphModel getRandomGraph() {
        Graph graph = new SingleGraph("graph");

        GraphInformation.numberOfNodes = getRandomNumberOfNodes();
        GraphInformation.numberOfEdges = getRandomNumberOfEdges();

        generateRandomGraph(graph);

        return new GraphModel(graph);
    }

    private static boolean isCyclic(boolean[][] adjacencyMatrix) {
        boolean[] visited = new boolean[GraphInformation.numberOfNodes];
        boolean[] stack = new boolean[GraphInformation.numberOfNodes];

        for (int sourceId = 0; sourceId < GraphInformation.numberOfNodes; sourceId++) {
            if (!visited[sourceId] && isCyclic(sourceId, adjacencyMatrix, visited, stack)) {
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
                if (adjacencyMatrix[sourceId][destinationId]) {
                    if (!visited[destinationId]) {
                        if (isCyclic(destinationId, adjacencyMatrix, visited, stack)) {
                            return true;
                        }
                    } else if (stack[destinationId]) {
                        return true;
                    }
                }
            }
        }

        stack[sourceId] = false;

        return false;
    }

    private static void addNode(Graph graph, String nodeId) {
        if (graph.getNode(nodeId) == null) {
            Node node = graph.addNode(nodeId);
            node.setAttribute("Weight", getRandomWeight());
        }
    }

    private static void addEdge(Graph graph, String sourceId, String destinationId) {
        String edgeId = sourceId.concat(destinationId);

        if (graph.getEdge(edgeId) == null) {
            Edge edge = graph.addEdge(sourceId.concat(destinationId), sourceId, destinationId, true);
            edge.setAttribute("Weight", getRandomWeight());
        }
    }

    private static void generateRandomGraph(Graph graph) {
        boolean[][] adjacencyMatrix = new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];

        int edgeCount = 0;

        // Naive DAG generator
        while (edgeCount < GraphInformation.numberOfEdges) {
            int sourceId = NumberUtility.getRandomInteger(0, GraphInformation.numberOfNodes - 1);
            int destinationId = NumberUtility.getRandomInteger(0, GraphInformation.numberOfNodes - 1);

            if (sourceId == destinationId || adjacencyMatrix[sourceId][destinationId]) {
                continue;
            }

            adjacencyMatrix[sourceId][destinationId] = true;

            if (isCyclic(adjacencyMatrix)) {
                adjacencyMatrix[sourceId][destinationId] = false;
            } else {
                ++edgeCount;
            }
        }

        createGraphFromAdjacencyMatrix(graph, adjacencyMatrix);
    }

    private static void createGraphFromAdjacencyMatrix(Graph graph, boolean[][] adjacencyMatrix) {
        for (int sourceIndex = 0; sourceIndex < GraphInformation.numberOfNodes; sourceIndex++) {
            for (int destinationIndex = 0; destinationIndex < GraphInformation.numberOfNodes; destinationIndex++) {
                if (adjacencyMatrix[sourceIndex][destinationIndex]) {
                    String sourceId = String.valueOf(sourceIndex);
                    String destinationId = String.valueOf(destinationIndex);

                    addNode(graph, sourceId);
                    addNode(graph, destinationId);

                    addEdge(graph, sourceId, destinationId);
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

    private static int getRandomNumberOfNodes() {
        return NumberUtility.getRandomInteger(NUMBER_OF_NODES_LOWER_BOUND, NUMBER_OF_NODES_UPPER_BOUND);
    }

    private static int getRandomNumberOfEdges() {
        int maxNumberOfEdges = (GraphInformation.numberOfNodes * (GraphInformation.numberOfNodes - 1)) / 2;
        double randomPercentage = NumberUtility.getRandomPercentage(EDGE_RATIO_LOWER_BOUND, EDGE_RATIO_UPPER_BOUND);

        return (int) (randomPercentage * maxNumberOfEdges);
    }

    private static double getRandomWeight() {
        return NumberUtility.getRandomInteger(WEIGHT_LOWER_BOUND, WEIGHT_UPPER_BOUND);
    }

    public static void setNumberOfProcessors(byte numberOfProcessors) {
        GraphInformation.numberOfProcessors = numberOfProcessors;
    }
}
