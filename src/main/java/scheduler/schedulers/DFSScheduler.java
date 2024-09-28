package scheduler.schedulers;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;

import java.util.*;

public class DFSScheduler extends Scheduler {
    private int bestFinishTime;

    private StateModel bestState;

    public DFSScheduler(GraphModel graph, int processors) {
        super(graph, processors);

        this.bestFinishTime = Integer.MAX_VALUE;

        this.bestState = null;

        getDFSSchedule(new StateModel(processors, this.numberOfNodes), new HashSet<>());
    }

    @Override
    public void getDFSSchedule(StateModel state, Set<StateModel> closedStates) {
        if (state.areAllNodesScheduled()) {
            int finishTime = Arrays.stream(state.getFinishTimes()).max().getAsInt();
            System.out.println(finishTime);

            if (this.bestFinishTime > finishTime) {
                this.bestFinishTime = finishTime;
                this.bestState = state;
            }

            return;
        }

        if (closedStates.contains(state)) {
           return;
        }

        closedStates.add(state);

        // For each available node
        for (NodeModel node : getAvailableNodes(state)) {
            // For each processor
            for (int processor = 0; processor < processors; processor++) {
                StateModel nextState = new StateModel(this.processors, this.numberOfNodes);

                System.arraycopy(state.getFinishTimes(), 0, nextState.getFinishTimes(), 0, processors);
                nextState.setScheduledNodes(state.getScheduledNodes().clone());

                int earliestStartTime = getEarliestStartTime(state, node, processor);

                nextState.addNode(node, processor, earliestStartTime);
                getDFSSchedule(nextState, closedStates);
            }
        }

        closedStates.remove(state);
    }

    public StateModel getSchedule() {
        return this.bestState;
    }
}
