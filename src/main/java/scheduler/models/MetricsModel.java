package scheduler.models;

public class MetricsModel {
    private int numberOfOpenedStates;
    private int numberOfClosedStates;

    public MetricsModel() {

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

    public void setNumberOfClosedStates(int numberOfClosedStates) {
        this.numberOfClosedStates = numberOfClosedStates;
    }
}
