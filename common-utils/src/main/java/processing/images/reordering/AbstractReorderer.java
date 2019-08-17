package processing.images.reordering;

import java.io.IOException;

import context.AbstractApplicationContext;

public abstract class AbstractReorderer {
    private AbstractApplicationContext<Enum<?>, Enum<?>> applicationContext;

    public void setApplicationContext(AbstractApplicationContext<Enum<?>, Enum<?>> applicationContext) {
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
