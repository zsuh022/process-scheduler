package visualiser;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import scheduler.enums.SceneType;
import scheduler.parsers.Arguments;
import scheduler.schedulers.Scheduler;
import visualiser.controllers.DynamicController;
import visualiser.controllers.StaticController;

import static scheduler.constants.Constants.WINDOW_HEIGHT;
import static scheduler.constants.Constants.WINDOW_WIDTH;

public class Visualiser extends Application {
    private static Scene scene;

    private static Arguments arguments;

    private static Scheduler scheduler;

    private static Map<SceneType, Parent> scenes;

    public static void run(Arguments arguments, Scheduler scheduler) {
        Visualiser.arguments = arguments;
        Visualiser.scheduler = scheduler;

        launch();
    }

    private static String getResource(SceneType sceneType) {
        String filename = sceneType.toString().toLowerCase();

        return "/fxml/" + filename + ".fxml";
    }

    private static Parent loadFxml(SceneType sceneType) throws IOException {
        if (scenes.containsKey(sceneType)) {
            return scenes.get(sceneType);
        }

        String resource = getResource(sceneType);

        FXMLLoader loader = new FXMLLoader(Visualiser.class.getResource(resource));

        Parent root = loader.load();

        if (sceneType == SceneType.DYNAMIC) {
            DynamicController controller = loader.getController();

            controller.setArguments(arguments);
            controller.setScheduler(scheduler);
        } else {
            StaticController controller = loader.getController();

            controller.setArguments(arguments);

            controller.initialise();
        }

        scenes.put(sceneType, root);

        return root;
    }

    public static void setScene(SceneType sceneType) throws IOException{
        scene.setRoot(loadFxml(sceneType));
    }

    @Override
    public void start(Stage stage) throws IOException {
        scenes = new EnumMap<>(SceneType.class);

        scene = new Scene(loadFxml(SceneType.DYNAMIC), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene = new Scene(loadFxml("dynamic"), 1280, 720);
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.setScene(scene);
        stage.show();
    }
}
