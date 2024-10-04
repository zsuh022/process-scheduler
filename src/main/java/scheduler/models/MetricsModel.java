package scheduler.models;

// refactor later, maybe make it a singleton?
public class MetricsModel {
    private int numberOfOpenedStates;
    private int numberOfClosedStates;

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
}
