package crop;

import javafx.application.Application;
import javafx.stage.Stage;

public class ImageViewerApp extends Application {
    private static String filename;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Use: " + ImageViewerApp.class.getName() + " <filename>");
            System.exit(-1);
        }

        filename = args[0];

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ImageViewer imageViewer = new ImageViewer();
        imageViewer.start(primaryStage, filename);
    }
}
