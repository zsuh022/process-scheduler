package scheduler.schedulers;

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
        PriorityQueue<StateModel> openedStates = new PriorityQueue<>(Comparator.comparingInt(this::f));
        Set<StateModel> closedStates = new HashSet<>();

        openedStates.add(new StateModel(this.processors, this.numberOfNodes));

        while (!openedStates.isEmpty()) {
            StateModel currentState = openedStates.poll();
            System.out.println(currentState.getMaximumFinishTime());

            if (currentState.areAllNodesScheduled()) {
                return currentState;
            }

            closedStates.add(currentState);

            for (NodeModel node : getAvailableNodes(currentState)) {
                for (int processor = 0; processor < processors; processor++) {
                    StateModel nextState = currentState.clone();

                    int earliestStartTime = getEarliestStartTime(currentState, node, processor);

                    nextState.addNode(node, processor, earliestStartTime);

                    if (!closedStates.contains(nextState)) {
                        openedStates.add(nextState);
                    }
                }
            }
        }

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

    public int getGetIdleTime() {
        int
    }

    // V2
    public int getMaxBottomLevelPathLength(StateModel state) {
        int maxBottomLevelPathLength = 0;

        for (NodeModel node : this.nodes) {
            if (state.isNodeScheduled(node)) {
                int cost = state.getNodeStartTime(node) + bottomLevelPathLengths[node.getByteId()];
                maxBottomLevelPathLength = Math.max(maxBottomLevelPathLength, cost);
            }
        }

        return maxBottomLevelPathLength;
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
