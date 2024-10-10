package scheduler.generator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import scheduler.models.GraphModel;
import scheduler.parsers.InputOutputParser;
import scheduler.utilities.Utility;

import java.io.IOException;

import static scheduler.constants.Constants.*;

public class GraphGenerator {
    private static class GraphInformation {
        protected static int numberOfNodes = 0;
        protected static int numberOfEdges = 0;

        protected static byte numberOfProcessors = 0;
    }

    public static GraphModel getRandomGraph() {
        GraphInformation.numberOfNodes = getRandomNumberOfNodes();
        GraphInformation.numberOfEdges = getRandomNumberOfEdges();

        return new GraphModel(generateRandomGraph());
    }

    private static boolean isCyclic(boolean[][] adjacencyMatrix) {
        boolean[] stack = new boolean[GraphInformation.numberOfNodes];
        boolean[] visited = new boolean[GraphInformation.numberOfNodes];

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

    private static Graph generateRandomGraph() {
        Graph graph = new SingleGraph("graph");

        boolean[][] adjacencyMatrix = new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];

        int[][] edgePermutations = getEdgePermutations();

        boolean[] isSourceNode = getSourceNodePermutations();

        int edgeIndex = 0;
        int edgeCount = 0;

        // Naive DAG generator- improved with 2d array shuffling
        while (edgeIndex < edgePermutations.length && edgeCount < GraphInformation.numberOfEdges) {
            int sourceId = edgePermutations[edgeIndex][0];
            int destinationId = edgePermutations[edgeIndex][1];

            if (isSourceNode[destinationId]) {
                ++edgeIndex;

                continue;
            }

            adjacencyMatrix[sourceId][destinationId] = true;

            if (isCyclic(adjacencyMatrix)) {
                adjacencyMatrix[sourceId][destinationId] = false;
            } else {
                ++edgeCount;
            }

            ++edgeIndex;
        }

        createGraphFromAdjacencyMatrix(graph, adjacencyMatrix);

        return graph;
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

        GraphInformation.numberOfEdges = graph.getEdgeCount();
    }

    // Could refactor into utility class later - formatter class?
    public static void displayGraphInformation() {
        System.out.println("\nGenerated Graph Information:");
        System.out.printf("  %-25s %d%n", "Number of nodes:", GraphInformation.numberOfNodes);
        System.out.printf("  %-25s %d%n", "Number of edges:", GraphInformation.numberOfEdges);
        System.out.printf("  %-25s %d%n", "Number of processors:", GraphInformation.numberOfProcessors);
    }

    private static int getRandomNumberOfNodes() {
        return Utility.getRandomInteger(NUMBER_OF_NODES_LOWER_BOUND, NUMBER_OF_NODES_UPPER_BOUND);
    }

    private static int getRandomNumberOfEdges() {
        double randomPercentage = Utility.getRandomPercentage(EDGE_RATIO_LOWER_BOUND, EDGE_RATIO_UPPER_BOUND);

        return (int) (randomPercentage * getMaximumNumberOfDAGEdges());
    }

    private static int getRandomNumberOfSourceNodes() {
        double randomPercentage = Utility.getRandomPercentage(SOURCE_NODE_RATIO_LOWER_BOUND, SOURCE_NODE_RATIO_UPPER_BOUND);

        return Math.max(1, (int) (randomPercentage * GraphInformation.numberOfNodes));
    }

    private static int getMaximumNumberOfDAGEdges() {
        return getMaximumNumberOfNonDAGEdges() / 2;
    }

    private static int getMaximumNumberOfNonDAGEdges() {
        return GraphInformation.numberOfNodes * (GraphInformation.numberOfNodes - 1);
    }

    private static double getRandomWeight() {
        return Utility.getRandomInteger(WEIGHT_LOWER_BOUND, WEIGHT_UPPER_BOUND);
    }

    public static void setNumberOfProcessors(byte numberOfProcessors) {
        GraphInformation.numberOfProcessors = numberOfProcessors;
    }

    private static int[][] getEdgePermutations() {
        int edgeIndex = 0;
        int maximumNumberOfEdges = getMaximumNumberOfNonDAGEdges();

        int[][] edgePermutations = new int[maximumNumberOfEdges][2];

        for (int sourceId = 0; sourceId < GraphInformation.numberOfNodes; sourceId++) {
            for (int destinationId = 0; destinationId < GraphInformation.numberOfNodes; destinationId++) {
                if (sourceId != destinationId) {
                    edgePermutations[edgeIndex][0] = sourceId;
                    edgePermutations[edgeIndex][1] = destinationId;

                    ++edgeIndex;
                }
            }
        }

        Utility.shuffle2DArray(edgePermutations, maximumNumberOfEdges);

        return edgePermutations;
    }

    private static boolean[] getSourceNodePermutations() {
        boolean[] isSourceNode = new boolean[GraphInformation.numberOfNodes];

        int sourceNodeIndex = 0;
        int numberOfSourceNodes = 0;
        int maximumNumberOfSourceNodes = getRandomNumberOfSourceNodes();

        while (sourceNodeIndex < GraphInformation.numberOfNodes && numberOfSourceNodes < maximumNumberOfSourceNodes) {
            isSourceNode[sourceNodeIndex] = Utility.getRandomBoolean();

            if (isSourceNode[sourceNodeIndex]) {
                ++numberOfSourceNodes;
            }

            ++sourceNodeIndex;
        }

        return isSourceNode;
    }

    public static void createAndSaveRandomGraphs(int numberOfRandomGraphs) throws IOException {
        for (int graphIndex = 0; graphIndex < numberOfRandomGraphs; graphIndex++) {
            String filename = "Random_%d_Graph.dot".formatted(graphIndex);

            InputOutputParser.writeDOTFile(generateRandomGraph(), filename);
        }
    }
}
