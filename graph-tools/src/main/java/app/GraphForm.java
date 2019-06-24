package app;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;

import app.context.ApplicationContext;
import app.context.ApplicationParameters;
import dto.Edge;
import dto.EdgeEndpoint;
import dto.Graph;
import dto.Point;
import dto.Vertex;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import utils.FileUtils;
import utils.JSONUtils;

public class GraphForm {
    private static final int VERTEX_EDGE_HOVER_WIDTH = 10;

    private ApplicationContext applicationContext;

    // General
    @FXML
    private Pane imagePane;
    @FXML
    private ImageView imageView;

    private File graphFile;
    private Graph graph;

    private Vertex selectedVertex;
    private double lastPosX;
    private double lastPosY;

    private double scale = 1;

    private Line hoverLine;
    private EdgeEndpoint edgeStart;
    private Line newEdgeLine;

    public GraphForm() {
        hoverLine = new Line();
        hoverLine.setFill(null);
        hoverLine.setStroke(Color.GREEN);
        hoverLine.setStrokeWidth(3);

        newEdgeLine = new Line();
        newEdgeLine.setFill(null);
        newEdgeLine.setStroke(Color.GREEN);
        newEdgeLine.setStrokeWidth(3);
    }

    public Parent init(ApplicationContext applicationContext) throws IOException {
        String graphFileName = applicationContext.getParameterValue(ApplicationParameters.GraphFileName);
        graphFile = new File(graphFileName);

        Parent root = FileUtils.loadFXML(this);

        imagePane.setOnMouseMoved(e -> handleMouseMoved(e));
        imagePane.setOnMousePressed(e -> handleMousePressed(e));
        imagePane.setOnMouseDragged(e -> handleMouseDrag(e));

        imagePane.setOnKeyPressed(e -> handleKeyPressed(e));

        restoreGraph();

        return root;
    }

    private void restoreGraph() {
        try {
            TypeReference<Graph> typeReference = new TypeReference<Graph>() {
            };
            graph = JSONUtils.loadFromDisk(graphFile, typeReference);
        } catch (IOException e) {
            graph = new Graph();
        }

        renderGraph();
    }

    private void handleMouseMoved(MouseEvent e) {
        double posOnImageX = getImageCoordinateX(e.getSceneX());
        double posOnImageY = getImageCoordinateY(e.getSceneY());

        Vertex vertex = findVertex(posOnImageX, posOnImageY);

        updateHoverLine(vertex, posOnImageX, posOnImageY);

        updateNewEdgeLine(posOnImageX, posOnImageY);
    }

    private void updateHoverLine(Vertex vertex, double posOnImageX, double posOnImageY) {
        if (vertex == null) {
            hoverLine.setOpacity(0);
            return;
        }

        double vertexLeftX = vertex.getPosition().x;
        double vertexRightX = vertexLeftX + vertex.getSize().x;
        double vertexTopY = vertex.getPosition().y;
        double vertexBottomY = vertexTopY + vertex.getSize().y;

        double startX = 0, endX = 0, startY = 0, endY = 0, opacity = 0;

        // Left Edge
        if (posOnImageX >= vertexLeftX - VERTEX_EDGE_HOVER_WIDTH && posOnImageX <= vertexLeftX + VERTEX_EDGE_HOVER_WIDTH) {
            startX = endX = vertexLeftX;
            startY = vertexTopY;
            endY = vertexBottomY;
            opacity = 1;
        }

        // Top Edge
        if (posOnImageY >= vertexTopY - VERTEX_EDGE_HOVER_WIDTH && posOnImageY <= vertexTopY + VERTEX_EDGE_HOVER_WIDTH) {
            startX = vertexLeftX;
            endX = vertexRightX;
            startY = endY = vertexTopY;
            opacity = 1;
        }

        // Right Edge
        if (posOnImageX >= vertexRightX - VERTEX_EDGE_HOVER_WIDTH && posOnImageX <= vertexRightX + VERTEX_EDGE_HOVER_WIDTH) {
            startX = endX = vertexRightX;
            startY = vertexTopY;
            endY = vertexBottomY;
            opacity = 1;
        }

        // Bottom Edge
        if (posOnImageY >= vertexBottomY - VERTEX_EDGE_HOVER_WIDTH && posOnImageY <= vertexBottomY + VERTEX_EDGE_HOVER_WIDTH) {
            startX = vertexLeftX;
            endX = vertexRightX;
            startY = endY = vertexBottomY;
            opacity = 1;
        }

        hoverLine.setOpacity(opacity);
        hoverLine.setStartX(startX);
        hoverLine.setStartY(startY);
        hoverLine.setEndX(endX);
        hoverLine.setEndY(endY);
    }

    private void updateNewEdgeLine(double posOnImageX, double posOnImageY) {
        if (edgeStart == null) {
            newEdgeLine.setOpacity(0);
            return;
        }

        Vertex vertex = edgeStart.getVertex();
        Point vertexPosition = vertex.getPosition();
        Point startPoint = edgeStart.getPoint();

        newEdgeLine.setOpacity(1);

        double startX = vertexPosition.x + startPoint.x;
        double startY = vertexPosition.y + startPoint.y;

        newEdgeLine.setStartX(startX);
        newEdgeLine.setStartY(startY);
        newEdgeLine.setEndX(posOnImageX);
        newEdgeLine.setEndY(posOnImageY);
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
            checkAddEdge(vertex, posOnImageX, posOnImageY);
        }

        renderGraph();
    }

    private void addVertex(double x, double y) {
        Vertex vertex;
        vertex = new Vertex();
        vertex.setPosition(x, y);
        vertex.setSize(100, 50);
        graph.addVertex(vertex);

        renderGraph();
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
        renderGraph();
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

    private void checkAddEdge(Vertex vertex, double x, double y) {
        double vertexLeftX = vertex.getPosition().x;
        double vertexRightX = vertexLeftX + vertex.getSize().x;
        double vertexTopY = vertex.getPosition().y;
        double vertexBottomY = vertexTopY + vertex.getSize().y;

        //edgeStart = null;
        Point point = null;
        if (vertex != null) {
            // Left Edge
            if (x >= vertexLeftX - VERTEX_EDGE_HOVER_WIDTH && x <= vertexLeftX + VERTEX_EDGE_HOVER_WIDTH) {
                point = new Point(0, y - vertexTopY);
            }

            // Top Edge
            if (y >= vertexTopY - VERTEX_EDGE_HOVER_WIDTH && y <= vertexTopY + VERTEX_EDGE_HOVER_WIDTH) {
                point = new Point(x - vertexLeftX, 0);
            }

            // Right Edge
            if (x >= vertexRightX - VERTEX_EDGE_HOVER_WIDTH && x <= vertexRightX + VERTEX_EDGE_HOVER_WIDTH) {
                point = new Point(vertexRightX - vertexLeftX, y - vertexTopY);
            }

            // Bottom Edge
            if (y >= vertexBottomY - VERTEX_EDGE_HOVER_WIDTH && y <= vertexBottomY + VERTEX_EDGE_HOVER_WIDTH) {
                point = new Point(x - vertexLeftX, vertexBottomY - vertexTopY);
            }

            if (edgeStart == null) {
                if (point != null) {
                    edgeStart = new EdgeEndpoint(point, vertex);
                }
            } else {
                addEdge(edgeStart, new EdgeEndpoint(point, vertex));
                edgeStart = null;
            }
        }
    }

    private void addEdge(EdgeEndpoint edgeStart, EdgeEndpoint edgeEnd) {
        Edge edge = new Edge(edgeStart, edgeEnd);
        graph.addEdge(edge);
        renderGraph();
    }

    private void renderGraph() {
        imagePane.getChildren().clear();

        imagePane.getChildren().add(hoverLine);
        imagePane.getChildren().add(newEdgeLine);

        for (Vertex vertex : graph.getVertices()) {
            Color color = vertex == selectedVertex ? Color.RED : Color.BLUE;
            Rectangle rectange = createVertexRectangle(vertex, color);
            imagePane.getChildren().add(rectange);
        }

        for (Edge edge : graph.getEdges()) {
            Line line = createEdgeLine(edge);
            imagePane.getChildren().add(line);
        }
    }

    private Rectangle createVertexRectangle(Vertex vertex, Color color) {
        Point position = vertex.getPosition();
        Point size = vertex.getSize();

        Rectangle rectange = new Rectangle(size.x, size.y, null);
        rectange.setStroke(color);
        rectange.setX(position.x);
        rectange.setY(position.y);

        return rectange;
    }

    private Line createEdgeLine(Edge edge) {
        Line line = new Line();
        line.setStroke(Color.BLUE);

        line.setStartX(edge.getEdgeStart().getVertex().getPosition().x + edge.getEdgeStart().getPoint().x);
        line.setStartY(edge.getEdgeStart().getVertex().getPosition().y + edge.getEdgeStart().getPoint().y);
        line.setEndX(edge.getEdgeEnd().getVertex().getPosition().x + edge.getEdgeEnd().getPoint().x);
        line.setEndY(edge.getEdgeEnd().getVertex().getPosition().y + edge.getEdgeEnd().getPoint().y);

        return line;
    }

    private void saveGraph() {
        try {
            JSONUtils.saveToDisk(graph, graphFile);
        } catch (IOException e) {
            applicationContext.showError("Error by Saving Graph", e);
        }
    }
}
