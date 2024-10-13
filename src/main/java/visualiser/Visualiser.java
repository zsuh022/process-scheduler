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

/**
 * The main class for visualiser.
 * This class is responsible for the managing the scenes used in visualiser.
 */
public class Visualiser extends Application {
    private static Scene scene;

    private static Arguments arguments;

    private static Scheduler scheduler;

    private static Map<SceneType, Parent> scenes;

    private static Map<SceneType, Object> controllers;

    /**
     * Runs the visualiser.
     * @param arguments arguments passed to the visualiser made of input and output files and processors and cores.
     * @param scheduler the scheduling algorithm responsible for managing schedule logic.
     */
    public static void run(Arguments arguments, Scheduler scheduler) {
        Visualiser.arguments = arguments;
        Visualiser.scheduler = scheduler;

        launch();
    }

    /**
     * Gets the resource for the scene.
     * @param sceneType the scene's enum
     * @return the resource for the scene
     */
    private static String getResource(SceneType sceneType) {
        String filename = sceneType.toString().toLowerCase();

        return "/fxml/" + filename + ".fxml";
    }

    /**
     * Loads the fxml file for the scene. If not found it creates a new one.
     * @param sceneType the scene's enum
     * @return the parent node of the scene
     * @throws IOException if the fxml file cannot be loaded
     */
    private static Parent loadFxml(SceneType sceneType) throws IOException {
        if (scenes.containsKey(sceneType)) {
            return scenes.get(sceneType);
        }

        String resource = getResource(sceneType);

        FXMLLoader loader = new FXMLLoader(Visualiser.class.getResource(resource));

        Parent root = loader.load();

        if (sceneType == SceneType.DYNAMIC) {
            DynamicController controller = loader.getController();
            controllers.put(sceneType,controller);

            controller.setArguments(arguments);
            controller.setScheduler(scheduler);
        } else {
            StaticController controller = loader.getController();
            controllers.put(sceneType,controller);

            controller.setArguments(arguments);

            controller.initialise();
        }

        scenes.put(sceneType, root);

        return root;
    }

    /**
     * Sets the scene for the visualiser.
     * @param sceneType the scene's enum
     * @throws IOException if the fxml file cannot be loaded
     */
    public static void setScene(SceneType sceneType) throws IOException{
        scene.setRoot(loadFxml(sceneType));
    }

    /**
     * Gets the controller for the scene.
     * @param scene the scene's enum
     * @return the controller for the scene
     */
    public static Object getController(SceneType scene) {
        if (controllers.containsKey(scene)) {
          return controllers.get(scene);
        } else {
          return null;
        }
      }

    /**
     * Starts the visualiser.
     * It initializes the scenes, sets up the window, and displays it.
     * @param stage the stage for the visualiser
     * @throws IOException if the fxml file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        scenes = new EnumMap<>(SceneType.class);
        controllers = new EnumMap<>(SceneType.class);

        scene = new Scene(loadFxml(SceneType.DYNAMIC), WINDOW_WIDTH, WINDOW_HEIGHT);
        loadFxml(SceneType.STATIC);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.setScene(scene);
        stage.show();
    }
}
