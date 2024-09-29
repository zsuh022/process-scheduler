package scheduler.parsers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkDOT;
import org.graphstream.stream.file.FileSourceDOT;

import scheduler.models.EdgeModel;
import scheduler.models.GraphModel;
import scheduler.models.NodeModel;

/**
 * The InputOutputParser handles parsing input and output graphs. The input graph should be converted to a Java graph
 * object (custom-defined by us), and the output graph should be converted to a valid DOT file.
 */
public class InputOutputParser {
    /**
     * Read the input DOT file.
     *
     * @param filename the DOT file filename
     * @return the graph the converted graph object
     * @throws IOException the exception from parsing the input graph
     */
    public static Graph readDOTFile(String filename) throws IOException {
        Graph graph = new SingleGraph("digraph");

        FileSourceDOT fileSourceDOT = new FileSourceDOT();
        fileSourceDOT.addSink(graph);

        try {
            fileSourceDOT.readAll(filename);
        } finally {
            fileSourceDOT.removeSink(graph);
        }

        return graph;
    }

    /**
     * Output the graph as a valid DOT file.
     *
     * @param graph    the output graph
     * @param filename the output filename
     * @throws IOException the exception from parsing the output graph
     */
    public static void outputDOTFile(GraphModel graph, String filename) throws IOException {
        Graph graphStream = convertGraphModelToGraphStream(graph);

        FileSinkDOT fileSinkDOT = new FileSinkDOT();
        fileSinkDOT.setDirected(true);

        fileSinkDOT.writeAll(graphStream, new FileWriter(filename));
    }

    /**
     * Converts a graph model instance to a graph stream instance.
     *
     * @param graph graph the graph model instance to be converted
     * @return {@link Graph} the converted graph object
     * @see Graph
     */
    private static Graph convertGraphModelToGraphStream(GraphModel graph) {
        Map<String, NodeModel> nodes = graph.getNodes();
        Map<String, EdgeModel> edges = graph.getEdges();

        Graph graphStream = new SingleGraph(graph.getId());

        addNodeModelsToGraphStream(graphStream, nodes);
        addEdgeModelsToGraphStream(graphStream, edges);

        return graphStream;
    }

     /**
      * Adds nodes to the graph stream graph.
      *
      * @param graph graph the graph stream graph
      * @param nodes nodes the nodes to be added to the graph
      */
     private static void addNodeModelsToGraphStream(Graph graph, Map<String, NodeModel> nodes) {
        nodes.forEach((id, node) -> {
            Node nodeStream = graph.addNode(id);

            nodeStream.setAttribute("Weight", node.getWeight());

            if (node.getStartTime() >= 0 && node.getProcessor() > 0) {
                nodeStream.setAttribute("Start", node.getStartTime());
                nodeStream.setAttribute("Processor", node.getProcessor());
            }
        });
    }

    /**
     * Adds edge models to the graph stream graph
     *
     * @param graph graph the graph stream graph
     * @param edges edges the edges to be added to the graph
     */
    private static void addEdgeModelsToGraphStream(Graph graph, Map<String, EdgeModel> edges) {
        edges.forEach((id, edge) -> {
            NodeModel source = edge.getSource();
            NodeModel destination = edge.getDestination();

            graph.addEdge(id, source.getId(), destination.getId(), true);

            graph.getEdge(id).setAttribute("Weight", edge.getWeight());
        });
    }
}
