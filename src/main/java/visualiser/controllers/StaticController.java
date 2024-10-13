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

/**
 * The controller for static visualiser.
 * This class is responsible for managing the static visualiser's logic.
 */
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

    /**
     * Sets the arguments for the static visualiser.
     * 
     * @param arguments the arguments for the static visualiser
     */
    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    /**
     * Initialises the static visualiser by loading graphs and labels.
     * 
     * @throws IOException if the input file is not found
     */
    public void initialise() throws IOException {
        initialiseGraph();
        initialiseLabels();
    }

    /**
     * Initialises the graph for the static visualiser.
     * 
     * @throws IOException if the input file is not found
     */
    private void initialiseGraph() throws IOException {
        this.graph = InputOutputParser.readDOTFile(this.arguments.getInputDOTFilePath());

        applyNodeStyle();
        applyEdgeStyle();

        visualiseGraph();
    }

    /**
     * Applies the style to the nodes in the graph.
     */
    private void applyNodeStyle() {
        for (Node node : this.graph) {
            node.setAttribute("ui.label", node.getId());
            node.setAttribute("ui.style", getNodeStyle());
        }
    }

    /**
     * Applies the style to the edges in the graph.
     */
    private void applyEdgeStyle() {
        this.graph.edges().forEach(edge -> {
            edge.setAttribute("ui.style", getEdgeStyle());
        });
    }

    /**
     * Generates and returns the CSS style of the nodes.
     * 
     * @return the node style as a string
     */
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

    /**
     * Returns the CSS styling for the edges of the graph
     * 
     * @return the edge styling as a string
     */
    private String getEdgeStyle() {
        return "size: 1px;" +
                "fill-color: black;" +
                "stroke-mode: plain;" +
                "stroke-color: black;" +
                "stroke-width: 1px;";
    }

    /**
     * Initializes the labels displaying the number of nodes, edges, cores, and processors.
     */
    private void initialiseLabels() {
        initialiseLabel(this.nodesLabel, this.nodesPane, String.valueOf(this.graph.getNodeCount()));
        initialiseLabel(this.edgesLabel, this.edgesPane, String.valueOf(this.graph.getEdgeCount()));
        initialiseLabel(this.coresLabel, this.coresPane, String.valueOf(this.arguments.getCores()));
        initialiseLabel(this.processorsLabel, this.processorsPane, String.valueOf(this.arguments.getProcessors()));
    }


    /**
     * Initializes a label with the given text and centers it in the pane.
     * 
     * @param label the label to initialize
     * @param pane the pane to center the label in
     * @param text the text to display in the label
     */
    private void initialiseLabel(Label label, Pane pane, String text) {
        label.setText(text);

        centerLabel(label, pane);
    }

    /**
     * Centers a label in a pane.
     * 
     * @param label the label to center
     * @param pane the pane to center the label in
     */
    private void centerLabel(Label label, Pane pane) {
        label.layoutXProperty().bind(pane.widthProperty().subtract(label.widthProperty()).divide(2));
        label.layoutYProperty().bind(pane.heightProperty().subtract(label.heightProperty()).divide(2));
    }

    /**
     * Switches the scene to the dynamic visualiser scene.
     * 
     * @throws IOException if the scene can't be loaded
     */
    @FXML
    private void switchToDynamicVisualiser() throws IOException {
        Visualiser.setScene(SceneType.DYNAMIC);
    }

    /**
     * Closes the notification popup of when the task is done
     */
    @FXML
    public void closePopup() {
        DynamicController dynamicController = (DynamicController) Visualiser.getController(SceneType.DYNAMIC);

        if(dynamicController != null) {
            dynamicController.closeCurrentPop();
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
    public void alertFinish() {
        TranslateTransition translate = new TranslateTransition();

        translate.setNode(popup);
        translate.setDuration(Duration.seconds(0.5));
        translate.setByY(-125);

        translate.play();
    }

    /**
     * Visualizes the graph using the GraphStream FxViewer.
     * The graph is displayed in the graph pane and automatically laid out.
     */
    private void visualiseGraph() {
        FxViewer viewer = new FxViewer(this.graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);

        viewer.enableAutoLayout();

        FxViewPanel viewPanel = (FxViewPanel) viewer.addDefaultView(false);

        viewPanel.prefWidthProperty().bind(this.graphPane.widthProperty());
        viewPanel.prefHeightProperty().bind(this.graphPane.heightProperty());

        this.graphPane.getChildren().add(viewPanel);
    }
}
