package visualiser.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import scheduler.models.MetricsModel;

public class DynamicController {

    @FXML
    private Label lblTimeElapsed;

    @FXML
    private LineChart<String, Number> lineChartRam;

    @FXML
    private LineChart<String, Number> lineChartCpu;

    private XYChart.Series<String, Number> seriesRam = new XYChart.Series<>();
    private XYChart.Series<String, Number> seriesCpu = new XYChart.Series<>();

    private MetricsModel metricsModel;

    @FXML
    public void initialize() {
        lineChartRam.getData().addAll(seriesRam);
        lineChartCpu.getData().addAll(seriesCpu);
    }
}
