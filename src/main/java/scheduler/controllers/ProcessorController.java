package scheduler.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;


public class ProcessorController {

    @FXML
    private Canvas canvas;

    private GraphicsContext gc;

    private int processors;

    private int latestLength;
    
    int unitLengths = 50;


    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        processors = 2;
        drawInitial(gc);
        latestLength = 80;
        

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
        length = ((length + 49) / unitLengths) * unitLengths + 80;
        gc.strokeLine(latestLength, 0, length, 0);
        for (int i = 0; i < processors; i++) {
            int y = unitLengths + i * unitLengths;
            gc.strokeLine(latestLength, y, length, y);
        }
        extendTimeAxis(gc, length);


    }
    private void drawTask(GraphicsContext gc, int delay, int length, int processor) {


        int startX = 80 + delay*unitLengths;
        int startY = unitLengths * processor - unitLengths;

        int taskWidth = length*unitLengths;

        int taskHeight = unitLengths;
        int totalLength = startX + taskWidth;
        if (totalLength >= latestLength) {
            canvas.setWidth(totalLength+10);
            extendGrid(gc, totalLength);

        }
        gc.setFill(Color.PURPLE);

        gc.fillRect(startX, startY, taskWidth, taskHeight);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(startX, startY, taskWidth, taskHeight);


    }

    private void extendTimeAxis(GraphicsContext gc, int length) {
        gc.setFill(Color.BLACK);
        gc.setLineWidth(2);
        for (int i = latestLength; i <= length; i += unitLengths) {
            gc.strokeLine(i, processors * unitLengths, i, processors * unitLengths + 10);
            gc.fillText(Integer.toString((i - 80)/unitLengths), i - 3, processors * unitLengths + 25);
        }
        latestLength = length;
    }
}
