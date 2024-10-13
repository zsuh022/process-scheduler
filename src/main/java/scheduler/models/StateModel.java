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
    private final byte[] scheduledNodes;
    private final byte[] normalisedProcessors;

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
        this.scheduledNodes = new byte[numberOfNodes];
        this.normalisedProcessors = new byte[numberOfNodes];

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

    private void normaliseProcessors() {
        byte[] nodeProcessorNormalisationIndices = new byte[this.numberOfProcessors];

        byte normalisationIndex = 0;

        Arrays.fill(nodeProcessorNormalisationIndices, (byte) -1);

        for (int nodeId = 0; nodeId < this.numberOfNodes; nodeId++) {
            if (isNodeScheduled(nodeId)) {
                byte nodeProcessorIndex = this.nodeProcessors[nodeId];

                if (nodeProcessorNormalisationIndices[nodeProcessorIndex] == -1) {
                    nodeProcessorNormalisationIndices[nodeProcessorIndex] = normalisationIndex++;
                }

                this.normalisedProcessors[nodeId] = nodeProcessorNormalisationIndices[nodeProcessorIndex];
            }
        }
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
    public boolean isEmpty() {
        return (this.numberOfScheduledNodes == 0);
    }

    /**
     * Returns the number of nodes that have been scheduled so far.
     *
     * @return the number of scheduled nodes
     */
    public byte getNumberOfScheduledNodes() {
        return this.numberOfScheduledNodes;
    }

    public int[] getNodeStartTimes() {
        return this.nodeStartTimes;
    }

    public int[] getNodeStartTimesCopy() {
        return this.nodeStartTimes.clone();
    }

    public byte[] getNodeProcessors() {
        return this.nodeProcessors;
    }


    public int getParentMaximumBottomLevelPathLength() {
        return this.parentMaximumBottomLevelPathLength;
    }

    public void setParentMaximumBottomLevelPathLength(int bottomLevelPathLength) {
        this.parentMaximumBottomLevelPathLength = bottomLevelPathLength;
    }

    public void setMaximumBottomLevelPathLength(int maximumBottomLevelPathLength) {
        this.maximumBottomLevelPathLength = maximumBottomLevelPathLength;
    }

    public int getMaximumBottomLevelPathLength() {
        return this.maximumBottomLevelPathLength;
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

    public int getNodeStartTime(byte nodeId) {
        return this.nodeStartTimes[nodeId];
    }

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

    public int getNodeFinishTime(NodeModel node) {
        return this.getNodeStartTime(node) + node.getWeight();
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

    public boolean isNodeScheduled(int nodeId) {
        return (this.scheduledNodes[nodeId] == 1);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.numberOfNodes, this.numberOfScheduledNodes);

        result = 31 * result + Arrays.hashCode(this.scheduledNodes);
        result = 31 * result + Arrays.hashCode(this.normalisedProcessors);
        result = 31 * result + Arrays.hashCode(this.nodeStartTimes);

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
        return this.finishTimes;
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

    public byte getNodeProcessor(byte nodeId) {
        return this.nodeProcessors[nodeId];
    }

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

    public void setFCost(int fCost) {
        this.fCost = fCost;
    }

    public int getFCost() {
        return this.fCost;
    }
}
