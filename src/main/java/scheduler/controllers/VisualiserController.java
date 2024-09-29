package scheduler.controllers;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import scheduler.visualiser.Visualiser;

public class VisualiserController {
    @FXML
    private Label lblProcessTime;

    @FXML
    private Label lblProcessorsUsed;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblTimeElapsed;

    @FXML
    void showSchedule(MouseEvent event) throws IOException{
        Visualiser.setScreen("processor");
    }
}
