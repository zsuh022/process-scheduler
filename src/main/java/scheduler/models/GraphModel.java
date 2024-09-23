package scheduler.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.Graph;

import scheduler.parsers.InputOutputParser;

public class GraphModel {
    private Graph graph;

    private Map<String, NodeModel> nodes;
    private Map<String, EdgeModel> edges;

    public GraphModel(String filename) throws IOException {
        this.graph = InputOutputParser.readDOTFile(filename);

        this.setNodes();
        this.setEdges();
    }

    private void setNodes() {
        Map<String, NodeModel> nodes = new HashMap<>();

        graph.nodes().forEach(node -> {
            String id = node.getId();
            int weight = (int) Math.round((Double) node.getAttribute("Weight"));

            nodes.put(id, new NodeModel(id, weight));
        });

        this.nodes = nodes;
    }

    private void setEdges() {
        Map<String, EdgeModel> edges = new HashMap<>();

        graph.edges().forEach(edge -> {
            String id = edge.getId();

            NodeModel source = getNode(edge.getSourceNode().getId());
            NodeModel destination = getNode(edge.getTargetNode().getId());

            int weight = (int) Math.round((Double) edge.getAttribute("Weight"));

            edges.put(id, new EdgeModel(source, destination, weight));

            source.getSuccessors().add(destination);
            destination.getPredecessors().add(source);
            
        });

        this.edges = edges;
    }

    public String getId() {
        return this.graph.getId();
    }

    public NodeModel getNode(String id) {
        return this.nodes.get(id);
    }

    public EdgeModel getEdge(String id) {
        return this.edges.get(id);
    }

    public List<Integer> getAdjacencyList() {
        return new ArrayList<>();
    }

    public Map<String, NodeModel> getNodes() {
        return this.nodes;
    }

    public Map<String, EdgeModel> getEdges() {
        return this.edges;
    }

    public Graph getGraph() {
        return graph;
    }
}
