package scheduler.models;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The MetricsModel class contains all the necessary information pertaining the scheduler itself. It contains metrics
 * for the number of opened and closed states, as well as the real time elapsed to compute the optimal schedule.
 */
public class MetricsModel {
    private StateModel bestState;

    private final AtomicInteger numberOfOpenedStates;
    private final AtomicInteger numberOfClosedStates;

    private float elapsedTime;

    /**
     * Constructor for MetricsModel class.
     */
    public MetricsModel() {
        this.numberOfOpenedStates = new AtomicInteger(0);
        this.numberOfClosedStates = new AtomicInteger(0);
    }

    /**
     * Method returns the number of opened states.
     *
     * @return the number of opened states
     */
    public int getNumberOfOpenedStates() {
        return this.numberOfOpenedStates.get();
    }

    /**
     * Method returns the number of closed states.
     *
     * @return the number of closed states
     */
    public int getNumberOfClosedStates() {
        return this.numberOfClosedStates.get();
    }

    /**
     * Method increments the number of opened states by one.
     */
    public void incrementNumberOfOpenedStates() {
        this.numberOfOpenedStates.getAndIncrement();
    }

    /**
     * Method sets the number of closed states.
     *
     * @param numberOfClosedStates the number of closed states to set
     */
    public void setNumberOfClosedStates(int numberOfClosedStates) {
        this.numberOfClosedStates.set(numberOfClosedStates);
    }

    /**
     * Method returns the best schedule state.
     *
     * @return the best state
     */
    public StateModel getBestState() {
        return this.bestState;
    }

    /**
     * Method sets the best schedule state.
     *
     * @param bestState the best state to set
     */
    public void setBestState(StateModel bestState) {
        this.bestState = bestState;
    }

    /**
     * Method returns the elapsed time in seconds.
     *
     * @return the elapsed time
     */
    public double getElapsedTime() {
        return this.elapsedTime;
    }

    /**
     * Method sets the elapsed time in seconds.
     *
     * @param elapsedTime the elapsed time
     */
    public void setElapsedTime(float elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /**
     * Method displays the metrics recorded such as elapsed time, number of states, best schedule finish
     * time and memory used.
     */
    public void display() {
        System.out.println("\nMetrics:");
        System.out.printf("  %-25s %.3fs%n", "Elapsed time in seconds:", elapsedTime);
        System.out.printf("  %-25s %d%n", "Number of opened states:", this.numberOfOpenedStates.get());
        System.out.printf("  %-25s %d%n", "Number of closed states:", this.numberOfClosedStates.get());
        System.out.printf("  %-25s %d%n", "Schedule finish time:", this.bestState.getMaximumFinishTime());
    }
}
