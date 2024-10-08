package visualiser.controllers;

import java.io.IOException;
import java.util.Map;

import javafx.scene.control.Button;
import org.graphstream.graph.Graph;

import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.parsers.Arguments;
import visualiser.Visualiser;
import visualiser.MusicPlayer;


public class ProcessorController {

    @FXML
    private Canvas canvas;

    @FXML
    private Button musicButton;

    private GraphicsContext gc;

    private int processors;

    private int latestLength;

    private Arguments arguments;
    
    int unitLengths = 25;

    private MusicPlayer musicPlayer;


    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        processors = 2;
        latestLength = 130;

        this.musicPlayer = MusicPlayer.getInstance();
    }

    @FXML
    public void drawAllTasks() throws IOException {
        String tasks = arguments.getOutputDOTFilePath();
        GraphModel graphModel = new GraphModel(tasks);
        for (NodeModel node : graphModel.getNodes().values()) {
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
        gc.strokeLine(130, 1, 130, 2*unitLengths*processors+1);
        gc.setFontSmoothingType(FontSmoothingType.LCD);
        gc.setFill(Color.web("#777777"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 16));

        for (int i = 2; i <= processors; i++) {
            gc.strokeLine(130, 2*unitLengths*i-unitLengths, 130, unitLengths*i-unitLengths);
        }
        for (int i = 0; i < processors; i++) {
            int y = (i+1)*unitLengths*2 - unitLengths;
            gc.fillText("PROCESSOR " + (i+1), 10, y-((double) unitLengths / 2) + 18 );
        }
    }

    private void extendGrid(GraphicsContext gc, int length) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        length = ((length + (unitLengths-1)) / unitLengths) * unitLengths;
        gc.strokeLine(latestLength, 0, length, 0);
        for (int i = 0; i < processors; i++) {
            int y = 2*unitLengths + 2*i * unitLengths;
            gc.strokeLine(latestLength, y, length, y);
        }
        extendTimeAxis(gc, length);
    }

    private void drawTask(GraphicsContext gc, int delay, int length, int processor, String id) {
        int startX = 130 + delay*unitLengths;
        int startY = unitLengths * processor;

        int taskWidth = length*unitLengths;

        int taskHeight = unitLengths*2;
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
        gc.setTextBaseline(VPos.CENTER);
        for (int i = latestLength; i <= length; i += unitLengths) {
            gc.strokeLine(i, processors * unitLengths * 2, i, processors * unitLengths * 2 + 10);
            int offset = -5;
            if ((i - 130)/unitLengths>=10){
                offset = -9;
            }
            gc.fillText(Integer.toString((i - 130)/unitLengths), i + offset, processors * unitLengths * 2 + 23);
            latestLength = i;
        }
        latestLength += unitLengths;
    }

    @FXML
    public void playMusic() {
        this.musicPlayer.play();
    }

    @FXML
    void showMetrics(MouseEvent event) throws IOException{
        Visualiser.setScreen("visualiser");
    }
}
