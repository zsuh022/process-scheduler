package scheduler.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NodeModel {
    private String id;

    private int weight;
    private int inDegree;
    private int outDegree;
    private int startTime;
    private int processor;

    private List<NodeModel> successors = new ArrayList<>();
    private List<NodeModel> predecessors = new ArrayList<>();

    public NodeModel(String id, int weight) {
        this.id = id;

        this.weight = weight;
        this.inDegree = 0;
        this.outDegree = 0;
        this.startTime = -1;
        this.processor = -1;
    }

    public String getId() {
        return id;
    }

    public int getWeight() {
        return weight;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getProcessor() {
        return processor;
    }

    public void setProcessor(int processor) {
        this.processor = processor;
    }

    public List<NodeModel> getPredecessors() {
        return predecessors;
    }

    public List<NodeModel> getSuccessors() {
        return successors;
    }

    public void addSuccessor(NodeModel node) {
        this.successors.add(node);
        this.outDegree++;
    }

    public void addPredecessor(NodeModel node) {
        this.predecessors.add(node);
        this.inDegree++;
    }

    public int getInDegree() {
        return this.inDegree;
    }

    public int getOutDegree() {
        return this.outDegree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof NodeModel))
            return false;
        NodeModel nodeModel = (NodeModel) o;
        return getWeight() == nodeModel.getWeight() &&
                getId().equals(nodeModel.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getWeight());
    }
}
