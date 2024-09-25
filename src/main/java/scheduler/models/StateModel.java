package scheduler.models;

import java.util.Arrays;

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
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
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
