package visualiser.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import scheduler.models.MetricsModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.parsers.Arguments;
import scheduler.schedulers.Scheduler;
import scheduler.utilities.Utility;
import visualiser.GanttChart;
import visualiser.Visualiser;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static scheduler.constants.Constants.*;

public class DynamicController {
    @FXML
    private Label lblTimeElapsed;

    @FXML
    private LineChart<String, Number> lineChartRam;

    @FXML
    private LineChart<String, Number> lineChartCpu;

    @FXML
    private ScrollPane ganttChartScrollPane;

    private XYChart.Series<String, Number> seriesRam;
    private XYChart.Series<String, Number> seriesCpu;

    private GanttChart<Number, String> ganttChart;

    private Arguments arguments;

    private Scheduler scheduler;

    private NodeModel[] nodes;

    private Timer ganttChartTimer;
    private Timer cpuAndRamUsageTimer;

    private int timeElapsed = 0;

    @FXML
    public void initialize() {
        initialiseGanttChart();
        initialiseMiscellaneous();
    }

    private void initialiseGanttChart() {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");

        this.ganttChart = new GanttChart<>(xAxis, new CategoryAxis());

        this.ganttChart.setPrefHeight(GANTT_CHART_WIDTH);
        this.ganttChart.setPrefWidth(GANTT_CHART_HEIGHT);
        this.ganttChart.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        this.ganttChart.setLegendVisible(false);

        Pane pane = new Pane();
        pane.getChildren().add(this.ganttChart);

        this.ganttChartScrollPane.setContent(pane);
    }

    private void initialiseMiscellaneous() {
        this.seriesRam = new XYChart.Series<>();
        this.seriesCpu = new XYChart.Series<>();

        this.lineChartRam.getData().addAll(seriesRam);
        this.lineChartCpu.getData().addAll(seriesCpu);

        this.lineChartCpu.setAnimated(false);
        this.lineChartCpu.setLegendVisible(false);

        this.lineChartRam.setAnimated(false);
        this.lineChartRam.setLegendVisible(false);

        this.ganttChartTimer = new Timer();
        this.cpuAndRamUsageTimer = new Timer();
    }

    @FXML
    void showMetrics(MouseEvent event) throws IOException {
        Visualiser.setScene("visualiser");
    }

    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;

        this.nodes = scheduler.getNodes();

        startTracking();
    }

    private void startTracking() {
        Task<Void> schedulingTask = getVoidTask();

        new Thread(schedulingTask).start();

        startGanttChartTimer();
        startCpuAndRamUsageTimer();
    }

    private void startGanttChartTimer() {
        this.ganttChartTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    updateGanttChart();
                });
            }
        }, 0, GANTT_CHART_UPDATE_INTERVAL);
    }

    private void startCpuAndRamUsageTimer() {
        this.cpuAndRamUsageTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateCpuAndRamUsageCharts();
            }
        }, 0, CPU_AND_RAM_UPDATE_INTERVAL);
    }

    private Task<Void> getVoidTask() {
        Task<Void> schedulingTask = new Task<>() {
            @Override
            protected Void call() {
                scheduler.schedule();

                return null;
            }
        };

        schedulingTask.setOnSucceeded(event -> {
            try {
                updateGanttChart();

                this.scheduler.saveBestState(this.arguments);

                this.ganttChartTimer.cancel();
                this.cpuAndRamUsageTimer.cancel();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return schedulingTask;
    }

    private void updateGanttChart() {
        this.ganttChart.clear();

        addAllTask();
    }

    private void updateCpuAndRamUsageCharts() {
        float cpuUsage = Utility.getCpuUsage();
        float ramUsage = Utility.getRamUsage(); 

        this.timeElapsed += CPU_AND_RAM_UPDATE_INTERVAL;

        double timeInSeconds = this.timeElapsed / 1000.0;

        Platform.runLater(() -> {
            plotNewPointsOnCpuAndRamCharts(cpuUsage, ramUsage, timeInSeconds);
        });
    }

    private void plotNewPointsOnCpuAndRamCharts(float cpuUsage, float ramUsage, double timeInSeconds) {
        seriesCpu.getData().add(new XYChart.Data<>(String.format("%.1f", timeInSeconds), cpuUsage));
        seriesRam.getData().add(new XYChart.Data<>(String.format("%.1f", timeInSeconds), ramUsage));

        lblTimeElapsed.setText(String.format("%.1f s", timeInSeconds)); // Display time in seconds

        if (seriesCpu.getData().size() > 10) {
            seriesCpu.getData().remove(0);
        }

        if (seriesRam.getData().size() > 10) {
            seriesRam.getData().remove(0);
        }
    }

    public void addTask(int processor, int startTime, int length, String taskName) {
        XYChart.Series<Number, String> series = new XYChart.Series<>();

        series.getData().add(new XYChart.Data<>(startTime, "Processor " + processor, new GanttChart.ExtraData(length, "JONKLERBLOCK", taskName)));

        ganttChart.getData().add(series);
    }

    public void addAllTask() {
        StateModel currentState = this.scheduler.getCurrentState();

        if (currentState == null) {
            return;
        }

        for (NodeModel node : nodes) {
            if (currentState.isNodeScheduled(node.getByteId())) {
                addTask(currentState.getNodeProcessor(node), currentState.getNodeStartTime(node), node.getWeight(), node.getId());
            }
        }
    }
}
