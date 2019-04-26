package process.processing.preprocessing.reordering;

import java.io.IOException;
import java.util.List;

import process.ApplicationContext;

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

    // TODO Get rid of List method, because they work opposite way, be careful
    public abstract List<Integer> getReorderedList(int amountOfPages);
    public abstract int getReorderedPageNumber(int index, int amountOfPages);
}
