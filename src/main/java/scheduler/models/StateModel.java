package scheduler.models;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

public class StateModel {
    private NodeModel lastScheduledNode;

    private int finishTime;
    private int numberOfNodes;
    private int numberOfScheduledNodes;

    private int[] processorTimes;

    private byte[] scheduledNodes;

    public StateModel(int numberOfProcessors, int numberOfNodes) {
        this.finishTime = 0;
        this.numberOfScheduledNodes = 0;
        this.numberOfNodes = numberOfNodes;

        this.processorTimes = new int[numberOfProcessors];

        this.scheduledNodes = new byte[numberOfNodes];
    }

    public NodeModel getLastScheduledNode() {
        return this.lastScheduledNode;
    }

    public void setLastScheduledNode(NodeModel lastScheduledNode) {
        this.lastScheduledNode = lastScheduledNode;
    }

    public int getFinishTime() {
        return this.finishTime;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
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

    public int[] getProcessorTimes() {
        return this.processorTimes;
    }

    public void setProcessorTimes(int[] processorTimes) {
        this.processorTimes = processorTimes;
    }

    public byte[] getScheduledNodes() {
        return this.scheduledNodes;
    }

    public void scheduleNode(byte nodeId) {
        this.scheduledNodes[nodeId] = 1;
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
}
