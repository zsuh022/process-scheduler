package scheduler.controllers;

import java.io.IOException;
import java.util.Map;

import org.graphstream.graph.Graph;

import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.parsers.Arguments;


public class ProcessorController {

    @FXML
    private Canvas canvas;

    private GraphicsContext gc;

    private int processors;

    private int latestLength;

    private Arguments arguments;
    
    int unitLengths = 25;


    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        processors = 2;
        latestLength = 80;
    }

    @FXML
    public void drawAllTasks() throws IOException {
        String tasks = arguments.getOutputDOTFilePath();
        GraphModel graphModel = new GraphModel(tasks);
        for (NodeModel node : graphModel.getNodes().values()) {
            System.out.println(node.getId() + " " + node.getStartTime() + " " + node.getWeight() + " " + node.getProcessor());
            drawTask(gc, node.getStartTime(), node.getWeight(), node.getProcessor(), node.getId());
        }
    }

    public void setArguments(Arguments args) {
        this.arguments = args;
        processors = args.getProcessors();
        drawInitial(gc);
    }

    private void drawInitial(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(80, 1, 80, unitLengths*processors+1);
        for (int i = 2; i <= processors; i++) {
            gc.strokeLine(unitLengths, unitLengths*i-unitLengths, 80, unitLengths*i-unitLengths);
        }
        for (int i = 0; i < processors; i++) {
            int y = unitLengths + i * unitLengths;
            gc.fillText("Processor " + (i+1), 10, y-((double) unitLengths /2) );
        }
    }

    private void extendGrid(GraphicsContext gc, int length) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        length = ((length + (unitLengths-1)) / unitLengths) * unitLengths;
        gc.strokeLine(latestLength, 0, length, 0);
        for (int i = 0; i < processors; i++) {
            int y = unitLengths + i * unitLengths;
            gc.strokeLine(latestLength, y, length, y);
        }
        extendTimeAxis(gc, length);
    }

    private void drawTask(GraphicsContext gc, int delay, int length, int processor, String id) {
        int startX = 80 + delay*unitLengths;
        int startY = unitLengths * processor - unitLengths;

        int taskWidth = length*unitLengths;

        int taskHeight = unitLengths;
        int totalLength = startX + taskWidth;
        if (totalLength >= latestLength) {
            canvas.setWidth(totalLength+unitLengths*2);
            extendGrid(gc, totalLength);

        }
        gc.setFill(Color.PURPLE);

        gc.fillRect(startX, startY, taskWidth, taskHeight);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(startX, startY, taskWidth, taskHeight);

        String taskNumber = id; 
        gc.setFill(Color.LIGHTGREEN); 
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(taskNumber, startX + taskWidth / 2, startY + taskHeight / 2);
    }

    private void extendTimeAxis(GraphicsContext gc, int length) {
        gc.setFill(Color.BLACK);
        gc.setLineWidth(2);
        for (int i = latestLength; i <= length; i += unitLengths) {
            gc.strokeLine(i, processors * unitLengths, i, processors * unitLengths + 10);
            gc.fillText(Integer.toString((i - 80)/unitLengths), i - 3, processors * unitLengths + 25);
        }
        this.latestLength = length;
    }
}
