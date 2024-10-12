package visualiser.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import scheduler.enums.SceneType;
import scheduler.parsers.Arguments;
import scheduler.parsers.InputOutputParser;
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

        // Set graph style
        String css = """
        node {
            shape: circle;
            size: 20px;
            fill-color: rgb(%1$d, %2$d, %3$d); /* Random color */
            text-mode: normal;
            text-background-mode: rounded-box;
            text-background-color: white;
            text-size: 15px;
            text-alignment: center;
            text-color: black;
        }

        edge {
            fill-color: black;
            arrow-shape: arrow;
            arrow-size: 10px, 5px;
        }
        """;

        applyRandomColorsAndLabels(css);

        visualiseGraph();
    }

    private void applyRandomColorsAndLabels(String css) {
        // Add style and label to each node
        for (Node node : this.graph) {
            node.setAttribute("ui.label", node.getId()); // Add ID as label

            // Generate random colors
            int r = (int) (Math.random() * 255);
            int g = (int) (Math.random() * 255);
            int b = (int) (Math.random() * 255);

            // Apply node style with random color
            String nodeStyle = String.format(
                    "shape: circle; size: 30px; fill-color: rgb(%d,%d,%d); " +
                            "stroke-color: black; stroke-width: 2px; " +  // Add border color and width
                            "text-mode: normal; text-size: 15px; text-color: black; text-alignment: center;",
                    r, g, b
            );
            node.setAttribute("ui.style", nodeStyle);

        }
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
    private void showSchedule() throws IOException {
        Visualiser.setScene(SceneType.DYNAMIC);
    }

    private void visualiseGraph() {
        FxViewer viewer = new FxViewer(this.graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();

        FxViewPanel viewPanel = (FxViewPanel) viewer.addDefaultView(false);

        this.graphPane.getChildren().add(viewPanel);
    }
}
