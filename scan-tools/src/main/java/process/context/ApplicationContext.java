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
    private Map<File, MarkupStorage> markupStorages;

    private File workFolder;

    public ApplicationContext(String configFileName) {
        super(configFileName);

        executorService = Executors.newCachedThreadPool();
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

    public synchronized void reloadSelectionBoundaries(File folder) {
        createMarkupStorage(folder);
    }

    public synchronized List<Point> getSelectionBoundaries(File folder, String filename) {
        MarkupStorage markupStorage = markupStorages.get(folder);
        if (markupStorage == null) {
            markupStorage = createMarkupStorage(folder);
        }

        List<Point> selectionBoundaries = markupStorage.getSelectionBoundaries(filename);
        return selectionBoundaries;
    }

    public synchronized void saveSelectionBoundaries(File folder, String filename, List<Point> selectionBoundaries) {
        MarkupStorage markupStorage = markupStorages.get(folder);
        if (markupStorage == null) {
            markupStorage = createMarkupStorage(folder);
        }

        markupStorage.saveSelectionBoundaries(filename, selectionBoundaries);
    }

    private MarkupStorage createMarkupStorage(File folder) {
        MarkupStorage markupStorage = new MarkupStorage(this, folder);
        markupStorages.put(folder, markupStorage);
        return markupStorage;
    }

    public File getWorkFolder() {
        return workFolder;
    }

    private void setWorkFolder(Object value) {
        File workFolder = (File) value;
        this.workFolder = workFolder;
    }

    public synchronized BufferedImage readImage(File sourceFile) throws IOException {
        BufferedImage image = ImageIO.read(sourceFile);
        return image;
    }

    public synchronized boolean writeImage(BufferedImage image, String formatName, File outputFile) throws IOException {
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
