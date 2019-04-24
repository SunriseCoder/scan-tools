package process;

import crop.ImageViewer;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import utils.FileUtils;

public class ScanProcessorApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @FXML
    private Tab markupTab;
    @FXML
    private Tab processingTab;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FileUtils.loadFXML(this);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        ImageViewer imageViewer = new ImageViewer();
        Node imageViewerParent = imageViewer.init();
        markupTab.setContent(imageViewerParent);
        imageViewer.afterStageShown();
    }
}
