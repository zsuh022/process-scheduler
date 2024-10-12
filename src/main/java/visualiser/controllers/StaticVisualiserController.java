package visualiser.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import scheduler.parsers.InputOutputParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class StaticVisualiserController {
    @FXML
    private Label nodesLabel;

    @FXML
    private Label edgesLabel;

    @FXML
    private Label processorsLabel;

    @FXML
    private Label coresLabel;

    @FXML
    private Pane graphPane;

    private String dotFilePath = "src/main/resources/dotfiles/input/Nodes_10_Random.dot";
    private String dotOutputFilePath = "src/main/resources/dotfiles/input/Nodes_10_Random-output.dot";

    @FXML
    public void initialize() {
        try {
            loadDotFile(dotFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        visualizeGraph(dotOutputFilePath);
    }

    @FXML
    private void showSchedule(MouseEvent event) {
        // Your logic for showing the schedule
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
