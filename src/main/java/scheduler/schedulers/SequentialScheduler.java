package scheduler.schedulers;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import scheduler.models.EdgeModel;
import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;

public class SequentialScheduler extends Scheduler {
    public SequentialScheduler(GraphModel graph, int processors) {
        super(graph, processors);
    }

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
                    openedStates.add(state); // Add the initialized state to OPEN list
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
        int g = Arrays.stream(state.getFinishTimes()).max().getAsInt(); // Current makespan (g(s))
        int h = 0;

        // Heuristic: max bottom-level path length for unscheduled nodes
        for (NodeModel node : this.nodes) {
            if (!state.isNodeScheduled(node)) {
                h = Math.max(h, bottomLevelPathLengths[node.getByteId()]);
            }
        }

        return g + h;
    }

    public int getEarliestStartTime(StateModel state, NodeModel node, int processor) {
        // Get the earliest start time for the current processor
        int earliestStartTime = state.getFinishTime(processor);

        for (NodeModel predecessor : node.getPredecessors()) {
            int finishTime = state.getNodeStartTime(node) + node.getWeight();

            // If we are scheduling on the same processor, we can ignore the communication
            // time, otherwise, we include the communication time, which is the edge weight
            // between predecessor and node
            if (state.getNodeProcessor(predecessor) == processor) {
                earliestStartTime = Math.max(earliestStartTime, finishTime);
            } else {
                EdgeModel edge = getEdge(predecessor, node);
                earliestStartTime = Math.max(earliestStartTime, finishTime + edge.getWeight());
            }
        }

        return earliestStartTime;
    }

    // available nodes all have their predecessors processed and is not visited
    // already
    public List<NodeModel> getAvailableNodes(StateModel state) {
        List<NodeModel> availableNodes = new ArrayList<>();

        for (NodeModel node : this.graph.getNodes().values()) {
            if (!state.isNodeScheduled(node) && arePredecessorsScheduled(state, node)) {
                availableNodes.add(node);
            }
        }

        return availableNodes;
    }

    // Check for the current state, the current task, if its predecessors were
    // scheduled already
    public boolean arePredecessorsScheduled(StateModel state, NodeModel node) {
        for (NodeModel predecessor : node.getPredecessors()) {
            if (!state.isNodeScheduled(predecessor)) {
                return false;
            }
        }

        return true;
    }
}
