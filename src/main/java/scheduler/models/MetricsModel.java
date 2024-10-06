package scheduler.models;

// refactor later, maybe make it a singleton?
public class MetricsModel {
    private StateModel bestState;

    private int numberOfOpenedStates;
    private int numberOfClosedStates;

    private double elapsedTime;
    private double memoryUsed;

    public MetricsModel() {
        this.numberOfOpenedStates = 0;
        this.numberOfClosedStates = 0;
    }

    public int getNumberOfOpenedStates() {
        return this.numberOfOpenedStates;
    }

    public void setNumberOfOpenedStates(int numberOfOpenedStates) {
        this.numberOfOpenedStates = numberOfOpenedStates;
    }

    public int getNumberOfClosedStates() {
        return this.numberOfClosedStates;
    }

    public void incrementNumberOfOpenedStates() {
        ++this.numberOfOpenedStates;
    }

    public void setNumberOfClosedStates(int numberOfClosedStates) {
        this.numberOfClosedStates = numberOfClosedStates;
    }

    public StateModel getBestState() {
        return this.bestState;
    }

    public void setBestState(StateModel bestState) {
        this.bestState = bestState;
    }

    public double getElapsedTime() {
        return this.elapsedTime;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public double getMemoryUsed() {
        return this.memoryUsed;
    }

    public void setMemoryUsed(double memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public void display() {
        System.out.println("Metrics:");
        System.out.printf("  %-25s %ss%n", "Elapsed time in seconds:", elapsedTime);
        System.out.printf("  %-25s %d%n", "Number of opened states:", this.numberOfOpenedStates);
        System.out.printf("  %-25s %d%n", "Number of closed states:", this.numberOfClosedStates);
        System.out.printf("  %-25s %d%n", "Schedule finish time:", this.bestState.getMaximumFinishTime());
        System.out.printf("  %-25s %.3fMB%n", "Memory used in MB:", this.memoryUsed / (1024 * 1024));
    }
}
