package scheduler;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import scheduler.models.GraphModel;
import scheduler.parser.InputOutputParser;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        Scene scene = new Scene(new StackPane(l), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws IOException {
        launch();

        GraphModel graph = new GraphModel("src/main/resources/dotfiles/input/Nodes_10_Random.dot");
        InputOutputParser.outputDOTFile(graph, "src/main/resources/dotfiles/output/Nodes_10_Random_Output.dot");
    }
}
