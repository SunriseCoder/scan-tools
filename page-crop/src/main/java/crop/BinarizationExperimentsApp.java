package crop;

import java.awt.image.BufferedImage;
import java.io.File;

import crop.filters.BinarizationFilter;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BinarizationExperimentsApp extends Application {
    private static String imageUrl;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: " + BinarizationExperimentsApp.class.getSimpleName() + " <folder>");
            System.exit(-1);
        }
        imageUrl = new File(args[0]).toURI().toString();
        launch(args);
    }

    private double lastMousePosX;
    private double lastMousePosY;
    private double scale = 0.2;
    private TextField thresholdField;
    private BufferedImage sourceImage;
    private TextField rField;
    private TextField gField;
    private TextField bField;
    private Button renderButton;
    private ImageView imageView1;
    private ImageView imageView2;
    private Pane imagePane;

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox root = new VBox();

        HBox hBox = new HBox();
        root.getChildren().add(hBox);

        Button centerButton = new Button("Center");
        centerButton.setOnAction(e -> centerImage());
        hBox.getChildren().add(centerButton);

        // Threshold
        thresholdField = new TextField("1");
        thresholdField.setOnAction(e -> renderImage());
        hBox.getChildren().addAll(new Label("Threshold"), thresholdField);

        // RGB
        rField = new TextField("1");
        rField.setOnAction(e -> renderImage());
        gField = new TextField("1");
        gField.setOnAction(e -> renderImage());
        bField = new TextField("1");
        bField.setOnAction(e -> renderImage());
        hBox.getChildren().addAll(new Label("RGB"), rField, gField, bField);

        // Render
        renderButton = new Button("Render");
        renderButton.setOnAction(e -> renderImage());
        hBox.getChildren().add(renderButton);

        // Image
        Image image = new Image(imageUrl);
        sourceImage = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_RGB);
        SwingFXUtils.fromFXImage(image, sourceImage);

        // ImageViews
        imageView1 = new ImageView(image);
        imageView2 = new ImageView(image);
        imagePane = new HBox();
        root.getChildren().add(imagePane);
        imagePane.getChildren().addAll(imageView1, imageView2);

        imagePane.setScaleX(scale);
        imagePane.setScaleY(scale);

        imagePane.setOnMousePressed(e -> {
            saveMouseClickPosition(e);
        });

        // Handling dragging event when mouse moved after the button is pressed
        imagePane.setOnMouseDragged(e -> {
            handleImageDrag(e);
            saveMouseClickPosition(e);
        });

        // Handle scaling event
        imagePane.setOnScroll(e -> {
            handleImageScale(e);
        });

        Scene scene = new Scene(root);
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();

        centerImage();
    }

    private void renderImage() {
        renderButton.setDisable(true);
        BufferedImage newImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        BinarizationFilter filter = new BinarizationFilter();
        filter.setImage(sourceImage);
        double threshold = Double.parseDouble(thresholdField.getText());
        double rw = Double.parseDouble(rField.getText());
        double gw = Double.parseDouble(gField.getText());
        double bw = Double.parseDouble(bField.getText());
        threshold *= rw + gw + bw;
        filter.setRw(rw);
        filter.setGw(gw);
        filter.setBw(bw);
        filter.setThreshold(threshold);

        for (int y = 0; y < newImage.getHeight(); y++) {
            for (int x = 0; x < newImage.getWidth(); x++) {
                int resultColor = filter.getRGB(x, y);
                newImage.setRGB(x, y, resultColor );
            }
        }

        Image newImageFX = SwingFXUtils.toFXImage(newImage, null);
        imageView2.setImage(newImageFX);

        renderButton.setDisable(false);
    }

    private void handleImageScale(ScrollEvent scrollEvent) {
        double delta = 1.2;
        double oldScale = scale;
        scale = scrollEvent.getDeltaY() < 0 ? oldScale / delta : oldScale * delta;

        // Adjusting shapes after scale changed
        adjustImageScale(scrollEvent, oldScale);
    }

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

    private void handleImageDrag(MouseEvent mouseEvent) {
        double currentMousePosX = mouseEvent.getSceneX();
        double currentMousePosY = mouseEvent.getSceneY();
        double mouseDeltaX = currentMousePosX - lastMousePosX;
        double mouseDeltaY = currentMousePosY - lastMousePosY;

        imagePane.setTranslateX(imagePane.getTranslateX() + mouseDeltaX);
        imagePane.setTranslateY(imagePane.getTranslateY() + mouseDeltaY);
    }

    private void saveMouseClickPosition(MouseEvent mouseEvent) {
        lastMousePosX = mouseEvent.getSceneX();
        lastMousePosY = mouseEvent.getSceneY();
    }

    private void centerImage() {
        // TODO Investigate, why alignment to center of the parent component does not work
        imagePane.setTranslateX(imagePane.getTranslateX() - imagePane.getBoundsInParent().getMinX());
        imagePane.setTranslateY(imagePane.getTranslateY() - imagePane.getBoundsInParent().getMinY() + 50);
    }
}
