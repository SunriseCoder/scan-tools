package process.processing.orientation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import filters.FilenameFilterImages;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import process.context.ApplicationContext;
import process.processing.AbstractNode;
import processing.images.rotation.AbstractOrientationRotate;
import processing.images.rotation.RotationAll90DegreesClockWise;
import processing.images.rotation.RotationAll90DegreesCounterClockWise;
import processing.images.rotation.RotationOdd180Degrees;
import utils.FileUtils;

public class OrientationNode extends AbstractNode {
    private ApplicationContext applicationContext;

    @FXML
    private ComboBox<RotationMethods> rotationComboBox;

    @FXML
    private ProgressBar progressBar;

    protected double progress;

    public Node init(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;
        Parent rootNode = FileUtils.loadFXML(this);
        return rootNode;
    }

    public void initialize() throws Exception {
        initComboBox(rotationComboBox, RotationMethodsListCell.class, RotationMethods.values());
    }

    @FXML
    private void startProcessing() throws Exception {
        Thread thread = new Thread(new OrientationTask());
        thread.start();
    }

    private class OrientationTask implements Runnable {

        @Override
        public void run() {
            try {
                // TODO Lock Start Button before start and unlock after job finished
                // TODO Implement Cancel Button (maybe same button, but change caption)
                runWithExceptions();
                Platform.runLater(() -> applicationContext.showMessage("Change Images Orientation is done"));
            } catch (Exception e) {
                Platform.runLater(() -> applicationContext.showError("Error due to Change Images Orientation", e));
            }
        }

        private void runWithExceptions() throws Exception {
            RotationMethods rotationMethod = rotationComboBox.getSelectionModel().getSelectedItem();

            if (rotationMethod == null) {
                return;
            }

            AbstractOrientationRotate rotator = rotationMethod.cl.newInstance();

            File inputFolder = applicationContext.getWorkFolder();
            File outputFolder = new File(inputFolder, "oriented");
            outputFolder.mkdir();

            File[] files = inputFolder.listFiles(new FilenameFilterImages());
            int amountOfImages = files.length;

            for (int i = 0; i < amountOfImages; i++) {
                int sourceIndex = i;
                int destinationIndex = i;


                File sourceFile = files[sourceIndex];
                BufferedImage image = ImageIO.read(sourceFile);

                image = rotator.rotateImage(image, destinationIndex);

                String outputFileName = files[destinationIndex].getName();
                File outputFile = new File(outputFolder, outputFileName);
                String formatName = FileUtils.getFileExtension(outputFileName);
                ImageIO.write(image, formatName, outputFile);

                progress = (double) (i + 1) / amountOfImages;
                Platform.runLater(new UpdateProgressTask());
            }
        }
    }

    public enum RotationMethods {
        All90DegreesCounterClockWise("All pages 90 Degrees Counter-Clock-Wise", RotationAll90DegreesCounterClockWise.class),
        Odd180Degrees("Odd pages 180 Degrees", RotationOdd180Degrees.class),
        All90DegreesClockWise("All pages 90 Degrees Clock-Wise", RotationAll90DegreesClockWise.class);

        private String text;
        private Class<? extends AbstractOrientationRotate> cl;

        private RotationMethods(String text, Class<? extends AbstractOrientationRotate> cl) {
            this.text = text;
            this.cl = cl;
        }

        public String getText() {
            return text;
        }

        public Class<? extends AbstractOrientationRotate> getCl() {
            return cl;
        }
    }

    public static class RotationMethodsListCell extends ListCell<RotationMethods> {
        @Override
        protected void updateItem(RotationMethods item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? null : item.getText());
        }
    }

    private class UpdateProgressTask implements Runnable {
        @Override
        public void run() {
            if (progressBar != null) {
                progressBar.setProgress(progress);
            }
        }
    }
}
