package process.processing.prepare;

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
import process.context.ApplicationContext;
import process.processing.AbstractNode;
import processing.images.reordering.AbstractReorderer;
import processing.images.reordering.Reordering4PagesOn1SheetFromMiddle;
import processing.images.rotation.AbstractRotator;
import processing.images.rotation.RotationOdd180Degrees;
import utils.FileUtils;

public class PrepareNode extends AbstractNode {
    private ApplicationContext applicationContext;

    @FXML
    private CheckBox reorderingCheckBox;
    @FXML
    private ComboBox<ReorderingMethods> reorderingComboBox;

    @FXML
    private CheckBox rotationCheckBox;
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
        initComboBox(reorderingComboBox, ReorderingMethodsListCell.class, ReorderingMethods.values());
        initComboBox(rotationComboBox, RotationMethodsListCell.class, RotationMethods.values());
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
                Platform.runLater(() -> applicationContext.showMessage("Prepare Images is done"));
            } catch (Exception e) {
                Platform.runLater(() -> applicationContext.showError("Error due to Prepare Images", e));
            }
        }

        private void runWithExceptions() throws Exception {
            boolean needReordering = reorderingCheckBox.isSelected();
            boolean needRotation = rotationCheckBox.isSelected();
            ReorderingMethods reorderingMethod = reorderingComboBox.getSelectionModel().getSelectedItem();
            RotationMethods rotationMethod = rotationComboBox.getSelectionModel().getSelectedItem();

            if (needReordering && reorderingMethod == null || needRotation && rotationMethod == null) {
                return;
            }

            AbstractReorderer reorderer = null;
            if (needReordering) {
                reorderer = reorderingMethod.cl.newInstance();

            }

            AbstractRotator rotator = null;
            if (needRotation) {
                rotator = rotationMethod.cl.newInstance();
            }

            File inputFolder = applicationContext.getWorkFolder();
            File outputFolder = new File(inputFolder, "prepared");
            outputFolder.mkdir();

            File[] files = inputFolder.listFiles(new FilenameFilterImages());
            int amountOfImages = files.length;

            for (int i = 0; i < amountOfImages; i++) {
                int sourceIndex = i;
                int destinationIndex = i;

                if (needReordering) {
                    sourceIndex = reorderer.getReorderedPageNumber(sourceIndex, amountOfImages);
                }

                File sourceFile = files[sourceIndex];
                BufferedImage image = ImageIO.read(sourceFile);

                if (needRotation) {
                    image = rotator.rotateImage(image, destinationIndex);
                }

                String outputFileName = files[destinationIndex].getName();
                File outputFile = new File(outputFolder, outputFileName);
                String formatName = FileUtils.getFileExtension(outputFileName);
                ImageIO.write(image, formatName, outputFile);

                progress = (double) (i + 1) / amountOfImages;
                Platform.runLater(new UpdateProgressTask());
            }
        }
    }

    public static enum ReorderingMethods {
        Method4PagesOn1SheetFromMiddle("4 pages, from middle", Reordering4PagesOn1SheetFromMiddle.class);

        private String text;
        private Class<? extends AbstractReorderer> cl;

        private ReorderingMethods(String text, Class<? extends AbstractReorderer> cl) {
            this.text = text;
            this.cl = cl;
        }

        public String getText() {
            return text;
        }

        public Class<? extends AbstractReorderer> getCl() {
            return cl;
        }
    }

    public static enum RotationMethods {
        Odd180Degrees("Odd pages 180 Degrees", RotationOdd180Degrees.class);

        private String text;
        private Class<? extends AbstractRotator> cl;

        private RotationMethods(String text, Class<? extends AbstractRotator> cl) {
            this.text = text;
            this.cl = cl;
        }

        public String getText() {
            return text;
        }

        public Class<? extends AbstractRotator> getCl() {
            return cl;
        }
    }

    public static class ReorderingMethodsListCell extends ListCell<ReorderingMethods> {
        @Override
        protected void updateItem(ReorderingMethods item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? null : item.getText());
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
