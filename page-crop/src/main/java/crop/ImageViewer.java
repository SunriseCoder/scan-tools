package crop;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class ImageViewer {
    private static final Color CIRCLE_COLOR = Color.RED;
    private static final double CIRCLE_RADIUS = 40;
    private static final double POLYGON_OPACITY = 0.5;

    // Circle names
    private static final String CIRCLE_NAME_BOTTOM_LEFT = "BottomLeft";
    private static final String CIRCLE_NAME_BOTTOM_RIGHT = "BottomRight";
    private static final String CIRCLE_NAME_TOP_RIGHT = "TopRight";
    private static final String CIRCLE_NAME_TOP_LEFT = "TopLeft";

    private Map<String, ExtCircle> circles;

    private Pane imagePane;
    private Image image;
    private Polygon polygon;
    private Rectangle rectangle;

    private double lastMousePosX;
    private double lastMousePosY;
    private double scale = 1;

    public void start(Stage primaryStage, String filename) throws Exception {
        String uri = new File(filename).toURI().toString();
        image = new Image(uri);

        Pane rootPane = new Pane();

        imagePane = new Pane();
        rootPane.getChildren().add(imagePane);

        // This rectangle has border around the image twice bigger than circle radius
        // to prevent imagePane resize during moving the circles
        rectangle = new Rectangle();
        rectangle.setFill(null);
        adjustRectangleScale();
        imagePane.getChildren().add(rectangle);

        // Image wrapper
        ImageView imageView = new ImageView(image);
        imageView.setFocusTraversable(true);
        imagePane.getChildren().add(imageView);

        // 4 circles to define points of image crop
        circles = createCircles(image.getWidth(), image.getHeight());
        imagePane.getChildren().addAll(circles.values());

        // Semi-transparently filled area to see the valuable part of image
        polygon = new Polygon();
        polygon.setFill(Color.GREEN);
        polygon.setStroke(CIRCLE_COLOR);
        polygon.setStrokeWidth(0);
        polygon.setOpacity(POLYGON_OPACITY);
        adjustPolygonBoundaries();
        imagePane.getChildren().add(polygon);

        // Saving mouse position when the button was pressed
        rootPane.setOnMousePressed(e -> {
            saveMouseClickPosition(e);
        });

        // Handling dragging event when mouse moved after the button is pressed
        rootPane.setOnMouseDragged(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                handleImageDrag(e);
            }

            if (e.getButton().equals(MouseButton.SECONDARY)) {
                handleCircleDrag(e);
            }

            saveMouseClickPosition(e);
        });

        // Handle scaling event
        rootPane.setOnScroll(e -> {
            handleImageScale(e);
        });

        // Moving image (or circles) with the keyboard
        rootPane.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case RIGHT:
                    imagePane.setTranslateX(imagePane.getTranslateX() + 10);
                    break;
                case LEFT:
                	imagePane.setTranslateX(imagePane.getTranslateX() - 10);
                    break;
                case UP:
                	imagePane.setTranslateY(imagePane.getTranslateY() - 10);
                    break;
                case DOWN:
                	imagePane.setTranslateY(imagePane.getTranslateY() + 10);
                    break;
                default:
                    // Ignore unsupported KeyCode
            }
        });

        // Rendering the form
        Scene scene = new Scene(rootPane, 300, 250);
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Map<String, ExtCircle> createCircles(double width, double height) {
        Map<String, ExtCircle> circles = new LinkedHashMap<>();
        circles.put(CIRCLE_NAME_TOP_LEFT, createCircle(CIRCLE_NAME_TOP_LEFT, 0, 0));
        circles.put(CIRCLE_NAME_TOP_RIGHT, createCircle(CIRCLE_NAME_TOP_RIGHT, width - 1, 0));
        circles.put(CIRCLE_NAME_BOTTOM_RIGHT, createCircle(CIRCLE_NAME_BOTTOM_RIGHT, width - 1, height - 1));
        circles.put(CIRCLE_NAME_BOTTOM_LEFT, createCircle(CIRCLE_NAME_BOTTOM_LEFT, 0, height - 1));
        return circles;
    }

    private ExtCircle createCircle(String name, double x, double y) {
        ExtCircle circle = new ExtCircle(name, x, y, CIRCLE_RADIUS);
        circle.setFill(null);
        circle.setStroke(CIRCLE_COLOR);
        circle.setStrokeWidth(3);
        return circle;
    }

    private ExtCircle findCircle(double x, double y) {
        double foundRange = Double.MAX_VALUE;
        for (ExtCircle circle : circles.values()) {
            double circleCenterX = circle.getCenterX();
            double circleCenterY = circle.getCenterY();

            double currentRange = Math.sqrt(Math.pow(x - circleCenterX, 2) + Math.pow(y - circleCenterY, 2));
            double visibleRadius = circle.getRadius() / scale;

            if (currentRange <= visibleRadius && currentRange < foundRange) {
                return circle;
            }
        }
        return null;
    }

    /**
     * Adjusting scale of circles that they would have the same visual size
     */
    private void adjustCirclesScale() {
        circles.values().forEach(circle -> {
            circle.setScaleX(1 / scale);
            circle.setScaleY(1 / scale);
        });
    }

    /**
     * Adjust image size and position after scale
     *
     * @param scrollEvent
     * @param oldScale
     */
    private void adjustImageScale(ScrollEvent scrollEvent, double oldScale) {
        // Scale Image
        imagePane.setScaleX(scale);
        imagePane.setScaleY(scale);

        // Calculating new coordinates to shift the Image
        Bounds boundsInParent = imagePane.getBoundsInParent();
        double currentMousePosX = scrollEvent.getSceneX();
        double currentMousePosY = scrollEvent.getSceneY();

        double factor = scale / oldScale - 1;
        double deltaX = boundsInParent.getWidth() / 2 + boundsInParent.getMinX() - currentMousePosX;
        double deltaY = boundsInParent.getHeight() / 2 + boundsInParent.getMinY() - currentMousePosY;

        // Shifting the Image that the part of image at cursor position was the same
        imagePane.setTranslateX(imagePane.getTranslateX() + factor * deltaX);
        imagePane.setTranslateY(imagePane.getTranslateY() + factor * deltaY);
    }

    /**
     * Adjusting scale of the Rectangle
     */
    private void adjustRectangleScale() {
        rectangle.setX(0 - CIRCLE_RADIUS * 2 / scale);
        rectangle.setY(0 - CIRCLE_RADIUS * 2 / scale);
        rectangle.setWidth(image.getWidth() + CIRCLE_RADIUS * 4 / scale);
        rectangle.setHeight(image.getHeight() + CIRCLE_RADIUS * 4 / scale);
    }

    /**
     * Adjusting the Polygon's boundaries
     */
    private void adjustPolygonBoundaries() {
        polygon.getPoints().clear();

        List<Double> points = new ArrayList<>();
        circles.values().forEach(c -> {
            points.add(c.getCenterX());
            points.add(c.getCenterY());
        });

        polygon.getPoints().addAll(points);
    }

    private void saveMouseClickPosition(MouseEvent mouseEvent) {
        lastMousePosX = mouseEvent.getSceneX();
        lastMousePosY = mouseEvent.getSceneY();
    }

    private void handleImageDrag(MouseEvent mouseEvent) {
        double currentMousePosX = mouseEvent.getSceneX();
        double currentMousePosY = mouseEvent.getSceneY();
        double mouseDeltaX = currentMousePosX - lastMousePosX;
        double mouseDeltaY = currentMousePosY - lastMousePosY;

        imagePane.setTranslateX(imagePane.getTranslateX() + mouseDeltaX);
        imagePane.setTranslateY(imagePane.getTranslateY() + mouseDeltaY);
    }

    private void handleCircleDrag(MouseEvent mouseEvent) {
        double currentMousePosX = mouseEvent.getSceneX();
        double currentMousePosY = mouseEvent.getSceneY();

        // Calculating position of the Circle on the Image
        double posOnImageX = getImageCoordinateX(lastMousePosX);
        double posOnImageY = getImageCoordinateY(lastMousePosY);

        // Looking for the Circle
        ExtCircle circle = findCircle(posOnImageX, posOnImageY);

        if (circle == null) {
            return;
        }

        // Calculating new position of the Circle
        double newPositionX = getImageCoordinateX(currentMousePosX);
        double newPositionY = getImageCoordinateY(currentMousePosY);

        // Validation and adjustment of the coordinates
        newPositionX = adjustNewCirclePositionX(circle, newPositionX);
        newPositionY = adjustNewCirclePositionY(circle, newPositionY);

        // Applying coordinates
        circle.setCenterX(newPositionX);
        circle.setCenterY(newPositionY);

        // Adjusting Polygon due to Circle move
        adjustPolygonBoundaries();
    }

    private void handleImageScale(ScrollEvent scrollEvent) {
        double delta = 1.2;
        double oldScale = scale;
        scale = scrollEvent.getDeltaY() < 0 ? oldScale / delta : oldScale * delta;

        // Adjusting shapes after scale changed
        adjustImageScale(scrollEvent, oldScale);
        adjustCirclesScale();
        adjustRectangleScale();
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

    private double adjustNewCirclePositionX(ExtCircle circle, double newX) {
        // Checking that position X is not outside the Image width
        double imageWidth = image.getWidth();
        newX = newX >= imageWidth ? imageWidth - 1 : newX;
        newX = newX < 0 ? 0 : newX;

        switch (circle.getName()) {
            case CIRCLE_NAME_TOP_LEFT:
            case CIRCLE_NAME_BOTTOM_LEFT:
                // Checking that Left Circles don't have position X bigger than Right Circles
                double topRightX = circles.get(CIRCLE_NAME_TOP_RIGHT).getCenterX();
                double bottomRightX = circles.get(CIRCLE_NAME_BOTTOM_RIGHT).getCenterX();
                newX = newX > topRightX ? topRightX : newX;
                newX = newX > bottomRightX ? bottomRightX : newX;
                break;
            case CIRCLE_NAME_TOP_RIGHT:
            case CIRCLE_NAME_BOTTOM_RIGHT:
                // Checking that Right Circles don't have position X smaller than Left Circles
                double topLeftX = circles.get(CIRCLE_NAME_TOP_LEFT).getCenterX();
                double bottomLeftX = circles.get(CIRCLE_NAME_BOTTOM_LEFT).getCenterX();
                newX = newX < topLeftX ? topLeftX : newX;
                newX = newX < bottomLeftX ? bottomLeftX : newX;
                break;
        }

        return newX;
    }

    private double adjustNewCirclePositionY(ExtCircle circle, double newY) {
        // Checking that position Y is not outside the Image height
        double imageHeight = image.getHeight();
        newY = newY >= imageHeight ? imageHeight - 1 : newY;
        newY = newY < 0 ? 0 : newY;

        switch (circle.getName()) {
            case CIRCLE_NAME_TOP_LEFT:
            case CIRCLE_NAME_TOP_RIGHT:
                // Checking that Top Circles don't have position Y bigger than Bottom Circles
                double bottomRightY = circles.get(CIRCLE_NAME_BOTTOM_RIGHT).getCenterY();
                double bottomLeftY = circles.get(CIRCLE_NAME_BOTTOM_LEFT).getCenterY();
                newY = newY > bottomRightY ? bottomRightY : newY;
                newY = newY > bottomLeftY ? bottomLeftY : newY;
                break;
            case CIRCLE_NAME_BOTTOM_RIGHT:
            case CIRCLE_NAME_BOTTOM_LEFT:
                // Checking that Bottom Circles don't have position Y smaller than Top Circles
                double topLeftY = circles.get(CIRCLE_NAME_TOP_LEFT).getCenterY();
                double topRightY = circles.get(CIRCLE_NAME_TOP_RIGHT).getCenterY();
                newY = newY < topLeftY ? topLeftY : newY;
                newY = newY < topRightY ? topRightY : newY;
                break;
        }

        return newY;
    }
}
