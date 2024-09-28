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

    private byte byteId;

    private List<NodeModel> successors;
    private List<NodeModel> predecessors;

    public NodeModel(String id, int weight) {
        this.id = id;

        this.weight = weight;
        this.inDegree = 0;
        this.outDegree = 0;
        this.startTime = -1;
        this.processor = -1;

        this.successors = new ArrayList<>();
        this.predecessors = new ArrayList<>();
    }

    public String getId() {
        return this.id;
    }

    public byte getByteId() {
        return this.byteId;
    }

    public int getWeight() {
        return this.weight;
    }

    public int getStartTime() {
        return this.startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getProcessor() {
        return this.processor;
    }

    public void setProcessor(int processor) {
        this.processor = processor;
    }

    public List<NodeModel> getPredecessors() {
        return this.predecessors;
    }

    public List<NodeModel> getSuccessors() {
        return this.successors;
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

    public void setByteId(byte id) {
        this.byteId = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeModel nodeModel = (NodeModel) o;
        return weight == nodeModel.weight && inDegree == nodeModel.inDegree && outDegree == nodeModel.outDegree && startTime == nodeModel.startTime && processor == nodeModel.processor && byteId == nodeModel.byteId && Objects.equals(id, nodeModel.id) && Objects.equals(successors, nodeModel.successors) && Objects.equals(predecessors, nodeModel.predecessors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, weight, inDegree, outDegree, startTime, processor, byteId, successors, predecessors);
    }
}
