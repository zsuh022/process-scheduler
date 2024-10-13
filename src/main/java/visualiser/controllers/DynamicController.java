package visualiser.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
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
import javafx.util.Duration;
import scheduler.enums.SceneType;
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

/**
 * The controller for the dynamic visualiser.
 * This class is responsible for managing the dynamic visualiser's logic.
 */
public class DynamicController {
    @FXML
    private Button btnStartSchedule;

    @FXML
    private Label lblTimeElapsed;

    @FXML
    private Label lblFinishTime;

    @FXML
    private LineChart<String, Number> lineChartRam;

    @FXML
    private LineChart<String, Number> lineChartCpu;

    @FXML
    private ScrollPane ganttChartScrollPane;

    @FXML
    private Pane popup;

    private XYChart.Series<String, Number> seriesRam;
    private XYChart.Series<String, Number> seriesCpu;

    private GanttChart<Number, String> ganttChart;

    private Arguments arguments;

    private Scheduler scheduler;

    private NodeModel[] nodes;

    private Timer ganttChartTimer;
    private Timer cpuAndRamUsageTimer;

    private int timeElapsed;

    /**
     * Initialises the dynamic visualiser.
     */
    @FXML
    public void initialize() {
        initialiseGanttChart();
        initialiseMiscellaneous();
    }

    /**
     * Initialises the Gantt chart.
     * The Gantt chart is used to visualise the schedule.
     */
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

    /**
     * Applies the custom stylesheet to the Gantt chart for styling
     */
    private void applyGanttChartStyleSheet() {
        URL stylesheetUrl = getClass().getResource("/css/style.css");

        if (stylesheetUrl == null) {
            throw new NullPointerException("Stylesheet not found: /css/style.css");
        }

        String stylesheet = stylesheetUrl.toExternalForm();

        this.ganttChart.getStylesheets().add(stylesheet);
    }

    /**
     * Initialises miscellaneous components.
     * These miscellaneous components are the graphs and timers for cpu and ram usage.
     */
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

    /**
     * Switches to the static visualiser.
     * @throws IOException if the fxml file cannot be loaded
     */
    @FXML
    void switchToStaticVisualiser() throws IOException {
        Visualiser.setScene(SceneType.STATIC);
    }

    /**
     * Handles the start schedule button click event.
     * The button is disabled and the starts the tracking when clicked.
     */
    @FXML
    void onStartScheduleClicked() {
        btnStartSchedule.setDisable(true);
        btnStartSchedule.setVisible(false);

        startTracking();
    }

    /**
     * Sets the arguments for the dynamic visualiser.
     * 
     * @param arguments the arguments
     */
    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    /**
     * Sets the scheduler for the dynamic visualiser.
     * 
     * @param scheduler the scheduler
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;

        this.nodes = scheduler.getNodes();
    }

    /**
     * Starts tracking the schedule.
     */
    private void startTracking() {
        Task<Void> schedulingTask = getVoidTask();

        new Thread(schedulingTask).start();

        startGanttChartTimer();
        startCpuAndRamUsageTimer();
    }

    /**
     * Starts the Gantt chart timer.
     */
    private void startGanttChartTimer() {
        this.ganttChartTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateGanttChart());
            }
        }, 0, GANTT_CHART_UPDATE_INTERVAL);
    }

    /**
     * Starts the CPU and RAM usage timer.
     */
    private void startCpuAndRamUsageTimer() {
        this.cpuAndRamUsageTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateCpuAndRamUsageCharts();
            }
        }, 0, CPU_AND_RAM_UPDATE_INTERVAL);
    }

    /**
     * Creates and returns a scheduling task
     * 
     * @return the scheduling task
     */
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
               updateElementsUponScheduleCompletion();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return schedulingTask;
    }

    /**
     * Updates all the elements on schedule completion
     */
    private void updateElementsUponScheduleCompletion() throws IOException {
        updateGanttChart();

        this.scheduler.saveBestState(this.arguments);

        this.ganttChartTimer.cancel();
        this.cpuAndRamUsageTimer.cancel();

        StaticController staticController = (StaticController) Visualiser.getController(SceneType.STATIC);

        if (staticController != null) {
            staticController.alertFinish();
        }

        this.alertFinish();
    }

    /**
     * Updates the Gantt chart with all tasks.
     */
    private void updateGanttChart() {
        this.ganttChart.clear();

        addAllTask();
    }

    /**
     * Closes the notification popup of when the task is done
     */
    @FXML
    public void closePopup() {
        StaticController staticController = (StaticController) Visualiser.getController(SceneType.STATIC);

        if (staticController != null) {
            staticController.closeCurrentPop();
        }

        closeCurrentPop();
    }


    /**
     * Closes the popup in the current scene
     */
    public void closeCurrentPop(){
        FadeTransition fade = new FadeTransition();

        fade.setNode(popup);
        fade.setDuration(Duration.seconds(0.5));
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(event -> popup.setDisable(true));

        fade.play();
    }

    /**
     * Alerts the user that the task is done through a popup
     */
    private void alertFinish() {
        TranslateTransition translate = new TranslateTransition();

        translate.setNode(popup);
        translate.setDuration(Duration.seconds(0.5));
        translate.setByY(-125);

        translate.play();
    }

    /**
     * Updates the CPU and RAM usage charts with new data points.
     */
    private void updateCpuAndRamUsageCharts() {
        float cpuUsage = Utility.getCpuUsage();
        float ramUsage = Utility.getRamUsage();

        double timeInSeconds = this.timeElapsed / 1000.0;

        Platform.runLater(() -> plotNewPointsOnCpuAndRamCharts(cpuUsage, ramUsage, timeInSeconds));

        this.timeElapsed += CPU_AND_RAM_UPDATE_INTERVAL;
    }

    private void plotNewPointsOnCpuAndRamCharts(float cpuUsage, float ramUsage, double timeInSeconds) {
        this.seriesCpu.getData().add(new XYChart.Data<>(String.format("%.1f", timeInSeconds), cpuUsage));
        this.seriesRam.getData().add(new XYChart.Data<>(String.format("%.1f", timeInSeconds), ramUsage));

        this.lblTimeElapsed.setText(String.format("%.1f", timeInSeconds));

        if (this.seriesCpu.getData().size() > MAXIMUM_NUMBER_OF_DATA_POINTS) {
            this.seriesCpu.getData().remove(0);
        }

        if (this.seriesRam.getData().size() > MAXIMUM_NUMBER_OF_DATA_POINTS) {
            this.seriesRam.getData().remove(0);
        }
    }

    /**
     * Adds a task to the Gantt chart.
     * 
     * @param state the state
     * @param node the node
     */
    public void addTask(StateModel state, NodeModel node) {
        String id = node.getId();

        int length = node.getWeight();
        int processor = state.getNodeProcessor(node);
        int startTime = state.getNodeStartTime(node);

        XYChart.Series<Number, String> series = new XYChart.Series<>();

        GanttChart.ExtraData extraData = new GanttChart.ExtraData(length, "JONKLERBLOCK", id);

        series.getData().add(new XYChart.Data<>(startTime, "Processor " + (processor+1), extraData));

        this.ganttChart.getData().add(series);
    }

    /**
     * Adds all tasks to the Gantt chart.
     */
    public void addAllTask() {
        StateModel state = this.scheduler.getCurrentState();
        
        if (state == null) {
            return;
        }

        this.lblFinishTime.setText(String.valueOf(state.getMaximumFinishTime()));

        for (NodeModel node : this.nodes) {
            if (state.isNodeScheduled(node.getByteId())) {
                addTask(state, node);
            }
        }
    }
}
