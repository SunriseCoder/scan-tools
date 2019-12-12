package process.processing.merge;

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
import javafx.scene.layout.GridPane;
import process.context.ApplicationContext;
import process.handlers.SmoothFilters;
import process.processing.AbstractNode;
import processing.images.merge.ImageMerge;
import utils.FileUtils;
import utils.ThreadUtils;

public class MergeNode extends AbstractNode {
    private ApplicationContext applicationContext;

    @FXML
    private GridPane gridPane;

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
                Platform.runLater(() -> applicationContext.showMessage("Merge Images is done"));
            } catch (Exception e) {
                Platform.runLater(() -> applicationContext.showError("Error due to Merge Images", e));
            }
        }

        private void runWithExceptions() throws Exception {
            File inputFolder = applicationContext.getWorkFolder();
            File outputFolder = new File(inputFolder, "merged");
            outputFolder.mkdir();

            applicationContext.reloadSelectionBoundaries(applicationContext.getWorkFolder());

            ImageMerge merge = new ImageMerge();
            ImageMergeMethods mergeMethod = imageMergeComboBox.getSelectionModel().getSelectedItem();

            File[] files = inputFolder.listFiles(new FilenameFilterImages());
            int amountOfImages = files.length;
            if (amountOfImages == 0) {
                applicationContext.showWarning("There is no images to render", null);
                return;
            }

            BufferedImage previousImage = null;
            int mergeCounter = 1;
            String fileNameBase = createFileNameBase(files[0].getName());
            progress = 0;
            ThreadUtils.runLater(new UpdateProgressTask());
            for (int i = 0; i < amountOfImages; i++) {
                File inputFile = files[i];
                BufferedImage image = ImageIO.read(inputFile);

                // Merge Images
                int remainder;
                switch (mergeMethod) {
                    case Method1ImageOnFirstPage:
                        remainder = 0;
                        break;
                    case Method2ImagesOnFirstPage:
                        remainder = 1;
                        break;
                    default:
                        throw new IllegalArgumentException("Merge Method is not supported: " + mergeMethod);
                }

                if (i % 2 == remainder) {
                    image = merge.mergeImages(previousImage, image);
                    saveMergedImage(image, outputFolder, fileNameBase, mergeCounter++);
                    previousImage = null;
                } else {
                    previousImage = image;
                }

                // Update Progress
                progress = (double) (i + 1) / amountOfImages;
                ThreadUtils.runLater(new UpdateProgressTask());
            }

            if (previousImage != null) {
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

    public enum ImageMergeMethods {
        Method1ImageOnFirstPage("On the First page should be 1 Image"),
        Method2ImagesOnFirstPage("On the First page should be 2 Images");

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

    public static class ImageMergeListCell extends ListCell<ImageMergeMethods> {
        @Override
        protected void updateItem(ImageMergeMethods item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? null : item.getText());
        }
    }
}
