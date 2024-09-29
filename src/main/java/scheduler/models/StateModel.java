package scheduler.models;

import java.util.Arrays;
import java.util.Objects;

public class StateModel {
    private NodeModel lastScheduledNode;

    private int numberOfNodes;
    private int maximumFinishTime;
    private int numberOfScheduledNodes;

    private int[] finishTimes;
    private int[] nodeStartTimes;

    private byte[] nodeProcessors;
    private byte[] scheduledNodes;

    public StateModel(int numberOfProcessors, int numberOfNodes) {
        this.maximumFinishTime = 0;
        this.numberOfNodes = numberOfNodes;
        this.numberOfScheduledNodes = 0;

        this.finishTimes = new int[numberOfProcessors];
        this.nodeStartTimes = new int[numberOfNodes];

        this.nodeProcessors = new byte[numberOfNodes];
        this.scheduledNodes = new byte[numberOfNodes];

        Arrays.fill(this.nodeProcessors, (byte) -1);
    }

    public StateModel(StateModel state) {
        this.numberOfNodes = state.numberOfNodes;
        this.numberOfScheduledNodes = state.numberOfScheduledNodes;

        this.finishTimes = state.finishTimes.clone();
        this.nodeStartTimes = state.nodeStartTimes.clone();

        this.nodeProcessors = state.nodeProcessors.clone();
        this.scheduledNodes = state.scheduledNodes.clone();
    }

    public void addNode(NodeModel node, int processor, int startTime) {
        byte nodeId = node.getByteId();

        this.nodeProcessors[nodeId] = (byte) processor;
        this.nodeStartTimes[nodeId] = startTime;
        this.finishTimes[processor] = startTime + node.getWeight();

        this.scheduleNode(node.getByteId());

        this.numberOfScheduledNodes++;
        this.maximumFinishTime = Math.max(this.maximumFinishTime, this.finishTimes[processor]);
    }

    public boolean isEmptyNode(NodeModel node, int processor, int startTime) {
        return (node == null && processor == -1 && startTime == -1);
    }

    public boolean isEmptyState() {
        return (this.numberOfScheduledNodes == 0);
    }

    public NodeModel getLastScheduledNode() {
        return this.lastScheduledNode;
    }

    public void setLastScheduledNode(NodeModel lastScheduledNode) {
        this.lastScheduledNode = lastScheduledNode;
    }

    public int getNumberOfNodes() {
        return this.numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public int getNumberOfScheduledNodes() {
        return this.numberOfScheduledNodes;
    }

    public void setNumberOfScheduledNodes(int numberOfScheduledNodes) {
        this.numberOfScheduledNodes = numberOfScheduledNodes;
    }

    public byte[] getScheduledNodes() {
        return this.scheduledNodes;
    }

    public void scheduleNode(byte nodeId) {
        this.scheduledNodes[nodeId] = 1;
    }

    public int getNodeStartTime(NodeModel node) {
        return this.nodeStartTimes[node.getByteId()];
    }

    public void setScheduledNodes(byte[] scheduledNodes) {
        this.scheduledNodes = scheduledNodes;
    }

    public boolean isNodeScheduled(NodeModel node) {
        return (this.scheduledNodes[node.getByteId()] == 1);
    }

    public boolean areAllNodesScheduled() {
        return (this.numberOfScheduledNodes == this.numberOfNodes);
    }

    public int getMaximumFinishTime() {
        return this.maximumFinishTime;
    }

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

    public int[] getFinishTimes() {
        return finishTimes;
    }

    public void setFinishTimes(int[] finishTimes) {
        this.finishTimes = finishTimes;
    }

    public byte getNodeProcessor(NodeModel node) {
        return this.nodeProcessors[node.getByteId()];
    }

    public int getFinishTime(int processor) {
        return this.finishTimes[processor];
    }
}
