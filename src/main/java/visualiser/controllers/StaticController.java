package visualiser.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import scheduler.enums.SceneType;
import scheduler.models.GraphModel;
import scheduler.parsers.Arguments;
import scheduler.parsers.InputOutputParser;
import visualiser.Visualiser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

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

    private void loadDotFile(String filePath) throws IOException {
        HashMap<String, Integer> stats = new HashMap<>();
        int nodeCount = 0;
        int edgeCount = 0;

        // Read the DOT file to extract information
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("->")) {
                    edgeCount++; // Counting edges
                } else if (line.contains("[Weight=")) {
                    nodeCount++; // Counting nodes
                }
            }
        }

        stats.put("nodes", nodeCount);
        stats.put("edges", edgeCount);
        stats.put("processors", 2);
        stats.put("cores", 4);

        // Update labels
        nodesLabel.setText("Nodes: " + stats.get("nodes"));
        edgesLabel.setText("Edges: " + stats.get("edges"));
        processorsLabel.setText("Processors: " + stats.get("processors"));
        coresLabel.setText("Cores: " + stats.get("cores"));
    }

    private void visualizeGraph(String dotFilePath) {
        try {

            Graph graph = InputOutputParser.readDOTFile(dotFilePath);

            FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
            viewer.enableAutoLayout();
    

            FxViewPanel viewPanel = (FxViewPanel) viewer.addDefaultView(false);
    
            // Add the viewPanel directly to the JavaFX Pane
            graphPane.getChildren().add(viewPanel);
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
