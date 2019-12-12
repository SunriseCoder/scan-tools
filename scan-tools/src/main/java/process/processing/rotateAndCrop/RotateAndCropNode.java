package process.processing.rotateAndCrop;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import dto.Point;
import filters.FilenameFilterImages;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import process.context.ApplicationContext;
import process.handlers.SmoothFilters;
import process.processing.AbstractNode;
import process.processing.render.RenderNode.SmoothFilterListCell;
import processing.images.crop.AbstractImageCropper;
import processing.images.crop.SimpleImageCropper;
import processing.images.filters.ImageFilter;
import processing.images.rotate.AbstractImageRotator;
import processing.images.rotate.MarkupImageRotator;
import utils.FileUtils;

public class RotateAndCropNode extends AbstractNode {
    private ApplicationContext applicationContext;

    @FXML
    private ComboBox<SmoothFilters> smoothFilterComboBox;

    @FXML
    private CheckBox imageRotateCheckBox;

    @FXML
    private CheckBox imageCropCheckBox;

    @FXML
    private ProgressBar progressBar;

    protected double progress;

    public Node init(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;
        Parent rootNode = FileUtils.loadFXML(this);
        return rootNode;
    }

    public void initialize() throws Exception {
        initComboBox(smoothFilterComboBox, SmoothFilterListCell.class, SmoothFilters.values());
    }

    @FXML
    private void startProcessing() throws Exception {
        Thread thread = new Thread(new PrepareTask());
        thread.start();
    }

    private class PrepareTask implements Runnable {

        @Override
        public void run() {
            try {
                // TODO Lock Start Button before start and unlock after job finished
                // TODO Implement Cancel Button (maybe same button, but change caption)
                runWithExceptions();
                Platform.runLater(() -> applicationContext.showMessage("Images Rotate and Crop is done"));
            } catch (Exception e) {
                Platform.runLater(() -> applicationContext.showError("Error due to Rotate and Crop Images", e));
            }
        }

        private void runWithExceptions() throws Exception {
            boolean needRotate = imageRotateCheckBox.isSelected();
            boolean needCrop = imageCropCheckBox.isSelected();

            // Prepare Processors
            SmoothFilters selectedSmoothFilter = smoothFilterComboBox.getSelectionModel().getSelectedItem();
            ImageFilter smoothFilter = selectedSmoothFilter.getCl().newInstance();

            AbstractImageRotator imageRotator = null;
            if (needRotate) {
                imageRotator = new MarkupImageRotator();
                imageRotator.setSmoothFilter(smoothFilter);
            }

            AbstractImageCropper imageCropper = null;
            if (needCrop) {
                imageCropper = new SimpleImageCropper();
            }

            File inputFolder = applicationContext.getWorkFolder();
            File outputFolder = new File(inputFolder, "rotated-and-cropped");
            outputFolder.mkdir();

            File[] files = inputFolder.listFiles(new FilenameFilterImages());
            int amountOfImages = files.length;

            boolean sentNoBoundariesWarning = false;
            for (int i = 0; i < amountOfImages; i++) {
                File sourceFile = files[i];
                String fileName = sourceFile.getName();
                BufferedImage image = ImageIO.read(sourceFile);

                // Getting Image Boundaries from Manual Markup by User
                List<Point> markupBoundaries = applicationContext.getSelectionBoundaries(inputFolder, fileName);
                List<Point> cropBoundaries = markupBoundaries;
                if (markupBoundaries == null) {
                    if (!sentNoBoundariesWarning) {
                        applicationContext.showWarning("Not all Images has been Marked Up", null);
                        sentNoBoundariesWarning = true;
                    }
                }

                // Rotate
                if (needRotate && markupBoundaries != null) {
                    image = imageRotator.processImage(image, markupBoundaries);
                    cropBoundaries = imageRotator.calculateCropBoundaries(image, markupBoundaries);
                }

                // Crop
                if (needCrop && markupBoundaries != null) {
                    image = imageCropper.processImage(image, cropBoundaries);
                }

                String outputFileName = sourceFile.getName();
                File outputFile = new File(outputFolder, outputFileName);
                String formatName = FileUtils.getFileExtension(outputFileName);
                ImageIO.write(image, formatName, outputFile);

                progress = (double) (i + 1) / amountOfImages;
                Platform.runLater(new UpdateProgressTask());
            }
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
