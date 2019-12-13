package multithreading;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import process.context.ApplicationContext;

public abstract class AbstractManagerTask implements Runnable {
    private String name;
    protected ApplicationContext applicationContext;
    protected ProgressBar progressBar;

    public AbstractManagerTask(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        try {
            // TODO Lock Start Button before start and unlock after job finished
            // TODO Implement Cancel Button (maybe same button, but change caption)
            runWithExceptions();
            Platform.runLater(() -> applicationContext.showMessage(name + " is done"));
        } catch (Exception e) {
            Platform.runLater(() -> applicationContext.showError("Error occured in " + name, e));
        }
    }

    protected abstract void runWithExceptions() throws Exception;

    public String getName() {
        return name;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }
}
