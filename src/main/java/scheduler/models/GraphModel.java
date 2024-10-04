package scheduler.models;

import java.io.IOException;
import java.util.*;

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

    private List<List<NodeModel>> equivalentNodes;

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

        this.equivalentNodes = new ArrayList<>();

        setNodes();
        setEdges();

        findEquivalentNodes();
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

            ++this.numberOfNodes;
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

    // Decide on a fixed order - part of pruning
    public void findEquivalentNodes() {
        for (NodeModel node : this.nodes.values()) {
            boolean isEquivalentNodeGroupFound = false;

            for (int groupId = 0; groupId < this.equivalentNodes.size(); groupId++) {
                List<NodeModel> equivalentNodeGroup = this.equivalentNodes.get(groupId);
                int groupSize = equivalentNodeGroup.size();

                if (areNodesEquivalent(node, equivalentNodeGroup.get(groupSize - 1))) {
                    node.setGroupId(groupId);
                    equivalentNodeGroup.add(node);
                    isEquivalentNodeGroupFound = true;

                    break;
                }
            }

            if (!isEquivalentNodeGroupFound) {
                node.setGroupId(this.equivalentNodes.size());
                this.equivalentNodes.add(new LinkedList<>(Collections.singletonList(node)));
            }
        }
    }

    private boolean areNodesEquivalent(NodeModel nodeA, NodeModel nodeB) {
        if (nodeA.getWeight() != nodeB.getWeight()) {
            return false;
        }

        if (!nodeA.getPredecessors().equals(nodeB.getPredecessors())) {
            return false;
        }

        if (!nodeA.getSuccessors().equals(nodeB.getSuccessors())) {
            return false;
        }

        if (!arePredecessorEdgeWeightsEquivalent(nodeA, nodeB)) {
            return false;
        }

        return areSuccessorEdgeWeightsEquivalent(nodeA, nodeB);
    }

    public List<NodeModel> getEquivalentNodeGroup(int groupId) {
        return this.equivalentNodes.get(groupId);
    }

    private boolean arePredecessorEdgeWeightsEquivalent(NodeModel nodeA, NodeModel nodeB) {
        for (NodeModel predecessor : nodeA.getPredecessors()) {
            int weightA = getEdge(predecessor, nodeA).getWeight();
            int weightB = getEdge(predecessor, nodeB).getWeight();

            if (weightA != weightB) {
                return false;
            }
        }

        return true;
    }

    private boolean areSuccessorEdgeWeightsEquivalent(NodeModel nodeA, NodeModel nodeB) {
        for (NodeModel successor : nodeA.getSuccessors()) {
            int weightA = getEdge(nodeA, successor).getWeight();
            int weightB = getEdge(nodeB, successor).getWeight();

            if (weightA != weightB) {
                return false;
            }
        }

        return true;
    }

    public List<List<NodeModel>> getEquivalentNodes() {
        return this.equivalentNodes;
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

    public EdgeModel getEdge(NodeModel source, NodeModel destination) {
        String edgeId = getEdgeId(source, destination);

        return this.edges.get(edgeId);
    }

    public String getEdgeId(NodeModel source, NodeModel destination) {
        String sourceId = source.getId();
        String destinationId = destination.getId();

        return sourceId.concat(destinationId);
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
