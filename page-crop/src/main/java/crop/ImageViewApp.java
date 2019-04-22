package crop;

import javafx.application.Application;
import javafx.stage.Stage;

public class ImageViewApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ImageViewer imageViewer = new ImageViewer();
        imageViewer.start(primaryStage);
    }
}
