package visualiser.controllers;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import visualiser.GanttChart;
import visualiser.Visualiser;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.parsers.Arguments;
import javafx.scene.chart.XYChart;

public class GanttChartController {

    @FXML
    private ScrollPane chartPane;

    private GanttChart<Number, String> ganttChart;

    private Arguments arguments;

    public void initialize() {
        // Create the axes
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time in Seconds");
        CategoryAxis yAxis = new CategoryAxis();
        // Initialize the GanttChart
        ganttChart = new GanttChart<>(xAxis, yAxis);
        ganttChart.setPrefHeight(300);
        ganttChart.setPrefWidth(1280);
        ganttChart.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        ganttChart.setLegendVisible(false);
        // Add the GanttChart to the Pane
        Pane pane = new Pane();
        pane.getChildren().add(ganttChart);
        chartPane.setContent(pane);
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

    @FXML
    void showMetrics(MouseEvent event) throws IOException {
        Visualiser.setScreen("visualiser");  
    }
}
