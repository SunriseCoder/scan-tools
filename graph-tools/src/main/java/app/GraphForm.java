package app;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;

import app.context.ApplicationContext;
import app.context.ApplicationParameters;
import dto.Graph;
import dto.GraphHistory;
import dto.Point;
import dto.Vertex;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.FileUtils;
import utils.JSONUtils;

public class GraphForm {
    private ApplicationContext applicationContext;

    // General
    @FXML
    private Pane imagePane;
    @FXML
    private ImageView imageView;

    private File graphFile;
    private GraphHistory graphHistory;
    private Graph graph;

    private Vertex selectedVertex;
    private double lastPosX;
    private double lastPosY;

    private double scale = 1;

    public Parent init(ApplicationContext applicationContext) throws IOException {
        String graphFileName = applicationContext.getParameterValue(ApplicationParameters.GraphFileName);
        graphFile = new File(graphFileName);

        Parent root = FileUtils.loadFXML(this);

        imagePane.setOnMousePressed(e -> handleMousePressed(e));
        imagePane.setOnMouseDragged(e -> handleMouseDrag(e));
        imagePane.setOnMouseDragExited(e -> handleMouseDragExited(e));

        imagePane.setOnKeyPressed(e -> handleKeyPressed(e));

        restoreGraph();

        return root;
    }

    private void restoreGraph() {
        try {
            TypeReference<GraphHistory> typeReference = new TypeReference<GraphHistory>() {
            };
            graphHistory = JSONUtils.loadFromDisk(graphFile, typeReference);
        } catch (IOException e) {
            graphHistory = new GraphHistory();
        }

        graph = graphHistory.getCurrentGraph();

        renderGraph();
    }

    private void handleMousePressed(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            selectVertex(e.getSceneX(), e.getSceneY());
        }

        saveLastMousePosition(e.getSceneX(), e.getSceneY());
    }

    private void selectVertex(double x, double y) {
        double posOnImageX = getImageCoordinateX(x);
        double posOnImageY = getImageCoordinateY(y);
        Vertex vertex = findVertex(posOnImageX, posOnImageY);

        if (vertex == null) {
            addVertex(posOnImageX, posOnImageY);
        } else {
            selectedVertex = vertex;
        }

        renderGraph();
    }

    private void addVertex(double x, double y) {
        Vertex vertex;
        vertex = new Vertex();
        vertex.setPosition(x, y);
        vertex.setSize(100, 50);
        graph.addVertex(vertex);
        updateGraphHistory();
    }

    private void saveLastMousePosition(double x, double y) {
        lastPosX = x;
        lastPosY = y;
    }

    private void handleMouseDrag(MouseEvent e) {
        double currentPosX = e.getSceneX();
        double currentPosY = e.getSceneY();
        if (e.isPrimaryButtonDown()) {
            if (selectedVertex != null) {
                handleVertexDrag(currentPosX, currentPosY);
            }
        } else if (e.isSecondaryButtonDown()) {
            handleImageDrag(currentPosX, currentPosY);
        }

        saveLastMousePosition(e.getSceneX(), e.getSceneY());
    }

    private void handleVertexDrag(double currentPosX, double currentPosY) {
        double deltaX = currentPosX - lastPosX;
        double deltaY = currentPosY - lastPosY;

        Point vertexOldPosition = selectedVertex.getPosition();
        selectedVertex.setPosition(vertexOldPosition.x + deltaX, vertexOldPosition.y + deltaY);

        renderGraph();
    }

    private void handleImageDrag(double currentPosX, double currentPosY) {
        double deltaX = currentPosX - lastPosX;
        double deltaY = currentPosY - lastPosY;

        imagePane.setTranslateX(imagePane.getTranslateX() + deltaX);
        imagePane.setTranslateY(imagePane.getTranslateY() + deltaY);
    }

    private void handleMouseDragExited(MouseDragEvent e) {
            updateGraphHistory();
    }

    private double getImageCoordinateX(double screenCoordinate) {
        double sceneOffset = imagePane.getBoundsInParent().getMinX();
        double rectangleOffset = imagePane.getBoundsInLocal().getMinX();
        double result = screenCoordinate / scale - sceneOffset / scale + rectangleOffset;
        return result;
    }

    private double getImageCoordinateY(double screenCoordinate) {
        double sceneOffset = imagePane.getBoundsInParent().getMinY();
        double rectangleOffset = imagePane.getBoundsInLocal().getMinY();
        double result = screenCoordinate / scale - sceneOffset / scale + rectangleOffset;
        return result;
    }

    private void handleKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
        case DELETE:
            if (selectedVertex != null) {
                deleteVertex(selectedVertex);
            }
            break;
        default:
            break;
        }

        if (e.isControlDown()) {
            switch (e.getCode()) {
            case S:
                saveGraph();
                break;
            default:
                break;
            }
        }
    }

    private void deleteVertex(Vertex vertex) {
        graph.removeVertex(vertex);
        updateGraphHistory();
    }

    private Vertex findVertex(double x, double y) {
        for (Vertex vertex : graph.getVertices()) {
            Point position = vertex.getPosition();
            Point size = vertex.getSize();

            boolean matches = x >= position.x && x <= position.x + size.x;
            matches &= y >= position.y && y <= position.y + size.y;

            if (matches) {
                return vertex;
            }
        }
        return null;
    }

    private void updateGraphHistory() {
        graph = graphHistory.createNewVersion();
        renderGraph();
    }

    private void renderGraph() {
        imagePane.getChildren().clear();

        for (Vertex vertex : graph.getVertices()) {
            Color color = vertex == selectedVertex ? Color.RED : Color.BLUE;
            Rectangle rectange = createRectangle(vertex, color);
            imagePane.getChildren().add(rectange);
        }
    }

    private Rectangle createRectangle(Vertex vertex, Color color) {
        Point position = vertex.getPosition();
        Point size = vertex.getSize();

        Rectangle rectange = new Rectangle(size.x, size.y, null);
        rectange.setStroke(color);
        rectange.setX(position.x);
        rectange.setY(position.y);
        return rectange;
    }

    private void saveGraph() {
        try {
            JSONUtils.saveToDisk(graphHistory, graphFile);
        } catch (IOException e) {
            applicationContext.showError("Error by Saving Graph", e);
        }
    }
}
