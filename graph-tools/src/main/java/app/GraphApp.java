package app;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;

import app.context.ApplicationContext;
import dto.Graph;
import dto.GraphHistory;
import dto.Point;
import dto.Vertex;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import utils.FileUtils;
import utils.JSONUtils;

public class GraphApp extends Application {
    private static final String APPLICATION_CONTEXT_CONFIG_FILENAME = "graph-tools-config.json";
    private static final String GRAPH_FILENAME = "graph.json";

    public static void main(String[] args) {
        launch(args);
    }

    private ApplicationContext applicationContext;

    // General
    @FXML
    private Pane imagePane;
    @FXML
    private ImageView imageView;

    private File graphFile;
    private GraphHistory graphHistory;
    private Graph graph;

    public GraphApp() {
        graphFile = new File(GRAPH_FILENAME);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Application Context
        applicationContext = new ApplicationContext(APPLICATION_CONTEXT_CONFIG_FILENAME);
        applicationContext.setStage(primaryStage);

        // Root UI Node
        Parent root = FileUtils.loadFXML(this);

        imagePane.setOnMouseClicked(e -> handleMouseClicked(e));

        // Main Scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Graph Tools");
        primaryStage.setMaximized(true);
        primaryStage.show();

        restoreGraph();
    }

    private void restoreGraph() {
        try {
            TypeReference<GraphHistory> typeReference = new TypeReference<GraphHistory>() {};
            graphHistory = JSONUtils.loadFromDisk(graphFile, typeReference );
        } catch (IOException e) {
            graphHistory = new GraphHistory();
        }

        graph = graphHistory.getCurrentGraph();

        renderGraph();
    }

    private void handleMouseClicked(MouseEvent e) {
        Vertex vertex = new Vertex();
        vertex.setPosition(e.getSceneX(), e.getSceneY());

        graph.addVertex(vertex);
        updateGraphHistory();

        saveGraph();
        renderGraph();
    }

    private void updateGraphHistory() {
        graph = graphHistory.createNewVersion();
    }

    private void renderGraph() {
        for (Vertex vertex : graph.getVertices()) {
            Point position = vertex.getPosition();

            Rectangle rectange = new Rectangle(100, 50, null);
            rectange.setStroke(Color.AQUA);
            rectange.setX(position.x);
            rectange.setY(position.y);

            imagePane.getChildren().add(rectange);
        }
    }

    private void saveGraph() {
        try {
            JSONUtils.saveToDisk(graphHistory, graphFile);
        } catch (IOException e) {
            applicationContext.showError("Error by Saving Graph", e);
        }
    }
}
