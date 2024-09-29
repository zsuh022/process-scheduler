package scheduler.models;

/**
 * Represents an edge in the graph with a source node, a destination node, and a weight.
 */
public class EdgeModel {
    private final String id;

    private final int weight;

    private final NodeModel source;
    private final NodeModel destination;

    /**
     * Constructs an {@code EdgeModel} with the specified id, source node, destination node, and weight.
     *
     * @param id          the unique identifier of the edge
     * @param source      the source node of the edge
     * @param destination the destination node of the edge
     * @param weight      the weight (communication cost) of the edge
     */
    public EdgeModel(String id, NodeModel source, NodeModel destination, int weight) {
        this.id = id;
        
        this.weight = weight;

        this.source = source;
        this.destination = destination;
    }
    /**
     * Returns the source node of this edge.
     *
     * @return the source node
     */
    public NodeModel getSource() {
        return source;
    }
    /**
     * Returns the destination node of this edge.
     *
     * @return the destination node
     */
    public NodeModel getDestination() {
        return destination;
    }
    /**
     * Returns the weight (communication cost) of this edge.
     *
     * @return the weight of the edge
     */
    public int getWeight() {
        return weight;
    }
    
    /**
     * Returns the id assigned to the edge.
     *
     * @return {@link String} the id of the edge
     * @see String
     */
    public String getId() {
        return this.id; 
    }
}
