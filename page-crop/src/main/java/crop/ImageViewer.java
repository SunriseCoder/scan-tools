package crop;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import crop.SystemConfiguration.Parameters;
import crop.dto.Point;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import storages.IconStorage;
import storages.IconStorage.Icons;
import utils.MathUtils;

public class ImageViewer {
    private static final Color CIRCLE_COLOR_ACTIVE = Color.RED;
    private static final Color CIRCLE_COLOR_PASSIVE = Color.BLUE;
    private static final double CIRCLE_RADIUS = 40;

    private static final int MARKUP_MODES = 1;
    private static final int MARKUP_FACTOR = 15;

    // Circle names
    private static final String CIRCLE_NAME_BOTTOM_LEFT = "BottomLeft";
    private static final String CIRCLE_NAME_BOTTOM_RIGHT = "BottomRight";
    private static final String CIRCLE_NAME_TOP_RIGHT = "TopRight";
    private static final String CIRCLE_NAME_TOP_LEFT = "TopLeft";

    private SystemConfiguration systemConfiguration;
    private MarkupStorage markupStorage;
    private Map<String, ExtCircle> circles;
    private File currentFolder;
    private String currentImageFilename;

    private Stage stage;
    private Image image;

    // ImageViewer components
    @FXML
    private SplitPane splitPane;
    @FXML
    private Pane imagePane;
    @FXML
    private ImageView imageView;
    @FXML
    private Polygon polygon;
    @FXML
    private Rectangle rectangle;

    // File operations components
    @FXML
    private TextField openFolderTextField;
    @FXML
    private ListView<FileListEntry> filesListView;

    private ExtCircle currentCircle;

    private double lastMousePosX;
    private double lastMousePosY;
    private double scale = 1;
    private int roughMarkupMode;

    public void start(Stage primaryStage) throws Exception {
        systemConfiguration = new SystemConfiguration();

        URL resource = getClass().getResource("ImageViewer.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setController(this);
        Parent root = loader.load();

        stage = primaryStage;
        stage.showingProperty().addListener(e -> {
            String positionsString = systemConfiguration.getValue(Parameters.SplitPaneDivider);
            if (positionsString != null) {
                double positions = Double.parseDouble(positionsString);
                splitPane.setDividerPositions(positions);
            }

            // Adding Listener here due to not overwrite existing value before it would be set
            splitPane.getDividers().get(0).positionProperty().addListener(e2 -> {
                Double value = splitPane.getDividerPositions()[0];
                systemConfiguration.setValue(Parameters.SplitPaneDivider, String.valueOf(value));
            });
        });

        // 4 circles to define points of image crop
        circles = createCircles();
        imagePane.getChildren().addAll(circles.values());

        // Saving mouse position when the button was pressed
        imagePane.setOnMousePressed(e -> {
            imagePane.requestFocus();
            saveMouseClickPosition(e);
            setCurrentCircle();
        });

        // Handling dragging event when mouse moved after the button is pressed
        imagePane.setOnMouseDragged(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                handleImageDrag(e);
            }

            if (e.getButton().equals(MouseButton.SECONDARY)) {
                handleCircleDrag(e);
            }

            saveMouseClickPosition(e);
        });

        // Handle scaling event
        imagePane.setOnScroll(e -> {
            handleImageScale(e);
        });

        // Moving image (or circles) with the keyboard
        imagePane.setOnKeyPressed(e -> {
            handleMoveViaKeyboard(e);
        });

        // TODO Rewrite it better way, maybe extract to nested or new file
        filesListView.setCellFactory(new Callback<ListView<FileListEntry>, ListCell<FileListEntry>>() {
            @Override
            public ListCell<FileListEntry> call(ListView<FileListEntry> param) {
                return new ListCell<FileListEntry>() {
                    @Override
                    protected void updateItem(FileListEntry item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            Icons icon = item != null && item.saved ? Icons.CHECKED_16 : Icons.TRANSPARENT_16;
                            Image image = IconStorage.getIcon(icon);
                            setGraphic(new ImageView(image));
                            setText(item.filename);
                        }
                    }
                };
            }
        });

        filesListView.getSelectionModel().selectedItemProperty().addListener(event -> {
            handleSelectFile();
        });

        String startFolder = systemConfiguration.getValue(Parameters.StartFolder);
        if (startFolder != null) {
            currentFolder = new File(startFolder);
            refreshFileList();
        }

        // Rendering the form
        Scene scene = new Scene(root);
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Map<String, ExtCircle> createCircles() {
        Map<String, ExtCircle> circles = new LinkedHashMap<>();
        circles.put(CIRCLE_NAME_TOP_LEFT, createCircle(CIRCLE_NAME_TOP_LEFT));
        circles.put(CIRCLE_NAME_TOP_RIGHT, createCircle(CIRCLE_NAME_TOP_RIGHT));
        circles.put(CIRCLE_NAME_BOTTOM_RIGHT, createCircle(CIRCLE_NAME_BOTTOM_RIGHT));
        circles.put(CIRCLE_NAME_BOTTOM_LEFT, createCircle(CIRCLE_NAME_BOTTOM_LEFT));

        linkCircles(circles);
        return circles;
    }

    private ExtCircle createCircle(String name) {
        ExtCircle circle = new ExtCircle(name, 0, 0, CIRCLE_RADIUS);
        circle.setFill(null);
        circle.setStroke(CIRCLE_COLOR_PASSIVE);
        circle.setStrokeWidth(3);
        circle.centerXProperty().addListener(e -> adjustPolygonBoundaries());
        circle.centerYProperty().addListener(e -> adjustPolygonBoundaries());
        return circle;
    }

    private void linkCircles(Map<String, ExtCircle> circles) {
        List<ExtCircle> list = new ArrayList<>(circles.values());
        ExtCircle firstCircle = list.get(0);
        ExtCircle lastCircle = null;
        for (int i = 0; i < list.size(); i++) {
            ExtCircle currentCircle = list.get(i);
            if (lastCircle != null) {
                currentCircle.previous = lastCircle;
                lastCircle.next = currentCircle;
            }
            lastCircle = currentCircle;
        }
        firstCircle.previous = lastCircle;
        lastCircle.next = firstCircle;
    }

    private ExtCircle findCircle(double x, double y) {
        for (ExtCircle circle : circles.values()) {
            double circleCenterX = circle.getCenterX();
            double circleCenterY = circle.getCenterY();

            double currentRange = MathUtils.calculateDistance(x - circleCenterX, y - circleCenterY);
            double visibleRadius = circle.getRadius() / scale;

            if (currentRange <= visibleRadius) {
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

    private void setCurrentCircle() {
        double posOnImageX = getImageCoordinateX(lastMousePosX);
        double posOnImageY = getImageCoordinateY(lastMousePosY);
        ExtCircle foundCircle = findCircle(posOnImageX, posOnImageY);
        setCurrentCircle(foundCircle);
    }

    private void setCurrentCircle(ExtCircle foundCircle) {
        if (foundCircle != null) {
            changeCircle(foundCircle);
        }
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
        // If image is not initialized yet
        if (image == null) {
            return;
        }

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

    private void handleMoveViaKeyboard(KeyEvent e) {
        e.consume();

        if (currentImageFilename == null || currentCircle == null) {
            return;
        }

        int step = (int) Math.round(1 / scale);
        step = MathUtils.adjustValue(step, 1, 100);
        step *= Math.pow(MARKUP_FACTOR, roughMarkupMode);

        double newX, newY;
        switch (e.getCode()) {
            case RIGHT:
            case D:
                newX = currentCircle.getCenterX() + step;
                newX = adjustNewCirclePositionX(currentCircle, newX);
                currentCircle.setCenterX(newX);
                break;
            case LEFT:
            case A:
                newX = currentCircle.getCenterX() - step;
                newX = adjustNewCirclePositionX(currentCircle, newX);
                currentCircle.setCenterX(newX);
                break;
            case UP:
            case W:
                newY = currentCircle.getCenterY() - step;
                newY = adjustNewCirclePositionY(currentCircle, newY);
                currentCircle.setCenterY(newY);
                break;
            case DOWN:
            case S:
                newY = currentCircle.getCenterY() + step;
                newY = adjustNewCirclePositionY(currentCircle, newY);
                currentCircle.setCenterY(newY);
                break;
            case Q:
            case Z:
                changeCircle(currentCircle.previous);
                break;
            case E:
            case X:
                changeCircle(currentCircle.next);
                break;
            case ENTER:
            case F:
                saveImage();
                break;
            case SHIFT:
                if (--roughMarkupMode < 0) {
                    roughMarkupMode = MARKUP_MODES - 1;
                }
            default:
                // Ignore unsupported KeyCode
        }
    }

    private void changeCircle(ExtCircle next) {
        if (currentCircle != null) {
            currentCircle.setStroke(CIRCLE_COLOR_PASSIVE);
        }
        next.setStroke(CIRCLE_COLOR_ACTIVE);
        currentCircle = next;
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
        newX = newX >= imageWidth ? imageWidth : newX;
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
        newY = newY >= imageHeight ? imageHeight : newY;
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

    @FXML
    private void selectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Folder with Images");
        File newFolder = directoryChooser.showDialog(stage);

        if (newFolder == null) {
            return;
        }

        currentFolder = newFolder;
        systemConfiguration.setValue(Parameters.StartFolder, currentFolder.getAbsolutePath());
        refreshFileList();
    }

    @FXML
    private void refreshFileList() {
        if (currentFolder == null) {
            return;
        }

        markupStorage = new MarkupStorage(currentFolder);

        openFolderTextField.setText(currentFolder.getAbsolutePath());

        // Forming new list of FileListEntry for filesListView
        String[] filenames = currentFolder.list();
        List<FileListEntry> fileEntries = Arrays.stream(filenames)
                .map(filename -> {
                    boolean saved = markupStorage.getSelectionBoundaries(filename) != null;
                    FileListEntry fileListEntry = new FileListEntry(filename, saved);
                    return fileListEntry;
                }).collect(Collectors.toList());
        ObservableList<FileListEntry> items = FXCollections.observableArrayList(fileEntries);

        // Looking for old selected item in the new list
        FileListEntry newSelectedItem = items.stream()
                .filter(item -> item.filename.equals(currentImageFilename))
                .findFirst().orElse(null);

        // Restoring the selection of the old file in the new list
        filesListView.setItems(items);
        if (newSelectedItem != null) {
            filesListView.getSelectionModel().select(newSelectedItem);
        }
    }

    private void handleSelectFile() {
        FileListEntry selectedItem = filesListView.getSelectionModel().getSelectedItem();

        // fileListView selection could be empty due to refresh
        if (selectedItem == null) {
            return;
        }

        currentImageFilename = selectedItem.filename;
        String uri = new File(currentFolder, currentImageFilename).toURI().toString();
        image = new Image(uri);
        imageView.setImage(image);

        // Scale image to fit into the window
        double parentWidth = imagePane.getParent().getBoundsInLocal().getWidth() - CIRCLE_RADIUS * 2;
        double parentHeight = imagePane.getParent().getBoundsInLocal().getHeight() - CIRCLE_RADIUS * 2;
        double horizontalRatio = parentWidth / image.getWidth();
        double verticalRatio = parentHeight / image.getHeight();
        scale = Math.min(horizontalRatio, verticalRatio);
        imagePane.setScaleX(scale);
        imagePane.setScaleY(scale);

        adjustRectangleScale();
        adjustCirclesScale();
        adjustCirclePositions();

        centerImage();

        roughMarkupMode = MARKUP_MODES - 1;
        setCurrentCircle(circles.get(CIRCLE_NAME_TOP_LEFT));
    }

    private void selectNextFile() {
        filesListView.getSelectionModel().selectNext();

        int selectedIndex = filesListView.getSelectionModel().getSelectedIndex();
        int scrollTo = selectedIndex >= 10 ? selectedIndex - 10 : 0;
        filesListView.scrollTo(scrollTo);
    }

    private void adjustCirclePositions() {
        List<Point> selectionBoundaries = markupStorage.getSelectionBoundaries(currentImageFilename);
        if (selectionBoundaries == null) {
            circles.get(CIRCLE_NAME_TOP_LEFT).setCenter(0, 0);
            circles.get(CIRCLE_NAME_TOP_RIGHT).setCenter(image.getWidth(), 0);
            circles.get(CIRCLE_NAME_BOTTOM_RIGHT).setCenter(image.getWidth(), image.getHeight());
            circles.get(CIRCLE_NAME_BOTTOM_LEFT).setCenter(0, image.getHeight());
        } else {
            Iterator<Point> iterator = selectionBoundaries.iterator();
            circles.values().forEach(circle -> {
                Point point = iterator.next();
                circle.setCenter(point.x, point.y);
            });
        }
    }

    @FXML
    private void centerImage() {
        // TODO Investigate, why alignment to center of the parent component does not work
        imagePane.setTranslateX(imagePane.getTranslateX() - imagePane.getBoundsInParent().getMinX() - CIRCLE_RADIUS);
        imagePane.setTranslateY(imagePane.getTranslateY() - imagePane.getBoundsInParent().getMinY() - CIRCLE_RADIUS);
    }

    @FXML
    private void saveImage() {
        try {
            trySaveImage();
            // TODO Rewrite it with button disabled and enabled when needed
            // I.e. by default disabled, by select image enabled, by refresh file list disabled
            // Be aware to enable button due to exception
            refreshFileList();
            selectNextFile();
        } catch (IOException e) {
            // TODO Rewrite to show error message to user
            throw new RuntimeException(e);
        }
    }

    private void trySaveImage() throws IOException {
        if (image == null) {
            return;
        }

        // Save boundaries for the Image to the log file
        List<Point> selectionBoundaries = extractBoundaries();
        saveBoundaries(selectionBoundaries);
    }

    private List<Point> extractBoundaries() {
        List<Point> extractedPoints = circles.values().stream()
                .map(circle -> new Point(circle.getCenterX(), circle.getCenterY()))
                .collect(Collectors.toList());
        return extractedPoints;
    }

    private void saveBoundaries(List<Point> selectionBoundaries) {
        if (currentImageFilename != null) {
            markupStorage.saveInfo(currentImageFilename, selectionBoundaries);
        }
    }

    private static class FileListEntry {
        private String filename;
        private boolean saved;

        public FileListEntry(String filename, boolean saved) {
            this.filename = filename;
            this.saved = saved;
        }
    }
}
