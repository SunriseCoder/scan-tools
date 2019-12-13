package multithreading;

import javafx.application.Platform;
import process.context.ApplicationContext;

public abstract class AbstractTask implements Runnable {
    private String name;
    protected ApplicationContext applicationContext;

    public AbstractTask(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        try {
            runWithExceptions();
        } catch (Exception e) {
            Platform.runLater(() -> applicationContext.showError("Error occured in " + name, e));
        }
    }

    protected abstract void runWithExceptions() throws Exception;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
