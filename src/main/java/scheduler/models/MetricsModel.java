package scheduler.models;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
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

    private float memoryUsed;
    private float elapsedTime;

    private final List<Float> cpuUsage;
    private final List<Float> ramUsage;

    private ScheduledExecutorService scheduledExecutorService;

    /**
     * Constructor for MetricsModel class.
     */
    public MetricsModel() {
        this.numberOfOpenedStates = new AtomicInteger(0);
        this.numberOfClosedStates = new AtomicInteger(0);

        this.cpuUsage = new ArrayList<>();
        this.ramUsage = new ArrayList<>();
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
    public void setElapsedTime(float elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /**
     * Method returns the memory used in bytes.
     *
     * @return the memory used
     */
    public float getMemoryUsed() {
        return this.memoryUsed;
    }

    /**
     * Method sets the memory used in bytes.
     *
     * @param memoryUsed the memory used
     */
    public void setMemoryUsed(float memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public List<Float> getPeriodicCpuUsage() {
        return this.cpuUsage;
    }

    public List<Float> getPeriodicRamUsage() {
        return this.ramUsage;
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
        this.cpuUsage.add(getCurrentCpuLoad());
        this.ramUsage.add(getCurrentRamUsage());
    }

    private float getCurrentCpuLoad() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        return (osBean == null) ? -1.0f : (float) (osBean.getCpuLoad() * 100.0);
    }

    private float getCurrentRamUsage() {
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        return (float) memoryUsed / 1024 / 1024;
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
        System.out.printf("  %-25s %.3fMB%n", "Memory used in MB:", getMegaBytesUsed());
        displayPeriodicMetrics();
    }

    private double getMegaBytesUsed() {
        return this.memoryUsed / 1024 / 1024;
    }

    private void displayPeriodicMetrics() {
        System.out.println("\nPeriodic CPU Usage:");

        for (int i = 0; i < this.cpuUsage.size(); i++) {
            System.out.printf("  Interval %d - RAM: %.3fMB, CPU: %.3f%%%n", i + 1, this.ramUsage.get(i), this.cpuUsage.get(i));
        }
    }
}
