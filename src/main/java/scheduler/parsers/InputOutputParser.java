package scheduler.parsers;

import java.io.File;
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

        return graph;
    }

    public static void outputDOTFile(GraphModel graph, String filename) throws IOException {
        Graph graphStream = convertGraphModelToGraphStream(graph);

        FileSinkDOT fileSinkDOT = new FileSinkDOT();
        fileSinkDOT.setDirected(true);

        fileSinkDOT.writeAll(graphStream, new FileWriter(new File(filename)));
    }

    private static Graph convertGraphModelToGraphStream(GraphModel graph) {
        Map<String, NodeModel> nodes = graph.getNodes();
        Map<String, EdgeModel> edges = graph.getEdges();

        Graph graphStream = new SingleGraph(graph.getId());

        addNodeModelsToGraphStream(graphStream, nodes);
        addEdgeModelsToGraphStream(graphStream, edges);

        return graphStream;
    }

    private static void addNodeModelsToGraphStream(Graph graph, Map<String, NodeModel> nodes) {
        nodes.forEach((id, node) -> {
            graph.addNode(id);
            graph.getNode(id).setAttribute("Weight", node.getWeight());
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
