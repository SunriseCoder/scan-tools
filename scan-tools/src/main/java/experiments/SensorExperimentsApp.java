package experiments;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class SensorExperimentsApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private Pane imagePane;

    @Override
    public void start(Stage primaryStage) throws Exception {
        imagePane = new Pane();

        imagePane.addEventHandler(TouchEvent.ANY, e -> {
            if (e.getEventType().equals(TouchEvent.TOUCH_STATIONARY)) {
                return;
            }
            System.out.println(e);
        });

        Scene scene = new Scene(imagePane);
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
