package scheduler.visualiser;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import scheduler.parsers.Arguments;

public class Visualiser extends Application {
    private static Arguments arguments;

    public static void run(Arguments arguments) {
        launch();
    }

    //for milestone 2
    // private static Parent loadFxml(final String fxml) throws IOException {
    // return new FXMLLoader(Visualiser.class.getResource("/fxml/" + fxml + ".fxml")).load();
    // }

    @Override
    public void start(Stage stage) throws IOException {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        //Scene scene = new Scene(loadFxml("visualiser"), 1280, 720);
        //above is for milestone 2
        Scene scene = new Scene(new StackPane(l), 640, 480);
        stage.setScene(scene);
        stage.show();
    }
}
