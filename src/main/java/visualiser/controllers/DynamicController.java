package visualiser.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import scheduler.models.GraphModel;
import scheduler.models.MetricsModel;
import scheduler.models.NodeModel;
import scheduler.parsers.Arguments;
import visualiser.GanttChart;
import visualiser.Visualiser;

import java.io.IOException;

public class DynamicController {

    @FXML
    private Label lblTimeElapsed;

    @FXML
    private LineChart<String, Number> lineChartRam;

    @FXML
    private LineChart<String, Number> lineChartCpu;

    @FXML
    private ScrollPane chartPane;

    private XYChart.Series<String, Number> seriesRam = new XYChart.Series<>();
    private XYChart.Series<String, Number> seriesCpu = new XYChart.Series<>();

    private GanttChart<Number, String> ganttChart;

    private Arguments arguments;

    private MetricsModel metricsModel;

    @FXML
    public void initialize() {
        lineChartRam.getData().addAll(seriesRam);
        lineChartCpu.getData().addAll(seriesCpu);

        // Create the axes
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time in Seconds");
        CategoryAxis yAxis = new CategoryAxis();
        // Initialize the GanttChart
        ganttChart = new GanttChart<>(xAxis, yAxis);
        ganttChart.setPrefHeight(270);
        ganttChart.setPrefWidth(1180);
        ganttChart.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        ganttChart.setLegendVisible(false);
        // Add the GanttChart to the Pane
        Pane pane = new Pane();
        pane.getChildren().add(ganttChart);
        chartPane.setContent(pane);
    }

    @FXML
    void showMetrics(MouseEvent event) throws IOException {
        Visualiser.setScreen("visualiser");
    }

    public void setArguments(Arguments arguments) throws IOException {
        this.arguments = arguments;
        addAllTask();
    }

    public void addTask(int processor, int startTime, int length, String taskName) {
        // Create a new series for the task
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        // Add the task to the series
        series.getData().add(new XYChart.Data<>(startTime, "Processor " + processor, new GanttChart.ExtraData(length, "JONKLERBLOCK", taskName)));
        // Add the series to the Gantt chart
        ganttChart.getData().add(series);
    }

    public void addAllTask() throws IOException {
        String tasks = arguments.getOutputDOTFilePath();
        GraphModel graphModel = new GraphModel(tasks);
        for (NodeModel node : graphModel.getNodes().values()) {
            addTask(node.getProcessor(), node.getStartTime(), node.getWeight(), node.getId());
        }
    }
}
