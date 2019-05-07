package process.processing.prepare.reordering;

import java.io.IOException;

import process.context.ApplicationContext;

public abstract class AbstractReorderer {
    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void run() {
        try {
            tryRun();
            applicationContext.showMessage("Reordering process is done");
        } catch (IOException e) {
            applicationContext.showError("Exception during the copying of the files", e);
        }
    }

    private void tryRun() throws IOException {

    }

    public abstract int getReorderedPageNumber(int index, int amountOfPages);
}
