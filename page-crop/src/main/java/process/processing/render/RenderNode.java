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
import javafx.scene.control.ProgressBar;
import process.ApplicationContext;
import process.dto.Point;
import process.processing.render.binarization.ImageBinarization;
import process.processing.render.crop.ImageCrop;
import process.processing.render.filters.BilinearFilter;
import process.processing.render.filters.BinarizationFilter;
import process.processing.render.filters.ImageFilter;
import process.processing.render.merge.ImageMerge;
import process.processing.render.resize.ImageResize;
import utils.FileUtils;

public class RenderNode {
    private ApplicationContext applicationContext;

    @FXML
    private ProgressBar progressBar;

    private double progress;

    public Node init(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;
        Parent rootNode = FileUtils.loadFXML(this);
        return rootNode;
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
            File inputFolder = applicationContext.getWorkFolder();
            File outputFolder = new File(inputFolder, "rendered");
            outputFolder.mkdir();

            applicationContext.reloadSelectionBoundaries(applicationContext.getWorkFolder());

            // Preparing processors
            // TODO Put selection on UI among with RoughFilter
            ImageFilter smoothFilter = new BilinearFilter();
            ImageFilter binarizationFilter = new BinarizationFilter();

            ImageCrop crop = new ImageCrop();
            crop.setSmoothFilter(smoothFilter);

            ImageResize resize = new ImageResize();
            resize.setSmoothFilter(smoothFilter);

            ImageBinarization binarization = new ImageBinarization();
            binarization.setColorFilter(binarizationFilter);

            ImageMerge merge = new ImageMerge();

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
            for (int i = 0; i < amountOfImages; i++) {
                File inputFile = files[i];
                File outputFile = inputFile;
                BufferedImage image = ImageIO.read(inputFile);

                // Crop
                // TODO Add CheckBox for this operation to UI
                List<Point> boundaries = applicationContext.getSelectionBoundaries(inputFolder, inputFile.getName());
                if (boundaries == null) {
                    if (!sentNoBoundariesWarning) {
                        applicationContext.showWarning("Not all Images has been Marked Up", null);
                    }
                } else {
                    image = crop.processImage(image, boundaries);
                }

                // Resize
                // TODO Add CheckBox for this operation to UI
                // TODO Also Add Source and Target DPI to UI
                image = resize.processImage(image, 600, 400);

                // Binarization
                // TODO Add CheckBox for this operation to UI
                // TODO Also Add Filter Parameters to UI (threshold and color weights)
                image = binarization.processImage(image);

                // Merge Images
                // TODO Add CheckBox for this operation to UI
                // TODO Also Add how many empty pages to add at beginning (0 or 1)
                if (i % 2 == 0) {
                    image = merge.mergeImages(previousImage, image);
                    saveMergedImage(image, outputFolder, fileNameBase, mergeCounter++);

                    previousImage = null;
                } else {
                    previousImage = image;
                }

                // Update Progress
                progress = (double) (i + 1) / amountOfImages;
                Platform.runLater(new UpdateProgressTask());
            }

            // TODO Add CheckBox's result for this operation from UI to this if-statement
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
}
