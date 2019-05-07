package process.processing.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

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
import process.dto.Point;
import process.processing.AbstractNode;
import process.processing.render.binarization.ImageBinarization;
import process.processing.render.crop.AbstractImageCrop;
import process.processing.render.crop.RotationImageCrop;
import process.processing.render.crop.SimpleCrop;
import process.processing.render.filters.AbstractImageFilter;
import process.processing.render.filters.BilinearFilter;
import process.processing.render.filters.BinarizationFilter;
import process.processing.render.filters.ImageFilter;
import process.processing.render.filters.RoughFilter;
import process.processing.render.merge.ImageMerge;
import process.processing.render.resize.ImageResize;
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
    private CheckBox imageCropCheckBox;
    @FXML
    private ComboBox<ImageCrops> imageCropComboBox;

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
    private CheckBox imageMergeCheckBox;
    @FXML
    private ComboBox<ImageMergeMethods> imageMergeComboBox;

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
        initComboBox(imageCropComboBox, ImageCropListCell.class, ImageCrops.values());

        sourceDPITextField.setText(DEFAULT_RESIZE_SOURCE_DPI);
        targetDPITextField.setText(DEFAULT_RESIZE_TARGET_DPI);

        thresholdTextField.setText(DEFAULT_BINARIZATION_THRESHOLD);
        weightRedTextField.setText(DEFAULT_BINARIZATION_WEIGHT_RED);
        weightGreenTextField.setText(DEFAULT_BINARIZATION_WEIGHT_GREEN);
        weightBlueTextField.setText(DEFAULT_BINARIZATION_WEIGHT_BLUE);

        initComboBox(imageMergeComboBox, ImageMergeListCell.class, ImageMergeMethods.values());
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
            boolean needCrop = imageCropCheckBox.isSelected();
            boolean needResize = imageResizeCheckBox.isSelected();
            boolean needBinarization = imageBinarizationCheckBox.isSelected();
            boolean needMerge = imageMergeCheckBox.isSelected();

            File inputFolder = applicationContext.getWorkFolder();
            File outputFolder = new File(inputFolder, "rendered");
            outputFolder.mkdir();

            applicationContext.reloadSelectionBoundaries(applicationContext.getWorkFolder());

            SmoothFilters selectedSmoothFilter = smoothFilterComboBox.getSelectionModel().getSelectedItem();
            ImageCrops selectedImageCrop = imageCropComboBox.getSelectionModel().getSelectedItem();

            // Preparing processors
            ImageFilter smoothFilter = selectedSmoothFilter.cl.newInstance();

            AbstractImageCrop imageCrop = null;
            if (needCrop) {
                imageCrop = selectedImageCrop.cl.newInstance();
                imageCrop.setSmoothFilter(smoothFilter);
            }

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

            ImageMerge merge = null;
            if (needMerge) {
                merge = new ImageMerge();
                ImageMergeMethods mergeMethod = imageMergeComboBox.getSelectionModel().getSelectedItem();
                merge.setMergeMethod(mergeMethod);
            }

            File[] files = inputFolder.listFiles(new FilenameFilterImages());
            int amountOfImages = files.length;
            if (amountOfImages == 0) {
                applicationContext.showWarning("There is no images to render", null);
                return;
            }

            BufferedImage previousImage = null;
            int mergeCounter = 1;
            String fileNameBase = createFileNameBase(files[0].getName());
            boolean sentNoBoundariesWarning = false;
            progress = 0;
            ThreadUtils.runLater(new UpdateProgressTask());
            for (int i = 0; i < amountOfImages; i++) {
                File inputFile = files[i];
                BufferedImage image = ImageIO.read(inputFile);

                // Crop
                String fileName = inputFile.getName();
                if (needCrop) {
                    List<Point> boundaries = applicationContext.getSelectionBoundaries(inputFolder, fileName);
                    if (boundaries == null) {
                        if (!sentNoBoundariesWarning) {
                            applicationContext.showWarning("Not all Images has been Marked Up", null);
                        }
                    } else {
                        image = imageCrop.processImage(image, boundaries);
                    }
                }

                // Resize
                if (needResize) {
                    image = resize.processImage(image);
                }

                // Binarization
                if (needBinarization) {
                    image = binarization.processImage(image);
                }

                // Merge Images
                if (needMerge) {
                    int remainder;
                    switch (merge.getMergeMethod()) {
                        case Method1ImageOnFirstPage:
                            remainder = 0;
                            break;
                        case Method2ImagesOnFirstPage:
                            remainder = 1;
                            break;
                        default:
                            throw new IllegalArgumentException("Merge Method is not supported: " + merge.getMergeMethod());
                    }

                    if (i % 2 == remainder) {
                        image = merge.mergeImages(previousImage, image);
                        saveMergedImage(image, outputFolder, fileNameBase, mergeCounter++);
                        previousImage = null;
                    } else {
                        previousImage = image;
                    }
                } else {
                    // TODO Ask User on the UI
                    String formatName = needBinarization ? "png" : "bmp";
                    String outputFileName = FileUtils.getFileName(fileName) + "." + formatName;
                    File outputFile = new File(outputFolder, outputFileName );
                    ImageIO.write(image, formatName, outputFile);
                }

                // Update Progress
                progress = (double) (i + 1) / amountOfImages;
                ThreadUtils.runLater(new UpdateProgressTask());
            }

            if (needMerge && previousImage != null) {
                BufferedImage image = merge.mergeImages(previousImage, null);
                saveMergedImage(image, outputFolder, fileNameBase, mergeCounter++);
            }
        }

        private String createFileNameBase(String fileName) {
            String base = FileUtils.getFileName(fileName);
            base = base.replaceAll("[0-9]+$", "");
            return base;
        }

        private void saveMergedImage(BufferedImage image, File outputFolder, String fileNameBase, int counter)
                throws IOException {
            // TODO Ask User on UI for the output format
            String formatName = "png";
            String mergedFileName = fileNameBase + String.format("%04d", counter) + "." + formatName;
            File outputFile = new File(outputFolder, mergedFileName);
            ImageIO.write(image, formatName, outputFile);
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

    public static enum SmoothFilters {
        BilinearFilter("Bilinear Filter", BilinearFilter.class),
        RoughFilter("Rough Filter", RoughFilter.class);

        private String text;
        private Class<? extends AbstractImageFilter> cl;

        private SmoothFilters(String text, Class<? extends AbstractImageFilter> cl) {
            this.text = text;
            this.cl = cl;
        }

        public String getText() {
            return text;
        }

        public Class<? extends AbstractImageFilter> getCl() {
            return cl;
        }
    }

    public static enum ImageCrops {
        RotationImageCrop("Rotate and crop", RotationImageCrop.class),
        SimpleImageCrop("Simple crop", SimpleCrop.class);

        private String text;
        private Class<? extends AbstractImageCrop> cl;

        private ImageCrops(String text, Class<? extends AbstractImageCrop> cl) {
            this.text = text;
            this.cl = cl;
        }

        public String getText() {
            return text;
        }

        public Class<? extends AbstractImageCrop> getCl() {
            return cl;
        }
    }

    public static enum ImageMergeMethods {
        Method1ImageOnFirstPage("1 Image"),
        Method2ImagesOnFirstPage("2 Images");

        private String text;

        private ImageMergeMethods(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public static class SmoothFilterListCell extends ListCell<SmoothFilters> {
        @Override
        protected void updateItem(SmoothFilters item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? null : item.getText());
        }
    }

    public static class ImageCropListCell extends ListCell<ImageCrops> {
        @Override
        protected void updateItem(ImageCrops item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? null : item.getText());
        }
    }

    public static class ImageMergeListCell extends ListCell<ImageMergeMethods> {
        @Override
        protected void updateItem(ImageMergeMethods item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? null : item.getText());
        }
    }
}
