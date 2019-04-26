package process.reordering.methods;

import java.io.File;
import java.io.IOException;
import java.util.List;

import filters.FilenameFilterImages;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import process.ApplicationContext;
import utils.FileUtils;

public abstract class AbstractReorderingTask implements Runnable {
    private ApplicationContext applicationContext;

    private File inputFolder;
    private File outputFolder;

    protected double progress;

    private ProgressBar progressBar;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setInputFolder(File inputFolder) {
        this.inputFolder = inputFolder;
    }

    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void run() {
        try {
            tryRun();
            applicationContext.showMessage("Reordering process is done");
        } catch (IOException e) {
            applicationContext.showError("Exception during the copying of the files", e);
        }
    }

    private void tryRun() throws IOException {
        File[] files = inputFolder.listFiles(new FilenameFilterImages());
        int amountOfPages = files.length;

        List<Integer> reorderedPagesIndices = getReorderedList(amountOfPages);

        for (int i = 0; i < amountOfPages; i++) {
            Integer reorderedPagesIndex = reorderedPagesIndices.get(i);

            File sourceFile = files[i];
            String destinationFileName = files[reorderedPagesIndex - 1].getName();
            File destinationFile = new File(outputFolder, destinationFileName);

            FileUtils.copyFiles(sourceFile, destinationFile);

            progress = (double) (i + 1) / amountOfPages;
            Platform.runLater(new UpdateProgressTask());
        }
    }

    protected abstract List<Integer> getReorderedList(int amountOfPages);

    private class UpdateProgressTask implements Runnable {
        @Override
        public void run() {
            if (progressBar != null) {
                progressBar.setProgress(progress);
            }
        }
    }
}
