package process.context;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import context.AbstractApplicationContext;
import dto.Point;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import process.MarkupStorage;

public class ApplicationContext extends AbstractApplicationContext<ApplicationParameters, ApplicationEvents> {
    private ExecutorService executorService;
    private Map<String, MarkupStorage> markupStorages;

    private File workFolder;

    public ApplicationContext(String configFileName) {
        super(configFileName);

        int numberOfCores = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(numberOfCores * 2);
        markupStorages = new HashMap<>();
    }

    public synchronized Future<?> submitTask(Runnable task) {
        Future<?> future = executorService.submit(task);
        return future;
    }

    @Override
    protected void subscribe() {
        addEventListener(ApplicationEvents.WorkFolderChanged, value -> setWorkFolder(value));
    }

    public void reloadSelectionBoundaries(File folder) {
        synchronized (markupStorages) {
            createMarkupStorage(folder);
        }
    }

    public List<Point> getSelectionBoundaries(File folder, String filename) {
        synchronized (markupStorages) {
            MarkupStorage markupStorage = markupStorages.get(folder.getAbsolutePath());
            if (markupStorage == null) {
                markupStorage = createMarkupStorage(folder);
            }

            List<Point> selectionBoundaries = markupStorage.getSelectionBoundaries(filename);
            return selectionBoundaries;
        }
    }

    public void saveSelectionBoundaries(File folder, String filename, List<Point> selectionBoundaries) {
        synchronized (markupStorages) {
            MarkupStorage markupStorage = markupStorages.get(folder.getAbsolutePath());
            if (markupStorage == null) {
                markupStorage = createMarkupStorage(folder);
            }

            markupStorage.saveSelectionBoundaries(filename, selectionBoundaries);
        }
    }

    private MarkupStorage createMarkupStorage(File folder) {
        synchronized (markupStorages) {
            MarkupStorage markupStorage = new MarkupStorage(this, folder);
            markupStorages.put(folder.getAbsolutePath(), markupStorage);
            return markupStorage;
        }
    }

    public File getWorkFolder() {
        return workFolder;
    }

    private void setWorkFolder(Object value) {
        File workFolder = (File) value;
        this.workFolder = workFolder;
    }

    public BufferedImage readImage(File sourceFile) throws IOException {
        BufferedImage image = ImageIO.read(sourceFile);
        return image;
    }

    public boolean writeImage(BufferedImage image, String formatName, File outputFile) throws IOException {
        boolean result = ImageIO.write(image, formatName, outputFile);
        return result;
    }

    public synchronized void updateProgress(ProgressBar progressBar, double progress) {
        Platform.runLater(new UpdateProgressTask(progressBar, progress));
    }

    private static class UpdateProgressTask implements Runnable {
        private ProgressBar progressBar;
        private double progress;

        private UpdateProgressTask(ProgressBar progressBar, double progress) {
            this.progressBar = progressBar;
            this.progress = progress;
        }

        @Override
        public void run() {
            if (progressBar != null) {
                progressBar.setProgress(progress);
            }
        }
    }
}
