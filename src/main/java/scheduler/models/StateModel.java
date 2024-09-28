package scheduler.models;

import java.util.Arrays;
import java.util.Objects;

public class StateModel {
    private NodeModel lastScheduledNode;

    private int numberOfNodes;
    private int numberOfScheduledNodes;

    private int[] startTimes; // start times for processors
    private int[] finishTimes; // finish times for processors
    private int[] nodeStartTimes;

    private byte[] nodeProcessors; // node maps to processor index
    private byte[] scheduledNodes;

    public StateModel(int numberOfProcessors, int numberOfNodes) {
        this.numberOfScheduledNodes = 0;
        this.numberOfNodes = numberOfNodes;

        this.startTimes = new int[numberOfProcessors];
        this.finishTimes = new int[numberOfProcessors];
        this.nodeStartTimes = new int[numberOfNodes];

        this.nodeProcessors = new byte[numberOfNodes];
        this.scheduledNodes = new byte[numberOfNodes];

        Arrays.fill(this.nodeProcessors, (byte) -1);
    }

    public void addNode(NodeModel node, int processor, int startTime) {
        this.nodeProcessors[node.getByteId()] = (byte) processor;
        this.nodeStartTimes[node.getByteId()] = startTime;
        this.finishTimes[processor] = startTime + node.getWeight();
        this.numberOfScheduledNodes++;
        this.scheduleNode(node.getByteId());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateModel that = (StateModel) o;
        return numberOfNodes == that.numberOfNodes && numberOfScheduledNodes == that.numberOfScheduledNodes && Objects.equals(lastScheduledNode, that.lastScheduledNode) && Objects.deepEquals(startTimes, that.startTimes) && Objects.deepEquals(finishTimes, that.finishTimes) && Objects.deepEquals(nodeStartTimes, that.nodeStartTimes) && Objects.deepEquals(nodeProcessors, that.nodeProcessors) && Objects.deepEquals(scheduledNodes, that.scheduledNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastScheduledNode, numberOfNodes, numberOfScheduledNodes, Arrays.hashCode(startTimes), Arrays.hashCode(finishTimes), Arrays.hashCode(nodeStartTimes), Arrays.hashCode(nodeProcessors), Arrays.hashCode(scheduledNodes));
    }

    public int[] getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(int[] startTimes) {
        this.startTimes = startTimes;
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

    public int getStartTime(NodeModel node) {
        return this.startTimes[node.getByteId()];
    }
}
