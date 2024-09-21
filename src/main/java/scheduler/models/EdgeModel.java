package scheduler.models;

public class EdgeModel {
    private NodeModel source;
    private NodeModel destination;

    private int weight;

    public EdgeModel(NodeModel source, NodeModel destination, int weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public NodeModel getSource() {
        return source;
    }

    public NodeModel getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }
}
