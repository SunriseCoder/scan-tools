package crop;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ImageViewApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ImageViewer imageViewer = new ImageViewer();
        Parent root = imageViewer.init();
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        imageViewer.afterStageShown();
    }
}
