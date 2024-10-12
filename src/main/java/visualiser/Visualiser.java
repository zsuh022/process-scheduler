package visualiser;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import scheduler.models.MetricsModel;
import scheduler.parsers.Arguments;
import scheduler.schedulers.Scheduler;
import visualiser.controllers.DynamicController;
import visualiser.controllers.GanttChartController;
import visualiser.controllers.ProcessorController;
import visualiser.controllers.VisualiserController;

public class Visualiser extends Application {
    private static Arguments arguments;
    private static Scene scene;
    private static Scheduler scheduler;
    public static void run(Arguments arguments, Scheduler scheduler){
        Visualiser.arguments = arguments;
        Visualiser.scheduler = scheduler;
        launch();
    }
    private static Parent loadFxml(final String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(Visualiser.class.getResource("/fxml/" + fxml + ".fxml"));
        Parent root = loader.load();
        
        if (fxml.equals("dynamic")) {
            DynamicController controller = loader.getController();
            MetricsModel metricsModel = new MetricsModel();
            controller.setArguments(arguments);
            controller.setScheduler(scheduler);
        } else if (fxml.equals("visualiser")){
            VisualiserController controller = loader.getController();
            controller.setArguments(arguments);
            MetricsModel metrics = scheduler.getMetrics();
            controller.setMetrics(metrics);
            //MetricsModel metrics = scheduler.getMetrics();
//            controller.setMetrics(metrics);
        } else if (fxml.equals("processor")){
            ProcessorController controller = loader.getController();
            controller.setArguments(arguments);
        }
        return root;
    }

    public static void setScene(String fxml) throws IOException{
        scene.setRoot(loadFxml(fxml));
    }

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFxml("dynamic"), 1280, 720);
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0); 
        });

        stage.setScene(scene);
        stage.show();
    }
}
