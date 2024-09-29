package scheduler.models;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.Graph;

import scheduler.parsers.InputOutputParser;

public class GraphModel {
    private Graph graph;

    private int numberOfNodes;

    private Map<String, NodeModel> nodes;
    private Map<String, EdgeModel> edges;

    public GraphModel(String filename) throws IOException {
        this.graph = InputOutputParser.readDOTFile(filename);

        this.numberOfNodes = 0;

        setNodes();
        setEdges();
    }

    private void setNodes() {
        Map<String, NodeModel> nodes = new HashMap<>();

        graph.nodes().forEach(node -> {
            String id = node.getId();
            int weight = (int) Math.round((Double) node.getAttribute("Weight"));

            nodes.put(id, new NodeModel(id, weight));

            this.numberOfNodes++;

            NodeModel nodeModel = nodes.get(id);

            if (node.hasAttribute("Start")) {
                int start = (int) Math.round((Double) node.getAttribute("Start"));
                nodeModel.setStartTime(start);
            }

            if (node.hasAttribute("Processor")) {
                int processor = (int) Math.round((Double) node.getAttribute("Processor"));
                nodeModel.setProcessor(processor);
            }
        });

        this.nodes = nodes;
    }

    private void setEdges() {
        Map<String, EdgeModel> edges = new HashMap<>();

        graph.edges().forEach(edge -> {
            NodeModel source = getNode(edge.getSourceNode().getId());
            NodeModel destination = getNode(edge.getTargetNode().getId());

            String id = source.getId().concat(destination.getId());

            int weight = (int) Math.round((Double) edge.getAttribute("Weight"));

            edges.put(id, new EdgeModel(id, source, destination, weight));

            source.getSuccessors().add(destination);
            destination.getPredecessors().add(source);
        });

        this.edges = edges;
    }

    public void setNodesAndEdgesForState(StateModel state) {
        for (NodeModel node : this.nodes.values()) {
            node.setProcessor(state.getNodeProcessor(node));
            node.setStartTime(state.getNodeStartTime(node));
        }
    }

    public String getId() {
        return this.graph.getId();
    }

    public int getNumberOfNodes() {
        return this.numberOfNodes;
    }

    public NodeModel getNode(String id) {
        return this.nodes.get(id);
    }

    public EdgeModel getEdge(String id) {
        return this.edges.get(id);
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
