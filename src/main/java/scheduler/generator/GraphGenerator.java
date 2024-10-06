package scheduler.generator;

import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Graph;
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


        RandomGenerator graphGenerator = new RandomGenerator();
        graphGenerator.addSink(graph);

        graphGenerator.begin();

        for (int i = 0; i < getNumberOfRandomNodes(); i++) {
            graphGenerator.nextEvents();
        }

        graphGenerator.end();

        addRandomNodes(graph);
        addRandomEdges(graph);

        return new GraphModel(graph);
    }

    private static void addRandomNodes(Graph graph) {
        graph.nodes().forEach(node -> {
            node.setAttribute("Weight", getRandomWeight());
            ++GraphInformation.numberOfNodes;
        });
    }

    private static void addRandomEdges(Graph graph) {
        graph.edges().forEach(edge -> {
            edge.setAttribute("Weight", getRandomWeight());
            ++GraphInformation.numberOfEdges;
        });
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

    private static double getRandomWeight() {
        return getRandomInteger(WEIGHT_LOWER_BOUND, WEIGHT_UPPER_BOUND);
    }

    private static int getRandomInteger(int minimum, int maximum) {
        return random.nextInt((maximum - minimum) + 1) + minimum;
    }

    public void setNumberOfProcessors(byte numberOfProcessors) {
        GraphInformation.numberOfProcessors = numberOfProcessors;
    }
}
