package scheduler.schedulers;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;

import java.util.*;

public class DFSScheduler extends Scheduler {
    private int bestFinishTime;

    private StateModel bestState;

    private Set<StateModel> closedStates;

    public DFSScheduler(GraphModel graph, int processors) {
        super(graph, processors);

        this.bestFinishTime = Integer.MAX_VALUE;

        this.bestState = null;

        this.closedStates = new HashSet<>();

        getDFSSchedule(new StateModel(processors, this.numberOfNodes));
    }

    @Override
    public void getDFSSchedule(StateModel state) {
        if (state.areAllNodesScheduled()) {
            int maximumFinishTime = state.getMaximumFinishTime();

            if (this.bestFinishTime > maximumFinishTime) {
                this.bestFinishTime = maximumFinishTime;
                this.bestState = state;
            }

            return;
        }

        if (this.closedStates.contains(state)) {
           return;
        }

        this.closedStates.add(state);

        // For each available node
        for (NodeModel node : getAvailableNodes(state)) {
            // For each processor
            for (int processor = 0; processor < processors; processor++) {
                StateModel nextState = state.clone();

                int earliestStartTime = getEarliestStartTime(state, node, processor);

                nextState.addNode(node, processor, earliestStartTime);

                if (nextState.getMaximumFinishTime() >= this.bestFinishTime) {
                    continue;
                }

                getDFSSchedule(nextState);
            }
        }
    }

    public StateModel getSchedule() {
        return this.bestState;
    }
}