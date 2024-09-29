package scheduler.models;

public class EdgeModel {
    String id;

    private int weight;

    private NodeModel source;
    private NodeModel destination;

    public EdgeModel(String id, NodeModel source, NodeModel destination, int weight) {
        this.id = id;

        this.weight = weight;

        this.source = source;
        this.destination = destination;
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
