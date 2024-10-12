package visualiser.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.parsers.Arguments;
import scheduler.schedulers.Scheduler;
import scheduler.utilities.Utility;
import visualiser.GanttChart;
import visualiser.Visualiser;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import static scheduler.constants.Constants.*;

public class DynamicController {
    @FXML
    private Button btnStartSchedule;

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

    private int timeElapsed;

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

        applyGanttChartStyleSheet();

        this.ganttChart.setLegendVisible(false);

        Pane pane = new Pane();
        pane.getChildren().add(this.ganttChart);

        this.ganttChartScrollPane.setContent(pane);
    }

    private void applyGanttChartStyleSheet() {
        URL stylesheetUrl = getClass().getResource("/css/style.css");

        if (stylesheetUrl == null) {
            throw new NullPointerException("Stylesheet not found: /css/style.css");
        }

        String stylesheet = stylesheetUrl.toExternalForm();

        this.ganttChart.getStylesheets().add(stylesheet);
    }

    private void initialiseMiscellaneous() {
        this.seriesRam = new XYChart.Series<>();
        this.seriesCpu = new XYChart.Series<>();

        Collections.addAll(this.lineChartRam.getData(), seriesRam);
        Collections.addAll(this.lineChartCpu.getData(), seriesCpu);

        this.lineChartCpu.setAnimated(false);
        this.lineChartCpu.setLegendVisible(false);

        this.lineChartRam.setAnimated(false);
        this.lineChartRam.setLegendVisible(false);

        this.ganttChartTimer = new Timer();
        this.cpuAndRamUsageTimer = new Timer();

        this.timeElapsed = 0;
    }

    @FXML
    void onShowScheduleChartClicked() throws IOException {
        Visualiser.setScene("visualiser");
    }

    @FXML
    void onStartScheduleClicked() {
        btnStartSchedule.setDisable(true);
        btnStartSchedule.setVisible(false);

        startTracking();
    }

    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;

        this.nodes = scheduler.getNodes();
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

        Platform.runLater(() -> plotNewPointsOnCpuAndRamCharts(cpuUsage, ramUsage, timeInSeconds));
    }

    private void plotNewPointsOnCpuAndRamCharts(float cpuUsage, float ramUsage, double timeInSeconds) {
        this.seriesCpu.getData().add(new XYChart.Data<>(String.format("%.1f", timeInSeconds), cpuUsage));
        this.seriesRam.getData().add(new XYChart.Data<>(String.format("%.1f", timeInSeconds), ramUsage));

        this.lblTimeElapsed.setText(String.format("%.1f s", timeInSeconds));

        if (this.seriesCpu.getData().size() > MAXIMUM_NUMBER_OF_DATA_POINTS) {
            this.seriesCpu.getData().remove(0);
        }

        if (this.seriesRam.getData().size() > MAXIMUM_NUMBER_OF_DATA_POINTS) {
            this.seriesRam.getData().remove(0);
        }
    }

    public void addTask(StateModel state, NodeModel node) {
        String id = node.getId();

        int length = node.getWeight();
        int processor = state.getNodeProcessor(node);
        int startTime = state.getNodeStartTime(node);

        XYChart.Series<Number, String> series = new XYChart.Series<>();

        GanttChart.ExtraData extraData = new GanttChart.ExtraData(length, "JONKLERBLOCK", id);

        series.getData().add(new XYChart.Data<>(startTime, "Processor " + processor, extraData));

        this.ganttChart.getData().add(series);
    }

    public void addAllTask() {
        StateModel state = this.scheduler.getCurrentState();

        if (state == null) {
            return;
        }

        for (NodeModel node : this.nodes) {
            if (state.isNodeScheduled(node.getByteId())) {
                addTask(state, node);
            }
        }
    }
}
