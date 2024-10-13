package scheduler.models;

import java.util.*;

/**
 * Represents the state of a scheduling process at a given point in time.
 * Stores information about scheduled nodes, their start times, processors, and finish times.
 * Used in algorithms like branch-and-bound to keep track of the current scheduling state.
 */
public class StateModel {
    private byte lastNodeId;
    private final byte numberOfNodes;
    private final byte numberOfProcessors;
    private byte numberOfScheduledNodes;

    private int fCost;
    private int totalIdleTime;
    private int maximumFinishTime;
    private int maximumBottomLevelPathLength;
    private int parentMaximumBottomLevelPathLength;

    private final int[] finishTimes;
    private final int[] nodeStartTimes;

    private final byte[] nodeProcessors;
    private final byte[] normalisedProcessors;

    private final boolean[] scheduledNodes;

    /**
     * Constructs a new {@code StateModel} with the specified number of processors and nodes.
     * Initializes arrays to keep track of processors, nodes, and scheduling times.
     *
     * @param numberOfProcessors the number of processors available for scheduling
     * @param numberOfNodes      the total number of nodes to schedule
     */
    public StateModel(byte numberOfProcessors, byte numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
        this.totalIdleTime = 0;
        this.maximumFinishTime = 0;
        this.maximumBottomLevelPathLength = 0;
        this.parentMaximumBottomLevelPathLength = 0;

        this.numberOfProcessors = numberOfProcessors;
        this.numberOfScheduledNodes = 0;

        this.finishTimes = new int[numberOfProcessors];
        this.nodeStartTimes = new int[numberOfNodes];

        this.nodeProcessors = new byte[numberOfNodes];
        this.normalisedProcessors = new byte[numberOfNodes];

        this.scheduledNodes = new boolean[numberOfNodes];

        Arrays.fill(this.nodeProcessors, (byte) -1);
        Arrays.fill(this.normalisedProcessors, (byte) -1);
    }

    /**
     * Constructs a new {@code StateModel} as a deep copy of the given state.
     *
     * @param state the state to copy
     */
    public StateModel(StateModel state) {
        this.lastNodeId = state.lastNodeId;

        this.fCost = state.fCost;
        this.numberOfNodes = state.numberOfNodes;
        this.totalIdleTime = state.totalIdleTime;
        this.maximumFinishTime = state.maximumFinishTime;
        this.maximumBottomLevelPathLength = state.maximumBottomLevelPathLength;
        this.parentMaximumBottomLevelPathLength = state.parentMaximumBottomLevelPathLength;

        this.numberOfProcessors = state.numberOfProcessors;
        this.numberOfScheduledNodes = state.numberOfScheduledNodes;

        this.finishTimes = state.finishTimes.clone();
        this.nodeStartTimes = state.nodeStartTimes.clone();

        this.nodeProcessors = state.nodeProcessors.clone();
        this.scheduledNodes = state.scheduledNodes.clone();
        this.normalisedProcessors = state.normalisedProcessors.clone();
    }

    /**
     * Adds a node to the current state by scheduling it on a processor at a specific start time.
     * Updates finish times, scheduled nodes, and maximum finish time accordingly.
     *
     * @param node      the node to schedule
     * @param processor the processor to schedule the node on
     * @param startTime the start time for the node
     */
    public void addNode(NodeModel node, byte processor, int startTime) {
        byte nodeId = node.getByteId();

        updateTotalIdleTime(processor, startTime);

        this.nodeProcessors[nodeId] = processor;
        this.nodeStartTimes[nodeId] = startTime;
        this.finishTimes[processor] = startTime + node.getWeight();

        scheduleNode(nodeId);
        normaliseProcessors();

        this.lastNodeId = nodeId;
        this.maximumFinishTime = Math.max(this.maximumFinishTime, this.finishTimes[processor]);

        ++this.numberOfScheduledNodes;
    }

    /**
     * Each time a task is added, we re-normalise the processors. This could be optimised further, but due to time
     * constraints, we were not able to provide a novel implementation.
     */
    private void normaliseProcessors() {
        byte[] nodeProcessorNormalisationIndices = new byte[this.numberOfProcessors];

        byte normalisationIndex = 0;

        Arrays.fill(nodeProcessorNormalisationIndices, (byte) -1);

        for (byte nodeId = 0; nodeId < this.numberOfNodes; nodeId++) {
            if (isNodeScheduled(nodeId)) {
                byte nodeProcessorIndex = this.nodeProcessors[nodeId];

                if (nodeProcessorNormalisationIndices[nodeProcessorIndex] == -1) {
                    nodeProcessorNormalisationIndices[nodeProcessorIndex] = normalisationIndex++;
                }

                this.normalisedProcessors[nodeId] = nodeProcessorNormalisationIndices[nodeProcessorIndex];
            }
        }
    }

    /**
     * Updates the total idle time based on dynamic programming approach.
     *
     * @param processor the processor
     * @param startTime the start time
     */
    public void updateTotalIdleTime(int processor, int startTime) {
        this.totalIdleTime += Math.max(0, startTime - this.finishTimes[processor]);
    }

    /**
     * Returns the total idle time for the current state/schedule.
     *
     * @return the total idle time
     */
    public int getTotalIdleTime() {
        return this.totalIdleTime;
    }

    /**
     * Checks if the state has no scheduled nodes.
     *
     * @return true if no nodes are scheduled; false otherwise
     */
    public boolean isEmpty() {
        return (this.numberOfScheduledNodes == 0);
    }

    /**
     * Returns the list of node/task start times.
     *
     * @return an array of node/task start times
     */
    public int[] getNodeStartTimes() {
        return this.nodeStartTimes;
    }

    /**
     * Returns the parent state's maximum bottom level path length.
     *
     * @return the parent state's maximum bottom level path length
     */
    public int getParentMaximumBottomLevelPathLength() {
        return this.parentMaximumBottomLevelPathLength;
    }

    /**
     * Sets the parent state's maximum bottom level path length.
     *
     * @param maximumBottomLevelPathLength the maximum bottom level path length
     */
    public void setParentMaximumBottomLevelPathLength(int maximumBottomLevelPathLength) {
        this.parentMaximumBottomLevelPathLength = maximumBottomLevelPathLength;
    }

    /**
     * Sets the maximum bottom level path length for the current state/schedule.
     *
     * @param maximumBottomLevelPathLength the maximum bottom level path length
     */
    public void setMaximumBottomLevelPathLength(int maximumBottomLevelPathLength) {
        this.maximumBottomLevelPathLength = maximumBottomLevelPathLength;
    }

    /**
     * Retrieves the maximum bottom level path length for the current state/schedule. This optimised our
     * algorithm from O(|free(s) * |P|) to O(1). However, that increased the memory usage by n + 9 bytes.
     *
     * @return the maximum bottom level path length
     */
    public int getMaximumBottomLevelPathLength() {
        return this.maximumBottomLevelPathLength;
    }

    /**
     * Marks a node as scheduled using its byte ID.
     *
     * @param nodeId the byte ID of the node to mark as scheduled
     */
    public void scheduleNode(byte nodeId) {
        this.scheduledNodes[nodeId] = true;
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
     * Returns the start time for a node for the corresponding node id.
     *
     * @param nodeId the node id
     * @return node start time
     */
    public int getNodeStartTime(byte nodeId) {
        return this.nodeStartTimes[nodeId];
    }

    /**
     * Retrieves the last node scheduled.
     *
     * @return the last node scheduled
     */
    public byte getLastNode() {
        return this.lastNodeId;
    }

    /**
     * Checks if a given node has been scheduled.
     *
     * @param node the node to check
     * @return true if the node is scheduled; false otherwise
     */
    public boolean isNodeScheduled(NodeModel node) {
        return this.scheduledNodes[node.getByteId()];
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
     * @param object the object to compare with
     * @return true if the states are equal; false otherwise
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof StateModel that)) {
            return false;
        }

        if (this.numberOfNodes != that.numberOfNodes || this.numberOfScheduledNodes != that.numberOfScheduledNodes) {
            return false;
        }

        if (!Arrays.equals(this.scheduledNodes, that.scheduledNodes)) {
            return false;
        }

        for (byte nodeId = 0; nodeId < this.numberOfNodes; nodeId++) {
            if (this.scheduledNodes[nodeId] != that.scheduledNodes[nodeId]) {
                continue;
            }

            if (this.isNodeScheduled(nodeId)) {
                byte thisNormalisedProcessor = this.normalisedProcessors[nodeId];
                byte thatNormalisedProcessor = that.normalisedProcessors[nodeId];

                if (thisNormalisedProcessor != thatNormalisedProcessor) {
                    return false;
                }

                if (this.nodeStartTimes[nodeId] != that.nodeStartTimes[nodeId]) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if a node is scheduled for the corresponding id.
     *
     * @param nodeId the node's id to check
     * @return is the node scheduled
     */
    public boolean isNodeScheduled(int nodeId) {
        return this.scheduledNodes[nodeId];
    }

    /**
     * Returns the hash code for this class. It ensures correctness in the schedule expansion.
     *
     * @return the hash code of the class
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(this.numberOfNodes, this.numberOfScheduledNodes);

        result = 31 * result + Arrays.hashCode(this.scheduledNodes);
        result = 31 * result + Arrays.hashCode(this.normalisedProcessors);
        result = 31 * result + Arrays.hashCode(this.nodeStartTimes);

        return result;
    }

    /**
     * Creates a cloned state model
     *
     * @return the cloned state model
     */
    @Override
    public StateModel clone() {
        return new StateModel(this);
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
     * Returns a list of nodes on the same processor sorted by their start time. It is used for schedule equivalence
     * pruning.
     *
     * @param processor the processor to check
     * @return a list of nodes on the same processor sorted by their start time
     */
    public List<Byte> getNodesOnSameProcessorSortedOnStartTime(byte processor) {
        List<Byte> nodesOnSameProcessor = new ArrayList<>();

        for (byte nodeId = 0; nodeId < this.numberOfNodes; nodeId++) {
            if (this.nodeProcessors[nodeId] == processor) {
                nodesOnSameProcessor.add(nodeId);
            }
        }

        nodesOnSameProcessor.sort(Comparator.comparingInt(this::getNodeStartTime));

        return nodesOnSameProcessor;
    }

    /**
     * Returns the finish time for a specific processor.
     *
     * @return the finish time for a specific processor
     */
    public int getFinishTime(byte processor) {
        return this.finishTimes[processor];
    }

    /**
     * Sets the f-cost of the current state/schedule.
     *
     * @param fCost the f-cost to be set
     */
    public void setFCost(int fCost) {
        this.fCost = fCost;
    }
}
