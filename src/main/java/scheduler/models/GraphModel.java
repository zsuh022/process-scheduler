package scheduler.models;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.Graph;

import scheduler.parsers.InputOutputParser;

/**
 * This {@code GraphModel} class represents a directed acyclic graph. Used for task scheduling.
 * Keeps track of the number of nodes, and information on nodes and edges.
 */
public class GraphModel {
    private Graph graph;

    private String rootId;

    private int numberOfNodes;

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

        setNodes();
        setEdges();

        setRoot();
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
     * Method sets the root node of the graph. Identifies node with no predecessors.
     */
    public void setRoot() {
        for (NodeModel node : this.nodes.values()) {
            if (node.getPredecessors().size() == 0) {
                this.rootId = node.getId();
                break;
            }
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
     * Method returns the root node of the graph.
     *
     * @return the root node.
     */
    public NodeModel getRoot() {
        return this.nodes.get(this.rootId);
    }

    /**
     * Method returns the node with the given ID.
     *
     * @param id represents the ID of the node.
     * @return the node with the given ID.
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

    /**
     * Method returns an adjacency list representing the graph.
     *
     * @return a hashmap with node ID keys and child node ID values.
     */
    public HashMap<String, List<String>> getAdjacencyList() {
        HashMap<String, List<String>> adjacencyList = new HashMap<>();

        for (EdgeModel edge : this.edges.values()) {
            NodeModel source = edge.getSource();
            NodeModel destination = edge.getDestination();

            adjacencyList.get(source.getId()).add(destination.getId());
        }

        return adjacencyList;
    }

    /**
     * Method returns a map of nodes in the graph.
     *
     * @return a map with node ID keys and node object values.
     */
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

    /**
     * Method returns the graph object.
     *
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }
}
