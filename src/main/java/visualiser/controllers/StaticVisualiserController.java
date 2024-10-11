package visualiser.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
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

    private String dotFilePath = "../dotfiles/input/Nodes_10_Random-output.dot";  // Set the path to your DOT file
    private String dotOutputFilePath = "src/main/resources/dotfiles/input/Nodes_10_Random-output.dot";  // Set the path to your processed DOT file

    @FXML
    public void initialize() {
        try {
            loadDotFile(dotFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        visualizeGraph(dotOutputFilePath);
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
        stats.put("processors", 2); // Example: 2 processors (can be extracted dynamically)
        stats.put("cores", 4); // Example: 4 cores (can be extracted dynamically)

        // Update labels
        nodesLabel.setText("Nodes: " + stats.get("nodes"));
        edgesLabel.setText("Edges: " + stats.get("edges"));
        processorsLabel.setText("Processors: " + stats.get("processors"));
        coresLabel.setText("Cores: " + stats.get("cores"));
    }

    private void visualizeGraph(String dotOutputFilePath) {
        WebView webView = new WebView();
        graphPane.getChildren().add(webView);

        // Assuming you're using an external tool like Graphviz to render the DOT graph as an SVG
        String graphSvgPath = convertDotToSvg(dotOutputFilePath);
        webView.getEngine().load("file:///" + graphSvgPath);
    }

    private String convertDotToSvg(String dotOutputFilePath) {
        // Logic to convert the DOT file to an SVG image using Graphviz or similar
        // This is just a placeholder method. You can implement Graphviz or any other visualization tool here.
        return "path/to/generated/svg";
    }
}
