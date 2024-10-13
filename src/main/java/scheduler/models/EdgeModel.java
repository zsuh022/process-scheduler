package scheduler.models;

/**
 * Represents an edge in the graph with a source node, a destination node, and a weight.
 */
public record EdgeModel(String id, NodeModel source, NodeModel destination, int weight) {
    /**
     * Constructs an {@code EdgeModel} with the specified id, source node, destination node, and weight.
     *
     * @param id          the unique identifier of the edge
     * @param source      the source node of the edge
     * @param destination the destination node of the edge
     * @param weight      the weight (communication cost) of the edge
     */
    public EdgeModel {
    }

    /**
     * Returns the source node of this edge.
     *
     * @return the source node
     */
    @Override
    public NodeModel source() {
        return source;
    }

    /**
     * Returns the destination node of this edge.
     *
     * @return the destination node
     */
    @Override
    public NodeModel destination() {
        return destination;
    }

    /**
     * Returns the weight (communication cost) of this edge.
     *
     * @return the weight of the edge
     */
    @Override
    public int weight() {
        return weight;
    }

    /**
     * Returns the id assigned to the edge.
     *
     * @return {@link String} the id of the edge
     * @see String
     */
    @Override
    public String id() {
        return this.id;
    }
}
