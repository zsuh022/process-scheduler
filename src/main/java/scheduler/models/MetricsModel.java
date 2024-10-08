package scheduler.models;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// refactor later, maybe make it a singleton?
public class MetricsModel {
    private StateModel bestState;

    private int numberOfOpenedStates;
    private int numberOfClosedStates;

    private double elapsedTime;
    private double memoryUsed;

    private List<Double> cpuUsage;
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * Constructor for MetricsModel class.
     */
    public MetricsModel() {
        this.numberOfOpenedStates = 0;
        this.numberOfClosedStates = 0;
        this.cpuUsage = new ArrayList<>();
    }

    /**
     * Method returns the number of opened states.
     *
     * @return the number of opened states
     */
    public int getNumberOfOpenedStates() {
        return this.numberOfOpenedStates;
    }

    /**
     * Method sets the number of opened states.
     *
     * @param numberOfOpenedStates the number of opened states to set
     */
    public void setNumberOfOpenedStates(int numberOfOpenedStates) {
        this.numberOfOpenedStates = numberOfOpenedStates;
    }

    /**
     * Method returns the number of closed states.
     *
     * @return the number of closed states
     */
    public int getNumberOfClosedStates() {
        return this.numberOfClosedStates;
    }

    /**
     * Method increments the number of opened states by one.
     */
    public void incrementNumberOfOpenedStates() {
        ++this.numberOfOpenedStates;
    }

    /**
     * Method sets the number of closed states.
     *
     * @param numberOfClosedStates the number of closed states to set
     */
    public void setNumberOfClosedStates(int numberOfClosedStates) {
        this.numberOfClosedStates = numberOfClosedStates;
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
    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /**
     * Method returns the memory used in bytes.
     *
     * @return the memory used
     */
    public double getMemoryUsed() {
        return this.memoryUsed;
    }

    /**
     * Method sets the memory used in bytes.
     *
     * @param memoryUsed the memory used
     */
    public void setMemoryUsed(double memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public List<Double> getPeriodicCpuUsage() {
        return this.cpuUsage;
    }

    public void startPeriodicTracking(long interval) {
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        this.scheduledExecutorService.scheduleAtFixedRate(this::capturePeriodicMetrics, 0, interval, TimeUnit.MILLISECONDS);
    }

    public void stopPeriodicTracking() {
        if (this.scheduledExecutorService != null && !this.scheduledExecutorService.isShutdown()) {
            this.scheduledExecutorService.shutdown();
        }
    }

    private void capturePeriodicMetrics() {
        double cpuUsage = getCurrentCpuLoad();
        this.cpuUsage.add(cpuUsage);
    }

    private double getCurrentCpuLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getSystemCpuLoad() * 100;
        }
        return -1;
    }

    /**
     * Method displays the metrics recorded such as elapsed time, number of states, best schedule finish
     * time and memory used.
     */
    public void display() {
        System.out.println("\nMetrics:");
        System.out.printf("  %-25s %ss%n", "Elapsed time in seconds:", elapsedTime);
        System.out.printf("  %-25s %d%n", "Number of opened states:", this.numberOfOpenedStates);
        System.out.printf("  %-25s %d%n", "Number of closed states:", this.numberOfClosedStates);
        System.out.printf("  %-25s %d%n", "Schedule finish time:", this.bestState.getMaximumFinishTime());
        System.out.printf("  %-25s %.3fMB%n", "Memory used in MB:", this.memoryUsed / (1024 * 1024));
        displayPeriodicMetrics();
    }

    private void displayPeriodicMetrics() {
        System.out.println("\nPeriodic CPU Usage:");

        for (int i = 0; i < cpuUsage.size(); i++) {
            System.out.printf("  Interval %d - CPU: %.3f%%%n", i + 1, cpuUsage.get(i));
        }
    }
}
