package visualiser.controllers;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import scheduler.parsers.Arguments;
import scheduler.models.MetricsModel;
import visualiser.Visualiser;

public class VisualiserController {
    @FXML
    private Label lblProcessTime;

    @FXML
    private Label lblProcessorsUsed;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblTimeElapsed;

    private Arguments arguments;

    private int processors;

    private MetricsModel metrics;

    @FXML
    void showSchedule(MouseEvent event) throws IOException{
        Visualiser.setScreen("processor");
    }

    public void setArguments(Arguments args) {
        this.arguments = args;
        processors = args.getProcessors();
    }

    public void setMetrics(MetricsModel metrics) {
        this.metrics = metrics;
        updateMetricsDisplay();
    }

    private void updateMetricsDisplay() {
        lblProcessTime.setText("asads");
        if (metrics != null) {
            lblProcessTime.setText(String.valueOf(metrics.getBestState().getMaximumFinishTime()));
            lblProcessorsUsed.setText(String.valueOf(processors));
            lblTimeElapsed.setText(String.format("%.3f", metrics.getElapsedTime()));
            lblStatus.setText("Completed");
        } else {
            lblProcessTime.setText("N/A");
            lblProcessorsUsed.setText("N/A");
            lblTimeElapsed.setText("N/A");
            lblStatus.setText("Metrics not available");
        }
    }
}
