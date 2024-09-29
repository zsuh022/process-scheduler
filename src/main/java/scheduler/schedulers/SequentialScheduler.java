package scheduler.schedulers;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;

public class SequentialScheduler extends Scheduler {
    public SequentialScheduler(GraphModel graph, int processors) {
        super(graph, processors);
    }

    @Override
    public StateModel getAStarSchedule() {
        // 1. create opened list of partial schedules
        PriorityQueue<StateModel> openedStates = new PriorityQueue<>(Comparator.comparingInt(this::f));
        Set<StateModel> closedStates = new HashSet<>();

        // 2. Add initial states
        for (NodeModel node : this.nodes) {
            if (node.getPredecessors().size() == 0) { // This is a root node
                for (int i = 0; i < this.processors; i++) {
                    StateModel state = new StateModel(this.processors, this.numberOfNodes);
                    state.addNode(node, i, 0); // Schedule root node on processor i
                    openedStates.add(state); // Add the initialised state to OPEN list
                }
            }
        }

        // 3. While there are still unexplored states
        while (!openedStates.isEmpty()) {
            StateModel currentState = openedStates.poll();
            System.out.println(Arrays.stream(currentState.getFinishTimes()).max().getAsInt());

            // 4. Naive pruning, where we exit once all nodes are scheduled
            if (currentState.areAllNodesScheduled()) {
                return currentState;
            }

            // 5. Close the current state (visited)
            closedStates.add(currentState);

            // 6. Expand states by checking available nodes for each processor
            for (NodeModel node : getAvailableNodes(currentState)) {
                for (int processor = 0; processor < processors; processor++) {
                    StateModel nextState = new StateModel(this.processors, this.numberOfNodes);

                    // Copy over the current state's details
                    System.arraycopy(currentState.getFinishTimes(), 0, nextState.getFinishTimes(), 0, processors);
                    nextState.setScheduledNodes(currentState.getScheduledNodes().clone());

                    // Set the earliest start time for this task on this processor
                    int earliestStartTime = getEarliestStartTime(currentState, node, processor);

                    // Schedule the task on the next state
                    nextState.addNode(node, processor, earliestStartTime);

                    if (!closedStates.contains(nextState)) {
                        openedStates.add(nextState);
                    }
                }
            }
        }

        // No schedule found which is impossible because a valid schedule should be
        // generated
        return null;
    }

    public int f(StateModel state) {
//        int g = Arrays.stream(state.getFinishTimes()).max().getAsInt();
//        int h = 0;
//
//        // Heuristic: max bottom-level path length for unscheduled nodes
//        for (NodeModel node : this.nodes) {
//            if (!state.isNodeScheduled(node)) {
//                h = Math.max(h, bottomLevelPathLengths[node.getByteId()]);
//            }
//        }
//
//        return g + h;
        int fbl = fBL(state);
        int fdrt = fDRT(state);

        return Math.max(fbl, fdrt);
    }

    public int fDRT(StateModel state) {
        int maxDRT = 0;

        for (NodeModel node : getAvailableNodes(state)) {
            int earliestStartTime = getEarliestStartTime(state, node);
            maxDRT = Math.max(maxDRT, earliestStartTime + bottomLevelPathLengths[node.getByteId()]);
        }

        return maxDRT;
    }

    public int fBL(StateModel state) {
        int maxBL = 0;

        for (byte nodeId : state.getScheduledNodes()) {
            NodeModel node = this.nodes[nodeId];
            int earliestStartTime = getEarliestStartTime(state, node);
            maxBL = Math.max(maxBL, earliestStartTime + bottomLevelPathLengths[nodeId]);
        }

        return maxBL;
    }

    public int getEarliestStartTime(StateModel state, NodeModel node) {
        int earliestStartTime = Integer.MAX_VALUE;

        for (int processor = 0; processor < processors; processor++) {
            int est = getEarliestStartTime(state, node, processor);
            earliestStartTime = Math.min(est, earliestStartTime);
        }

        return earliestStartTime;
    }
}
