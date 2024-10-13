package visualiser.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import scheduler.enums.SceneType;
import scheduler.parsers.Arguments;
import scheduler.parsers.InputOutputParser;
import scheduler.utilities.Utility;
import visualiser.Visualiser;

import java.io.IOException;

public class StaticController {
    @FXML
    private Label nodesLabel;

    @FXML
    private Label edgesLabel;

    @FXML
    private Label coresLabel;

    @FXML
    private Pane popup;

    @FXML
    private Label processorsLabel;

    @FXML
    private Pane graphPane;

    @FXML
    private Pane nodesPane;

    @FXML
    private Pane edgesPane;

    @FXML
    private Pane coresPane;

    @FXML
    private Pane processorsPane;

    private Arguments arguments;

    private Graph graph;

    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    public void initialise() throws IOException {
        initialiseGraph();
        initialiseLabels();
    }

    private void initialiseGraph() throws IOException {
        this.graph = InputOutputParser.readDOTFile(this.arguments.getInputDOTFilePath());

        applyNodeStyle();
        applyEdgeStyle();

        visualiseGraph();
    }

    private void applyNodeStyle() {
        for (Node node : this.graph) {
            node.setAttribute("ui.label", node.getId());
            node.setAttribute("ui.style", getNodeStyle());
        }
    }

    private void applyEdgeStyle() {
        this.graph.edges().forEach(edge -> {
            edge.setAttribute("ui.style", getEdgeStyle());
        });
    }

    private String getNodeStyle() {
        int[] rgb = Utility.getRandomRgb();

        return String.format(
                "shape: circle;" +
                "size: 30px;" +
                "fill-color: rgb(%d,%d,%d);" +
                "stroke-mode: plain;" +
                "stroke-color: black;" +
                "stroke-width: 1px;" +
                "text-mode: normal;" +
                "text-size: 14px;" +
                "text-color: black;" +
                "text-alignment: center;",
                rgb[0], rgb[1], rgb[2]
        );
    }

    private String getEdgeStyle() {
        return "size: 1px;" +
                "fill-color: black;" +
                "stroke-mode: plain;" +
                "stroke-color: black;" +
                "stroke-width: 1px;";
    }

    private void initialiseLabels() {
        initialiseLabel(this.nodesLabel, this.nodesPane, String.valueOf(this.graph.getNodeCount()));
        initialiseLabel(this.edgesLabel, this.edgesPane, String.valueOf(this.graph.getEdgeCount()));
        initialiseLabel(this.coresLabel, this.coresPane, String.valueOf(this.arguments.getCores()));
        initialiseLabel(this.processorsLabel, this.processorsPane, String.valueOf(this.arguments.getProcessors()));
    }

    private void initialiseLabel(Label label, Pane pane, String text) {
        label.setText(text);

        centerLabel(label, pane);
    }

    private void centerLabel(Label label, Pane pane) {
        label.layoutXProperty().bind(pane.widthProperty().subtract(label.widthProperty()).divide(2));
        label.layoutYProperty().bind(pane.heightProperty().subtract(label.heightProperty()).divide(2));
    }

    @FXML
    private void switchToDynamicVisualiser() throws IOException {
        Visualiser.setScene(SceneType.DYNAMIC);
    }

    @FXML
    public void closePopup() {
        DynamicController dynamicController = (DynamicController) Visualiser.getController(SceneType.DYNAMIC);

        if(dynamicController != null) {
            dynamicController.closeCurrentPop();
        }

        closeCurrentPop();
    }
    public void closeCurrentPop(){
        FadeTransition fade = new FadeTransition();

        fade.setNode(popup);
        fade.setDuration(Duration.seconds(0.5));
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(event -> popup.setDisable(true));

        fade.play();
    }

    public void alertFinish() {
        TranslateTransition translate = new TranslateTransition();

        translate.setNode(popup);
        translate.setDuration(Duration.seconds(0.5));
        translate.setByY(-125);

        translate.play();
    }

    private void visualiseGraph() {
        FxViewer viewer = new FxViewer(this.graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);

        viewer.enableAutoLayout();

        FxViewPanel viewPanel = (FxViewPanel) viewer.addDefaultView(false);

        viewPanel.prefWidthProperty().bind(this.graphPane.widthProperty());
        viewPanel.prefHeightProperty().bind(this.graphPane.heightProperty());

        this.graphPane.getChildren().add(viewPanel);
    }
}
