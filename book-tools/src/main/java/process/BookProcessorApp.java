package process;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import process.context.ApplicationContext;
import process.forms.ContentTreeForm;
import process.forms.EditorForm;
import utils.FileUtils;

public class BookProcessorApp extends Application {
    private static final String APPLICATION_CONTEXT_CONFIG_FILENAME = "book-processor-config.json";

    public static void main(String[] args) {
        launch(args);
    }

    private ApplicationContext applicationContext;

    @FXML
    private SplitPane splitPane;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Application Context
        applicationContext = new ApplicationContext(APPLICATION_CONTEXT_CONFIG_FILENAME);
        applicationContext.setStage(primaryStage);

        // Root UI Node
        Parent root = FileUtils.loadFXML(this);

        // ContentTreeForm
        ContentTreeForm contentTreeForm = new ContentTreeForm();
        Node contentTreeFormNode = contentTreeForm.createUI(applicationContext);
        splitPane.getItems().add(contentTreeFormNode);

        // EditorForm
        EditorForm editorForm = new EditorForm();
        Node editorFormNode = editorForm.createUI(applicationContext);
        splitPane.getItems().add(editorFormNode);

        // Main Scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Book Processor");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
}
