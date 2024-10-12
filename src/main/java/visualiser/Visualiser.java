package visualiser;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import scheduler.models.MetricsModel;
import scheduler.parsers.Arguments;
import scheduler.schedulers.Scheduler;
import visualiser.controllers.DynamicController;

import static scheduler.constants.Constants.WINDOW_HEIGHT;
import static scheduler.constants.Constants.WINDOW_WIDTH;

public class Visualiser extends Application {
    private static Scene scene;

    private static Arguments arguments;

    private static Scheduler scheduler;

    public static void run(Arguments arguments, Scheduler scheduler) {
        Visualiser.arguments = arguments;
        Visualiser.scheduler = scheduler;

        launch();
    }
    private static Parent loadFxml(final String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(Visualiser.class.getResource("/fxml/" + fxml + ".fxml"));
        Parent root = loader.load();


        if (fxml.equals("dynamic")) {
            DynamicController controller = loader.getController();

            controller.setArguments(arguments);
            controller.setScheduler(scheduler);
        } else if (fxml.equals("visualiser")) {

        }

        return root;
    }

    public static void setScene(String fxml) throws IOException{
        scene.setRoot(loadFxml(fxml));
    }

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFxml("dynamic"), WINDOW_WIDTH, WINDOW_HEIGHT);

        stage.setScene(scene);
        stage.show();
    }
}
