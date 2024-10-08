package scheduler.models;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// refactor later, maybe make it a singleton?
public class MetricsModel {
    private StateModel bestState;

    private final AtomicInteger numberOfOpenedStates;

    private final AtomicInteger numberOfClosedStates;


    private double memoryUsed;
    private double elapsedTime;

    private List<Double> cpuUsage;

    private ScheduledExecutorService scheduledExecutorService;

    /**
     * Constructor for MetricsModel class.
     */
    public MetricsModel() {
        this.numberOfOpenedStates = new AtomicInteger(0);
        this.numberOfClosedStates = new AtomicInteger(0);

        this.cpuUsage = new ArrayList<>();
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

    public void incrementNumberOfClosedStates() {
        this.numberOfClosedStates.getAndIncrement();
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
        double currentCpuUsage = getCurrentCpuLoad();

        // if CPU usage is NaN
        if (Double.isNaN(currentCpuUsage)) {
            System.out.println("**** NaN ****");
            if (!this.cpuUsage.isEmpty()) {
                // use previously recorded value
                currentCpuUsage = this.cpuUsage.get(this.cpuUsage.size() - 1);
            } else {
                // use 0.0%
                currentCpuUsage = 0.0;
            }
        }

        this.cpuUsage.add(currentCpuUsage);
    }

    private double getCurrentCpuLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getCpuLoad() * 100;
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
        System.out.printf("  %-25s %d%n", "Number of opened states:", this.numberOfOpenedStates.get());
        System.out.printf("  %-25s %d%n", "Number of closed states:", this.numberOfClosedStates.get());
        System.out.printf("  %-25s %d%n", "Schedule finish time:", this.bestState.getMaximumFinishTime());
        System.out.printf("  %-25s %.3fMB%n", "Memory used in MB:", getMegaBytesUsed());
        displayPeriodicMetrics();
    }

    private double getMegaBytesUsed() {
        return this.memoryUsed / 1024 / 1024;
    }

    private void displayPeriodicMetrics() {
        System.out.println("\nPeriodic CPU Usage:");

        for (int i = 0; i < this.cpuUsage.size(); i++) {
            System.out.printf("  CPU Usage at interval %d: %.3f%%%n", i + 1, this.cpuUsage.get(i));
        }
    }
}
