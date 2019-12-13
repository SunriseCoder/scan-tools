package process.processing.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import filters.FilenameFilterImages;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import process.context.ApplicationContext;
import process.handlers.SmoothFilters;
import process.processing.AbstractNode;
import processing.images.binarization.ImageBinarization;
import processing.images.filters.BinarizationFilter;
import processing.images.filters.ImageFilter;
import processing.images.resize.ImageResize;
import utils.FileUtils;
import utils.ThreadUtils;

public class RenderNode extends AbstractNode {
    private static final String DEFAULT_RESIZE_SOURCE_DPI = "600";
    private static final String DEFAULT_RESIZE_TARGET_DPI = "400";

    private static final String DEFAULT_BINARIZATION_THRESHOLD = "100000";
    private static final String DEFAULT_BINARIZATION_WEIGHT_RED = "3";
    private static final String DEFAULT_BINARIZATION_WEIGHT_GREEN = "1";
    private static final String DEFAULT_BINARIZATION_WEIGHT_BLUE = "1";

    private ApplicationContext applicationContext;

    @FXML
    private GridPane gridPane;

    @FXML
    private ComboBox<SmoothFilters> smoothFilterComboBox;

    @FXML
    private CheckBox imageResizeCheckBox;
    @FXML
    private TextField sourceDPITextField;
    @FXML
    private TextField targetDPITextField;

    @FXML
    private CheckBox imageBinarizationCheckBox;
    @FXML
    private TextField thresholdTextField;
    @FXML
    private TextField weightRedTextField;
    @FXML
    private TextField weightGreenTextField;
    @FXML
    private TextField weightBlueTextField;

    @FXML
    private ProgressBar progressBar;

    private double progress;

    public Node init(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;
        Parent rootNode = FileUtils.loadFXML(this);
        return rootNode;
    }

    public void initialize() throws Exception {
        initComboBox(smoothFilterComboBox, SmoothFilterListCell.class, SmoothFilters.values());

        sourceDPITextField.setText(DEFAULT_RESIZE_SOURCE_DPI);
        targetDPITextField.setText(DEFAULT_RESIZE_TARGET_DPI);

        thresholdTextField.setText(DEFAULT_BINARIZATION_THRESHOLD);
        weightRedTextField.setText(DEFAULT_BINARIZATION_WEIGHT_RED);
        weightGreenTextField.setText(DEFAULT_BINARIZATION_WEIGHT_GREEN);
        weightBlueTextField.setText(DEFAULT_BINARIZATION_WEIGHT_BLUE);
    }

    @FXML
    private void startProcessing() {
        Thread thread = new Thread(new RenderTask());
        thread.start();
    }

    private class RenderTask implements Runnable {

        @Override
        public void run() {
            try {
                // TODO Lock Start Button before start and unlock after job finished
                // TODO Implement Cancel Button (maybe same button, but change caption)
                runWithExceptions();
                Platform.runLater(() -> applicationContext.showMessage("Render Images is done"));
            } catch (Exception e) {
                Platform.runLater(() -> applicationContext.showError("Error due to Render Images", e));
            }
        }

        private void runWithExceptions() throws Exception {
            boolean needResize = imageResizeCheckBox.isSelected();
            boolean needBinarization = imageBinarizationCheckBox.isSelected();

            File inputFolder = applicationContext.getWorkFolder();
            File outputFolder = new File(inputFolder, "rendered");
            outputFolder.mkdir();

            SmoothFilters selectedSmoothFilter = smoothFilterComboBox.getSelectionModel().getSelectedItem();

            // Preparing processors
            ImageFilter smoothFilter = selectedSmoothFilter.getCl().newInstance();

            ImageResize resize = null;
            if (needResize) {
                resize = new ImageResize();
                resize.setSmoothFilter(smoothFilter);
                int sourceDPI = Integer.parseInt(sourceDPITextField.getText());
                resize.setSourceDPI(sourceDPI);
                int targetDPI = Integer.parseInt(targetDPITextField.getText());
                resize.setTargetDPI(targetDPI);
            }

            ImageBinarization binarization = null;
            if (needBinarization) {
                BinarizationFilter binarizationFilter = new BinarizationFilter();
                double weightRed = Double.parseDouble(weightRedTextField.getText());
                binarizationFilter.setWeightRed(weightRed);
                double weightGreen = Double.parseDouble(weightGreenTextField.getText());
                binarizationFilter.setWeightGreen(weightGreen);
                double weightBlue = Double.parseDouble(weightBlueTextField.getText());
                binarizationFilter.setWeightBlue(weightBlue);
                double threshold = Double.parseDouble(thresholdTextField.getText());
                threshold *= weightRed + weightGreen + weightBlue;
                binarizationFilter.setThreshold(threshold);

                binarization = new ImageBinarization();
                binarization.setColorFilter(binarizationFilter);
            }

            File[] files = inputFolder.listFiles(new FilenameFilterImages());
            int amountOfImages = files.length;
            if (amountOfImages == 0) {
                applicationContext.showWarning("There is no images to render", null);
                return;
            }

            progress = 0;
            ThreadUtils.runLater(new UpdateProgressTask());
            for (int i = 0; i < amountOfImages; i++) {
                File inputFile = files[i];
                String fileName = inputFile.getName();
                BufferedImage image = ImageIO.read(inputFile);

                // Resize
                if (needResize) {
                    image = resize.processImage(image);
                }

                // Binarization
                if (needBinarization) {
                    image = binarization.processImage(image);
                }

                // TODO Ask User on the UI
                String formatName = needBinarization ? "png" : "bmp";
                String outputFileName = FileUtils.getFileName(fileName) + "." + formatName;
                File outputFile = new File(outputFolder, outputFileName );
                ImageIO.write(image, formatName, outputFile);

                // Update Progress
                progress = (double) (i + 1) / amountOfImages;
                ThreadUtils.runLater(new UpdateProgressTask());
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

    public static class SmoothFilterListCell extends ListCell<SmoothFilters> {
        @Override
        protected void updateItem(SmoothFilters item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? null : item.getText());
        }
    }
}
