package app;

import app.context.ApplicationContext;
import app.context.ApplicationParameters;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GraphApp extends Application {
    private static final String APPLICATION_CONTEXT_CONFIG_FILENAME = "graph-tools-config.json";
    private static final String GRAPH_FILENAME = "graph.json";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Application Context
        ApplicationContext applicationContext = new ApplicationContext(APPLICATION_CONTEXT_CONFIG_FILENAME);
        applicationContext.setStage(primaryStage);
        applicationContext.setParameterValue(ApplicationParameters.GraphFileName, GRAPH_FILENAME);

        // Graph Form
        GraphForm graphForm = new GraphForm();
        Parent root = graphForm.init(applicationContext);

        // Main Scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Graph Tools");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
}
