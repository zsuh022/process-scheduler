package scheduler.parsers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkDOT;
import org.graphstream.stream.file.FileSourceDOT;

import scheduler.models.EdgeModel;
import scheduler.models.GraphModel;
import scheduler.models.NodeModel;

public class InputOutputParser {
    public static Graph readDOTFile(String filename) throws IOException {
        Graph graph = new SingleGraph("digraph");

        FileSourceDOT fileSourceDOT = new FileSourceDOT();
        fileSourceDOT.addSink(graph);

        try {
            fileSourceDOT.readAll(filename);
        } finally {
            fileSourceDOT.removeSink(graph);
        }

        visualiseGraph(graph);

        return graph;
    }

    public static void outputDOTFile(GraphModel graph, String filename) throws IOException {
        Graph graphStream = convertGraphModelToGraphStream(graph);

        FileSinkDOT fileSinkDOT = new FileSinkDOT();
        fileSinkDOT.setDirected(true);

        fileSinkDOT.writeAll(graphStream, new FileWriter(filename));
    }

    private static Graph convertGraphModelToGraphStream(GraphModel graph) {
        Map<String, NodeModel> nodes = graph.getNodes();
        Map<String, EdgeModel> edges = graph.getEdges();

        Graph graphStream = new SingleGraph(graph.getId());

        addNodeModelsToGraphStream(graphStream, nodes);
        addEdgeModelsToGraphStream(graphStream, edges);

        return graphStream;
    }

    public static void visualiseGraph(Graph graph) {
        System.setProperty("org.graphstream.ui", "javafx");
        String styleSheet = "node { size: 20px, 20px; fill-color: red; text-color: black; }" +
                "edge { fill-color: black; size: 1px; }";

        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        // Automatically layout the graph
        // graph.display();
    }

    private static void addNodeModelsToGraphStream(Graph graph, Map<String, NodeModel> nodes) {
        nodes.forEach((id, node) -> {
            graph.addNode(id);
            graph.getNode(id).setAttribute("Weight", node.getWeight());

            if (node.getStartTime() >= 0 && node.getProcessor() > 0) {
                graph.getNode(id).setAttribute("Start", node.getStartTime());
                graph.getNode(id).setAttribute("Processor", node.getProcessor());
            }
        });
    }

    private static void addEdgeModelsToGraphStream(Graph graph, Map<String, EdgeModel> edges) {
        edges.forEach((id, edge) -> {
            NodeModel source = edge.getSource();
            NodeModel destination = edge.getDestination();

            graph.addEdge(id, source.getId(), destination.getId(), true);
            graph.getEdge(id).setAttribute("Weight", edge.getWeight());
        });
    }
}
