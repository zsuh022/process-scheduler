package scheduler.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a node (task) in the graph with an identifier, weight, scheduling information,
 * and connections to predecessor and successor nodes.
 */
public class NodeModel {
    private final String id;

    private final int weight;
    private int groupId;
    private int inDegree;
    private int outDegree;
    private int startTime;
    private int processor;

    private byte byteId;

    private final List<NodeModel> successors;
    private final List<NodeModel> predecessors;

    /**
     * Constructs a {@code NodeModel} with the specified id and weight.
     * Initializes scheduling attributes and adjacency lists.
     *
     * @param id     the unique identifier of the node
     * @param weight the weight (execution time) of the node
     */
    public NodeModel(String id, int weight) {
        this.id = id;

        this.weight = weight;
        this.groupId = -1;
        this.inDegree = 0;
        this.outDegree = 0;
        this.startTime = -1;
        this.processor = -1;

        this.byteId = (byte) -1;

        this.successors = new ArrayList<>();
        this.predecessors = new ArrayList<>();
    }

    public int getGroupId() {
        return this.groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /**
     * Returns the unique identifier of this node.
     *
     * @return the node's id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the byte representation of the node's id.
     *
     * @return the byte id
     */
    public byte getByteId() {
        return this.byteId;
    }

    /**
     * Sets the byte representation of the node's id.
     *
     * @param id the byte id to set
     */
    public void setByteId(byte id) {
        this.byteId = id;
    }

    /**
     * Returns the weight (execution time) of this node.
     *
     * @return the weight of the node
     */
    public int getWeight() {
        return this.weight;
    }

    /**
     * Returns the start time of this node in the schedule.
     *
     * @return the start time
     */
    public int getStartTime() {
        return this.startTime;
    }

    /**
     * Sets the start time of this node in the schedule.
     *
     * @param startTime the start time to set
     */
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the processor assigned to this node.
     *
     * @return the processor number
     */
    public int getProcessor() {
        return this.processor;
    }

    /**
     * Sets the processor assigned to this node.
     *
     * @param processor the processor number to set
     */
    public void setProcessor(int processor) {
        this.processor = processor;
    }

    /**
     * Returns the list of predecessor nodes.
     *
     * @return the list of predecessors
     */
    public List<NodeModel> getPredecessors() {
        return this.predecessors;
    }

    /**
     * Returns the list of successor nodes.
     *
     * @return the list of successors
     */
    public List<NodeModel> getSuccessors() {
        return this.successors;
    }

    /**
     * Adds a successor node to this node's adjacency list and increments the out-degree.
     *
     * @param node the successor node to add
     */
    public void addSuccessor(NodeModel node) {
        this.successors.add(node);
        this.outDegree++;
    }

    /**
     * Adds a predecessor node to this node's adjacency list and increments the in-degree.
     *
     * @param node the predecessor node to add
     */
    public void addPredecessor(NodeModel node) {
        this.predecessors.add(node);
        this.inDegree++;
    }

    /**
     * Returns the in-degree of this node.
     *
     * @return the in-degree
     */
    public int getInDegree() {
        return this.inDegree;
    }

    public NodeModel getPredecessor(int id) {
        return this.predecessors.get(id);
    }

    public NodeModel getSuccessor(int id) {
        return this.successors.get(id);
    }

    /**
     * Returns the out-degree of this node.
     *
     * @return the out-degree
     */
    public int getOutDegree() {
        return this.outDegree;
    }

    /**
     * Computes the hash code for this node based on its id and weight.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, weight);
    }

    /**
     * Compares this node to another object for equality.
     * Two nodes are equal if they have the same id.
     *
     * @param obj the object to compare
     * @return {@code true} if the nodes are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof NodeModel))
            return false;
        NodeModel other = (NodeModel) obj;
        return this.id.equals(other.id);
    }
}
