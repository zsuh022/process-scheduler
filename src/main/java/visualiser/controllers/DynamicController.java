package visualiser.controllers;

import javafx.application.Platform;
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
import scheduler.models.StateModel;
import scheduler.parsers.Arguments;
import scheduler.schedulers.Scheduler;
import visualiser.GanttChart;
import visualiser.Visualiser;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class DynamicController {

    private static final int PERIODIC_INTERVAL_MS = 500;

    @FXML
    private Label lblTimeElapsed;

    @FXML
    private LineChart<String, Number> lineChartRam;

    @FXML
    private LineChart<String, Number> lineChartCpu;

    @FXML
    private ScrollPane chartPane;

    private XYChart.Series<String, Number> seriesRam;
    private XYChart.Series<String, Number> seriesCpu;

    private GanttChart<Number, String> ganttChart;

    private Arguments arguments;
    private Scheduler scheduler;
    private NodeModel[] nodes;

    private MetricsModel metricsModel;

    private Timer timer;
    private int timeElapsed = 0;

    @FXML
    public void initialize() {
        seriesRam = new XYChart.Series<>();
        seriesCpu = new XYChart.Series<>();

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

//        startTracking();
    }

    @FXML
    void showMetrics(MouseEvent event) throws IOException {
        Visualiser.setScene("visualiser");
    }

    public void setArguments(Arguments arguments) throws IOException {
        this.arguments = arguments;
//        addAllTask();
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.nodes = scheduler.getNodes();
        startTracking();
    }

    public void setMetricsModel(MetricsModel metricsModel) {
        this.metricsModel = metricsModel;
        this.metricsModel.startPeriodicTracking(PERIODIC_INTERVAL_MS);
//        startTracking();
    }

    private void startTracking() {
        this.scheduler.schedule();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateLineCharts();
            }
        }, 0, PERIODIC_INTERVAL_MS);
    }

    private void updateLineCharts() {
        int index = metricsModel.getPeriodicCpuUsage().size() - 1;
        if (index >= 0) {
            float currentCpuUsage = metricsModel.getPeriodicCpuUsage().get(index);
            float currentRamUsage = metricsModel.getPeriodicRamUsage().get(index);

            timeElapsed += PERIODIC_INTERVAL_MS;

            Platform.runLater(() -> {
                seriesCpu.getData().add(new XYChart.Data<>(String.valueOf(timeElapsed), currentCpuUsage));
                seriesRam.getData().add(new XYChart.Data<>(String.valueOf(timeElapsed), currentRamUsage));

                lblTimeElapsed.setText(String.valueOf(timeElapsed));

                // number of data points visible
                if (seriesCpu.getData().size() > 20) {
                    seriesCpu.getData().remove(0);
                }
                if (seriesRam.getData().size() > 20) {
                    seriesRam.getData().remove(0);
                }

                addAllTask();
            });
        }
    }

    public void addTask(int processor, int startTime, int length, String taskName) {
        // Create a new series for the task
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        // Add the task to the series
        series.getData().add(new XYChart.Data<>(startTime, "Processor " + processor, new GanttChart.ExtraData(length, "JONKLERBLOCK", taskName)));
        // Add the series to the Gantt chart
        ganttChart.getData().add(series);
    }

//    public void addAllTask() throws IOException {
//        String tasks = arguments.getOutputDOTFilePath();
//        GraphModel graphModel = new GraphModel(tasks);
//        for (NodeModel node : graphModel.getNodes().values()) {
//            addTask(node.getProcessor(), node.getStartTime(), node.getWeight(), node.getId());
//        }
//    }

    public void addAllTask() {
        StateModel currentState = this.scheduler.getCurrentState();

        if (currentState == null) {
            return;
        }

        for (byte nodeByteId : currentState.getScheduledNodes()) {
            NodeModel node = nodes[nodeByteId];
            System.out.println(node.getId());
            addTask(currentState.getNodeProcessor(node), currentState.getNodeStartTime(node), node.getWeight(), node.getId());
        }
    }
}
