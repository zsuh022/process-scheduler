package scheduler.models;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the state of a scheduling process at a given point in time.
 * Stores information about scheduled nodes, their start times, processors, and finish times.
 * Used in algorithms like branch-and-bound to keep track of the current scheduling state.
 */
public class StateModel {
    private int numberOfNodes;
    private int totalIdleTime;
    private int maximumFinishTime;
    private int numberOfScheduledNodes;

    private int[] finishTimes;
    private final int[] nodeStartTimes;

    private final byte[] nodeProcessors;
    private byte[] scheduledNodes;

    /**
     * Constructs a new {@code StateModel} with the specified number of processors and nodes.
     * Initializes arrays to keep track of processors, nodes, and scheduling times.
     *
     * @param numberOfProcessors the number of processors available for scheduling
     * @param numberOfNodes      the total number of nodes to schedule
     */
    public StateModel(int numberOfProcessors, int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
        this.totalIdleTime = 0;
        this.maximumFinishTime = 0;
        this.numberOfScheduledNodes = 0;

        this.finishTimes = new int[numberOfProcessors];
        this.nodeStartTimes = new int[numberOfNodes];

        this.nodeProcessors = new byte[numberOfNodes];
        this.scheduledNodes = new byte[numberOfNodes];

        Arrays.fill(this.nodeProcessors, (byte) -1);
    }
    /**
     * Constructs a new {@code StateModel} as a deep copy of the given state.
     *
     * @param state the state to copy
     */
    public StateModel(StateModel state) {
        this.numberOfNodes = state.numberOfNodes;
        this.totalIdleTime = state.totalIdleTime;
        this.maximumFinishTime = state.maximumFinishTime;
        this.numberOfScheduledNodes = state.numberOfScheduledNodes;

        this.finishTimes = state.finishTimes.clone();
        this.nodeStartTimes = state.nodeStartTimes.clone();

        this.nodeProcessors = state.nodeProcessors.clone();
        this.scheduledNodes = state.scheduledNodes.clone();
    }

    /**
     * Adds a node to the current state by scheduling it on a processor at a specific start time.
     * Updates finish times, scheduled nodes, and maximum finish time accordingly.
     *
     * @param node      the node to schedule
     * @param processor the processor to schedule the node on
     * @param startTime the start time for the node
     */
    public void addNode(NodeModel node, int processor, int startTime) {
        byte nodeId = node.getByteId();

        updateTotalIdleTime(processor, startTime);

        this.nodeProcessors[nodeId] = (byte) processor;
        this.nodeStartTimes[nodeId] = startTime;
        this.finishTimes[processor] = startTime + node.getWeight();

        this.scheduleNode(node.getByteId());

        this.numberOfScheduledNodes++;
        this.maximumFinishTime = Math.max(this.maximumFinishTime, this.finishTimes[processor]);
    }

    /**
     * Checks if the given node, processor, and start time represent an empty or uninitialized node.
     *
     * @param node      the node to check
     * @param processor the processor value
     * @param startTime the start time value
     * @return true if the node is null and processor and startTime are -1; false otherwise
     */
    public boolean isEmptyNode(NodeModel node, int processor, int startTime) {
        return (node == null && processor == -1 && startTime == -1);
    }

    public void updateTotalIdleTime(int processor, int startTime) {
        this.totalIdleTime += Math.max(0, startTime - this.finishTimes[processor]);
    }

    public int getTotalIdleTime() {
        return this.totalIdleTime;
    }

    /**
     * Checks if the state has no scheduled nodes.
     *
     * @return true if no nodes are scheduled; false otherwise
     */
    public boolean isEmptyState() {
        return (this.numberOfScheduledNodes == 0);
    }

    /**
     * Returns the number of nodes that have been scheduled so far.
     *
     * @return the number of scheduled nodes
     */
    public int getNumberOfScheduledNodes() {
        return this.numberOfScheduledNodes;
    }
    /**
     * Sets the number of nodes that have been scheduled.
     *
     * @param numberOfScheduledNodes the number of scheduled nodes to set
     */
    public void setNumberOfScheduledNodes(int numberOfScheduledNodes) {
        this.numberOfScheduledNodes = numberOfScheduledNodes;
    }
    /**
     * Returns an array indicating which nodes have been scheduled.
     *
     * @return an array of scheduled nodes
     */
    public byte[] getScheduledNodes() {
        return this.scheduledNodes;
    }

    /**
     * Marks a node as scheduled using its byte ID.
     *
     * @param nodeId the byte ID of the node to mark as scheduled
     */
    public void scheduleNode(byte nodeId) {
        this.scheduledNodes[nodeId] = 1;
    }
    /**
     * Returns the start time of the specified node.
     *
     * @param node the node whose start time is requested
     * @return the start time of the node
     */
    public int getNodeStartTime(NodeModel node) {
        return this.nodeStartTimes[node.getByteId()];
    }
    /**
     * Sets the array of scheduled nodes.
     *
     * @param scheduledNodes the array to set
     */
    public void setScheduledNodes(byte[] scheduledNodes) {
        this.scheduledNodes = scheduledNodes;
    }
    /**
     * Checks if a given node has been scheduled.
     *
     * @param node the node to check
     * @return true if the node is scheduled; false otherwise
     */
    public boolean isNodeScheduled(NodeModel node) {
        return (this.scheduledNodes[node.getByteId()] == 1);
    }
    /**
     * Checks if all nodes have been scheduled.
     *
     * @return true if all nodes are scheduled; false otherwise
     */
    public boolean areAllNodesScheduled() {
        return (this.numberOfScheduledNodes == this.numberOfNodes);
    }
    /**
     * Returns the maximum finish time among all processors, representing the total schedule length.
     *
     * @return the maximum finish time
     */
    public int getMaximumFinishTime() {
        return this.maximumFinishTime;
    }

    /**
     * Checks if this state is equal to another object.
     * Two states are equal if they have the same number of nodes, scheduled nodes,
     * finish times, node start times, node processors, and scheduled nodes array.
     *
     * @param o the object to compare with
     * @return true if the states are equal; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof StateModel))
            return false;
        StateModel that = (StateModel) o;
        return numberOfNodes == that.numberOfNodes &&
                numberOfScheduledNodes == that.numberOfScheduledNodes &&
                Arrays.equals(finishTimes, that.finishTimes) &&
                Arrays.equals(nodeStartTimes, that.nodeStartTimes) &&
                Arrays.equals(nodeProcessors, that.nodeProcessors) &&
                Arrays.equals(scheduledNodes, that.scheduledNodes);
    }

    /**
     * Computes the hash code for this state based on its fields.
     *
     * @return the hash code of the state
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(numberOfNodes, numberOfScheduledNodes);
        result = 31 * result + Arrays.hashCode(finishTimes);
        result = 31 * result + Arrays.hashCode(nodeStartTimes);
        result = 31 * result + Arrays.hashCode(nodeProcessors);
        result = 31 * result + Arrays.hashCode(scheduledNodes);
        return result;
    }

    @Override
    public StateModel clone() {
        return new StateModel(this);
    }

    /**
     * Returns the finish times of the processors.
     *
     * @return the finish time of the processors
     */
    public int[] getFinishTimes() {
        return finishTimes;
    }
    /**
     * Sets the finish times for all processors.
     *
     * @param finishTimes the array of finish times to set
     */
    public void setFinishTimes(int[] finishTimes) {
        this.finishTimes = finishTimes;
    }
    /**
     * Returns the processor assigned to the specified node.
     *
     * @param node the node whose processor is requested
     * @return the processor assigned to the node
     */
    public byte getNodeProcessor(NodeModel node) {
        return this.nodeProcessors[node.getByteId()];
    }

    /**
     * Returns the finish time for a specific processor.
     *
     * @return the finish time for a specific processor
     */
    public int getFinishTime(int processor) {
        return this.finishTimes[processor];
    }
}
