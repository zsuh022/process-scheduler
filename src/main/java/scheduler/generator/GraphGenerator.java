package scheduler.generator;

import static scheduler.constants.Constants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import scheduler.enums.GraphType;
import scheduler.models.GraphModel;
import scheduler.parsers.InputOutputParser;
import scheduler.utilities.Utility;

/**
 * The {@code GraphGenerator} class is responsible for generating different types of random graphs,
 * including stencil graphs, fork/join graphs, and chain graphs. It provides methods for creating
 * graphs with random layers, nodes, and edges, and for saving these graphs as DOT files.
 */
public class GraphGenerator {

  /**
   * This class holds metadata about the generated graph, such as the number of nodes, edges, and
   * processors.
   */
  private static class GraphInformation {
    protected static int numberOfNodes = 0;
    protected static int numberOfEdges = 0;
    protected static byte numberOfProcessors = 0;
  }

  /**
   * Generates a random directed acyclic graph (DAG) using random numbers of nodes and edges.
   *
   * @return a {@link GraphModel} representing the randomly generated graph.
   */
  public static GraphModel getRandomGraph() {
    initialiseGraphInformation();
    return new GraphModel(generateRandomGraph());
  }

  /**
   * Initializes the number of nodes and edges for the graph by generating random values within
   * defined upper and lower bounds.
   */
  private static void initialiseGraphInformation() {
    GraphInformation.numberOfNodes = getRandomNumberOfNodes();
    GraphInformation.numberOfEdges = getRandomNumberOfEdges();
  }

  /**
   * Checks if the generated graph has cycles, given an adjacency matrix.
   *
   * @param adjacencyMatrix the adjacency matrix representing the graph.
   * @return {@code true} if the graph contains a cycle; {@code false} otherwise.
   */
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

  /**
   * A recursive helper method for detecting cycles in the graph.
   *
   * @param sourceId the current node being visited.
   * @param adjacencyMatrix the adjacency matrix representing the graph.
   * @param visited an array to track visited nodes.
   * @param stack an array to track nodes in the current recursion stack.
   * @return {@code true} if a cycle is detected; {@code false} otherwise.
   */
  private static boolean isCyclic(
      int sourceId, boolean[][] adjacencyMatrix, boolean[] visited, boolean[] stack) {
    if (!visited[sourceId]) {
      stack[sourceId] = true;
      visited[sourceId] = true;

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

  /**
   * Adds a node to the graph with a random weight.
   *
   * @param graph the graph to which the node will be added.
   * @param nodeId the unique identifier of the node.
   */
  private static void addNode(Graph graph, String nodeId) {
    if (graph.getNode(nodeId) == null) {
      Node node = graph.addNode(nodeId);
      node.setAttribute("Weight", getRandomWeight());
    }
  }

  /**
   * Adds an edge between two nodes in the graph with a random weight.
   *
   * @param graph the graph to which the edge will be added.
   * @param sourceId the unique identifier of the source node.
   * @param destinationId the unique identifier of the destination node.
   */
  private static void addEdge(Graph graph, String sourceId, String destinationId) {
    String edgeId = sourceId.concat(destinationId);

    if (graph.getEdge(edgeId) == null) {
      Edge edge = graph.addEdge(edgeId, sourceId, destinationId, true);
      edge.setAttribute("Weight", getRandomWeight());
    }
  }

  /**
   * Generates a random graph using an adjacency matrix and edge permutations.
   *
   * @return a {@link Graph} object representing the randomly generated graph.
   */
  private static Graph generateRandomGraph() {
    Graph graph = new SingleGraph("graph");

    boolean[][] adjacencyMatrix =
        new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];
    int[][] edgePermutations = getEdgePermutations();
    boolean[] isSourceNode = getSourceNodePermutations();

    int edgeIndex = 0;
    int edgeCount = 0;

    // Naive DAG generator, improved with 2D array shuffling
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

  /**
   * Converts an adjacency matrix into a graph by adding nodes and edges.
   *
   * @param graph the graph object where nodes and edges will be added.
   * @param adjacencyMatrix the adjacency matrix representing the graph.
   */
  private static void createGraphFromAdjacencyMatrix(Graph graph, boolean[][] adjacencyMatrix) {
    for (int sourceIndex = 0; sourceIndex < GraphInformation.numberOfNodes; sourceIndex++) {
      for (int destinationIndex = 0;
          destinationIndex < GraphInformation.numberOfNodes;
          destinationIndex++) {
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

  /**
   * Generates a stencil graph with a random number of layers and nodes per layer, and then outputs
   * the graph to a DOT file located in the resources folder.
   *
   * @throws IOException if an I/O error occurs while writing the graph to a DOT file.
   */
  public static void createStencilGraphWithRandomLayersAndNodes() throws IOException {
    Graph graph = new SingleGraph("stencil-graph");

    int numLayers = getRandomNumberOfLayers();
    int numNodesPerLayer = getRandomNumberOfNodes();
    int totalNodes = numLayers * numNodesPerLayer;
    boolean[][] adjacencyMatrix = new boolean[totalNodes][totalNodes];

    int[][] layerNodes = new int[numLayers][numNodesPerLayer];
    for (int layer = 0; layer < numLayers; layer++) {
      for (int nodeIndex = 0; nodeIndex < numNodesPerLayer; nodeIndex++) {
        int actualNodeIndex = (layer * numNodesPerLayer) + nodeIndex;
        layerNodes[layer][nodeIndex] = actualNodeIndex;
        addNode(graph, Integer.toString(actualNodeIndex));
      }
    }

    // Connect nodes between layers, ensuring each node connects to at least 2 nodes in the next
    // layer
    for (int layer = 0; layer < numLayers - 1; layer++) {
      for (int nodeIndex = 0; nodeIndex < numNodesPerLayer; nodeIndex++) {
        int currentNode = layerNodes[layer][nodeIndex];
        List<Integer> nextLayerNodes = new ArrayList<>();
        for (int i = 0; i < numNodesPerLayer; i++) {
          nextLayerNodes.add(layerNodes[layer + 1][i]);
        }
        Collections.shuffle(nextLayerNodes);

        for (int i = 0; i < 2; i++) {
          int nextLayerNode = nextLayerNodes.get(i);
          addEdge(graph, Integer.toString(currentNode), Integer.toString(nextLayerNode));
          adjacencyMatrix[currentNode][nextLayerNode] = true;
        }
      }
    }

    InputOutputParser.writeDOTFile(graph, getFilename(GraphType.STENCIL));
  }

  /**
   * Displays information about the generated graph, including the number of nodes, edges, and
   * processors.
   */
  public static void displayGraphInformation() {
    System.out.println("\nGenerated Graph Information:");
    System.out.printf("  %-25s %d%n", "Number of nodes:", GraphInformation.numberOfNodes);
    System.out.printf("  %-25s %d%n", "Number of edges:", GraphInformation.numberOfEdges);
    System.out.printf("  %-25s %d%n", "Number of processors:", GraphInformation.numberOfProcessors);
  }

  /**
   * Generates a random number of nodes within a predefined lower and upper bound.
   *
   * @return a random integer representing the number of nodes.
   */
  private static int getRandomNumberOfNodes() {
    return Utility.getRandomInteger(NUMBER_OF_NODES_LOWER_BOUND, NUMBER_OF_NODES_UPPER_BOUND);
  }

  /**
   * Generates a random number of edges based on a random percentage of the maximum possible edges.
   *
   * @return a random integer representing the number of edges.
   */
  private static int getRandomNumberOfEdges() {
    double randomPercentage =
        Utility.getRandomPercentage(EDGE_RATIO_LOWER_BOUND, EDGE_RATIO_UPPER_BOUND);
    return (int) (randomPercentage * getMaximumNumberOfDAGEdges());
  }

  /**
   * Generates a random number of source nodes, which is calculated as a percentage of the total
   * number of nodes.
   *
   * @return a random integer representing the number of source nodes.
   */
  private static int getRandomNumberOfSourceNodes() {
    double randomPercentage =
        Utility.getRandomPercentage(SOURCE_NODE_RATIO_LOWER_BOUND, SOURCE_NODE_RATIO_UPPER_BOUND);
    return Math.max(1, (int) (randomPercentage * GraphInformation.numberOfNodes));
  }

  /**
   * Generates a random number of layers based on a random percentage of the total number of nodes.
   *
   * @return a random integer representing the number of layers.
   */
  private static int getRandomNumberOfLayers() {
    double randomPercentage =
        Utility.getRandomPercentage(LAYER_RATIO_LOWER_BOUND, LAYER_RATIO_UPPER_BOUND);
    return Math.max(2, (int) (randomPercentage * GraphInformation.numberOfNodes));
  }

  /**
   * Calculates the maximum number of directed acyclic graph (DAG) edges, which is half of the
   * maximum number of non-DAG edges.
   *
   * @return the maximum number of DAG edges.
   */
  private static int getMaximumNumberOfDAGEdges() {
    return getMaximumNumberOfNonDAGEdges() / 2;
  }

  /**
   * Calculates the maximum number of non-DAG edges, which is determined by the total number of
   * nodes.
   *
   * @return the maximum number of non-DAG edges.
   */
  private static int getMaximumNumberOfNonDAGEdges() {
    return GraphInformation.numberOfNodes * (GraphInformation.numberOfNodes - 1);
  }

  /**
   * Generates a random weight value for nodes and edges, based on predefined upper and lower
   * bounds.
   *
   * @return a random double representing the weight.
   */
  private static double getRandomWeight() {
    return Utility.getRandomInteger(WEIGHT_LOWER_BOUND, WEIGHT_UPPER_BOUND);
  }

  /**
   * Sets the number of processors for the graph.
   *
   * @param numberOfProcessors the number of processors.
   */
  public static void setNumberOfProcessors(byte numberOfProcessors) {
    GraphInformation.numberOfProcessors = numberOfProcessors;
  }

  /**
   * Generates all possible edge permutations for the graph, then shuffles them.
   *
   * @return a 2D array of edge permutations.
   */
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

  /**
   * Generates random permutations of source nodes, ensuring a certain number of nodes are marked as
   * source nodes.
   *
   * @return a boolean array where {@code true} represents a source node.
   */
  private static boolean[] getSourceNodePermutations() {
    boolean[] isSourceNode = new boolean[GraphInformation.numberOfNodes];

    int sourceNodeIndex = 0;
    int numberOfSourceNodes = 0;
    int maximumNumberOfSourceNodes = getRandomNumberOfSourceNodes();

    while (sourceNodeIndex < GraphInformation.numberOfNodes
        && numberOfSourceNodes < maximumNumberOfSourceNodes) {
      isSourceNode[sourceNodeIndex] = Utility.getRandomBoolean();

      if (isSourceNode[sourceNodeIndex]) {
        ++numberOfSourceNodes;
      }

      ++sourceNodeIndex;
    }

    return isSourceNode;
  }

  /**
   * Creates a specified number of random graphs and writes them to DOT files.
   *
   * @param numberOfRandomGraphs the number of random graphs to create.
   * @throws IOException if an I/O error occurs while writing the graphs to DOT files.
   */
  public static void createRandomGraphs(int numberOfRandomGraphs) throws IOException {
    for (int graphIndex = 0; graphIndex < numberOfRandomGraphs; graphIndex++) {
      String filename = "Random_%d_Graph.dot".formatted(graphIndex);
      InputOutputParser.writeDOTFile(generateRandomGraph(), filename);
    }
  }

  /**
   * Creates a fork-and-join graph and writes it to a DOT file.
   *
   * @throws IOException if an I/O error occurs while writing the graph to a DOT file.
   */
  public static void createForkAndJoinGraph() throws IOException {
    Graph graph = new SingleGraph("graph");
    initialiseGraphInformation();

    boolean[][] adjacencyMatrix =
        new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];

    for (int nodeId = 1; nodeId < GraphInformation.numberOfNodes - 1; nodeId++) {
      adjacencyMatrix[0][nodeId] = true;
      adjacencyMatrix[nodeId][GraphInformation.numberOfNodes - 1] = true;
    }

    createGraphFromAdjacencyMatrix(graph, adjacencyMatrix);
    InputOutputParser.writeDOTFile(graph, getFilename(GraphType.FORK_AND_JOIN));
  }

  /**
   * Creates a fork graph and writes it to a DOT file.
   *
   * @throws IOException if an I/O error occurs while writing the graph to a DOT file.
   */
  public static void createForkGraph() throws IOException {
    Graph graph = new SingleGraph("graph");
    initialiseGraphInformation();

    boolean[][] adjacencyMatrix =
        new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];

    for (int nodeId = 1; nodeId < GraphInformation.numberOfNodes; nodeId++) {
      adjacencyMatrix[0][nodeId] = true;
    }

    createGraphFromAdjacencyMatrix(graph, adjacencyMatrix);
    InputOutputParser.writeDOTFile(graph, getFilename(GraphType.FORK));
  }

  /**
   * Creates a join graph and writes it to a DOT file.
   *
   * @throws IOException if an I/O error occurs while writing the graph to a DOT file.
   */
  public static void createJoinGraph() throws IOException {
    Graph graph = new SingleGraph("graph");
    initialiseGraphInformation();

    boolean[][] adjacencyMatrix =
        new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];

    for (int nodeId = 0; nodeId < GraphInformation.numberOfNodes - 1; nodeId++) {
      adjacencyMatrix[nodeId][GraphInformation.numberOfNodes - 1] = true;
    }

    createGraphFromAdjacencyMatrix(graph, adjacencyMatrix);
    InputOutputParser.writeDOTFile(graph, getFilename(GraphType.JOIN));
  }

  /**
   * Creates an independent graph where nodes are not connected to each other, and writes it to a
   * DOT file.
   *
   * @throws IOException if an I/O error occurs while writing the graph to a DOT file.
   */
  public static void createIndependentGraph() throws IOException {
    Graph graph = new SingleGraph("graph");
    initialiseGraphInformation();

    for (int nodeId = 0; nodeId < GraphInformation.numberOfNodes; nodeId++) {
      addNode(graph, Integer.toString(nodeId));
    }

    InputOutputParser.writeDOTFile(graph, getFilename(GraphType.INDEPENDENT));
  }

  /**
   * Creates a chain graph where each node is connected to the next node, and writes it to a DOT
   * file.
   *
   * @throws IOException if an I/O error occurs while writing the graph to a DOT file.
   */
  public static void createChainGraph() throws IOException {
    Graph graph = new SingleGraph("graph");
    initialiseGraphInformation();

    boolean[][] adjacencyMatrix =
        new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];

    for (int nodeId = 0; nodeId < GraphInformation.numberOfNodes - 1; nodeId++) {
      adjacencyMatrix[nodeId][nodeId + 1] = true;
    }

    createGraphFromAdjacencyMatrix(graph, adjacencyMatrix);
    InputOutputParser.writeDOTFile(graph, getFilename(GraphType.CHAIN));
  }

  /**
   * Returns the filename for the DOT file based on the type of the graph.
   *
   * @param graphType the type of the graph.
   * @return the filename for the DOT file.
   */
  private static String getFilename(GraphType graphType) {
    return getFilenamePrefix(graphType).concat(getFilenameSuffix());
  }

  /**
   * Returns the filename prefix for the DOT file based on the graph type.
   *
   * @param graphType the type of the graph.
   * @return the filename prefix for the DOT file.
   */
  private static String getFilenamePrefix(GraphType graphType) {
    return switch (graphType) {
      case FORK -> "fork";
      case JOIN -> "join";
      case CHAIN -> "chain";
      case RANDOM -> "random";
      case INDEPENDENT -> "independent";
      case FORK_AND_JOIN -> "fork_and_join";
      case STENCIL -> "stencil";
    };
  }

  /**
   * Returns the filename suffix for the DOT file, which includes the number of nodes and edges in
   * the graph.
   *
   * @return the filename suffix for the DOT file.
   */
  private static String getFilenameSuffix() {
    return "_nodes_%d_edges_%d"
        .formatted(GraphInformation.numberOfNodes, GraphInformation.numberOfEdges);
  }
}
