package scheduler.models;

public class NodeModel {
    private String id;

    private int weight;

    public NodeModel(String id, int weight) {
        this.id = id;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public int getWeight() {
        return weight;
    }
}
