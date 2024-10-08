package visualiser;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import scheduler.parsers.Arguments;
import visualiser.controllers.ProcessorController;

public class Visualiser extends Application {
    private static Arguments arguments;
    private static Scene scene;

    public static void run(Arguments arguments) {
        Visualiser.arguments = arguments;
        launch();
    }
    private static Parent loadFxml(final String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(Visualiser.class.getResource("/fxml/" + fxml + ".fxml"));
        Parent root = loader.load();
        
        if (fxml.equals("processor")){
            ProcessorController controller = loader.getController();
            controller.setArguments(arguments);
        }
        return root;
    }

    public static void setScreen(String fxml) throws IOException{
        scene.setRoot(loadFxml(fxml));
    }

    @Override
    public void start(Stage stage) throws IOException {
        // String javaVersion = System.getProperty("java.version");
        // String javafxVersion = System.getProperty("javafx.version");
        // Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        //Scene scene = new Scene(loadFxml("visualiser"), 1280, 720);
        //above is for milestone 2
        //Scene scene = new Scene(new StackPane(l), 640, 480);
        
        scene = new Scene(loadFxml("visualiser"), 1280, 720);
        // Used for milestone 2
        stage.setScene(scene);
        stage.show();
    }
}
