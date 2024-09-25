package scheduler.schedulers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;

public class SequentialScheduler extends Scheduler {

    public SequentialScheduler(GraphModel graph, int processors) {
        super(graph, processors);
    }

    public StateModel getAStarSchedule() {
        // 1. create opened list of partial schedules
        PriorityQueue<StateModel> openedStates = new PriorityQueue<>(Comparator.comparingInt(state -> f(state)));
        Set<StateModel> closedStates = new HashSet<>();

        // Add empty initial states
        openedStates.add(new StateModel(this.processors, this.graph.getNodes().size()));

        while (!openedStates.isEmpty()) {
            StateModel currentState = openedStates.poll();

            // Check if all tasks have been scheduled
            if (currentState.areAllNodesScheduled()) {
                return currentState;
            }

            // Close the current state (visited)
            closedStates.add(currentState);

            // Expand states by checking each available node for each processor
            for (NodeModel node : getAvailableNodes(currentState)) {
                for (int processor = 0; processor < processors; processor++) {
                    StateModel nextState = new StateModel(this.processors, this.numberOfNodes);

                    // Create new state with copy

                    // Schedule the task on this processor

                    // Find the earliest finish time

                    if (closedStates.contains(nextState)) {
                        openedStates.add(nextState);
                    }
                }
            }
        }

        // No schedule found
        return null;
    }

    public int f(StateModel state) {
        int g = state.getFinishTime();
        int h = 0;

        for (NodeModel node : this.nodes) {
            if (!state.isNodeScheduled(node)) {
                h = Math.max(h, bottomLevelPathLengths[node.getByteId()]);
            }
        }

        return g + h;
    }

    // available nodes all have their predecessors processed and is not visited
    // already
    public List<NodeModel> getAvailableNodes(StateModel state) {
        List<NodeModel> availableNodes = new ArrayList<>();

        for (NodeModel node : this.graph.getNodes().values()) {
            if (state.isNodeScheduled(node) && arePredecessorsScheduled(state, node)) {
                availableNodes.add(node);
            }
        }

        return availableNodes;
    }

    // Check if the current state, the node if its predecessors were scheduled
    // already
    public boolean arePredecessorsScheduled(StateModel state, NodeModel node) {
        for (NodeModel predecessor : node.getPredecessors()) {
            if (!state.isNodeScheduled(predecessor)) {
                return false;
            }
        }

        return true;
    }

}
