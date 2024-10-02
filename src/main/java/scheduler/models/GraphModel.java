package scheduler.models;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.graphstream.graph.Graph;

import scheduler.parsers.InputOutputParser;

/**
 * This {@code GraphModel} class represents a directed acyclic graph. Used for task scheduling.
 * Keeps track of the number of nodes, and information on nodes and edges.
 */
public class GraphModel {
    private Graph graph;

    private int numberOfNodes;
    private int totalNodeWeight;

    private Map<String, NodeModel> nodes;
    private Map<String, EdgeModel> edges;

    /**
     * Constructor for GraphModel class. Loads a graph from a DOT file and initialises the nodes
     * and edges.
     *
     * @param filename represents the path to the file to be read.
     * @throws IOException if error occurs while reading the input file.
     */
    public GraphModel(String filename) throws IOException {
        this.graph = InputOutputParser.readDOTFile(filename);

        this.numberOfNodes = 0;
        this.totalNodeWeight = 0;

        setNodes();
        setEdges();
    }

    /**
     * Method used to initialise the nodes in the graph. Nodes are stored in nodes map.
     * Reads "Weight", "Start" (start time), "Processor" attributes.
     */
    private void setNodes() {
        Map<String, NodeModel> nodes = new HashMap<>();

        graph.nodes().forEach(node -> {
            String id = node.getId();

            int weight = (int) Math.round((Double) node.getAttribute("Weight"));
            nodes.put(id, new NodeModel(id, weight));

            NodeModel nodeModel = nodes.get(id);

            if (node.hasAttribute("Start")) {
                int start = (int) Math.round((Double) node.getAttribute("Start"));
                nodeModel.setStartTime(start);
            }

            if (node.hasAttribute("Processor")) {
                int processor = (int) Math.round((Double) node.getAttribute("Processor"));
                nodeModel.setProcessor(processor);
            }

            this.numberOfNodes++;
            this.totalNodeWeight += weight;
        });

        this.nodes = nodes;
    }

    /**
     * Method used to initialise the edges in the graph. Edges are stored in the edges map.
     * Reads "Weight" attribute.
     */
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

    /**
     * Method sets the nodes and edges for the state.
     *
     * @param state represents the current state of the schedule.
     */
    public void setNodesAndEdgesForState(StateModel state) {
        for (NodeModel node : this.nodes.values()) {
            node.setProcessor(state.getNodeProcessor(node) + 1);
            node.setStartTime(state.getNodeStartTime(node));
        }
    }

    /**
     * Method returns the ID of the graph.
     *
     * @return the ID of the graph
     */
    public String getId() {
        return this.graph.getId();
    }

    /**
     * Method returns the number of nodes in the graph.
     *
     * @return the number of nodes.
     */
    public int getNumberOfNodes() {
        return this.numberOfNodes;
    }

    /**
     *
     * @param id represents the ID of the node.
     * @return the edge with the given ID.
     */
    public NodeModel getNode(String id) {
        return this.nodes.get(id);
    }

    /**
     * Method returns the edge with the given ID.
     *
     * @param id represents the ID of the edge.
     * @return the edge with the given ID.
     */
    public EdgeModel getEdge(String id) {
        return this.edges.get(id);
    }

    public Map<String, NodeModel> getNodes() {
        return this.nodes;
    }

    /**
     * Method returns a map of edges in the graph.
     *
     * @return a map with edge ID keys and edge object values.
     */
    public Map<String, EdgeModel> getEdges() {
        return this.edges;
    }

    public int getTotalNodeWeight() {
        return this.totalNodeWeight;
    }

    /**
     * Method returns the graph object.
     *
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }
}
