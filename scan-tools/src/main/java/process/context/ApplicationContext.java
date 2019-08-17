package process.context;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import context.AbstractApplicationContext;
import dto.Point;
import process.MarkupStorage;

public class ApplicationContext extends AbstractApplicationContext<ApplicationParameters, ApplicationEvents> {
    private Map<File, MarkupStorage> markupStorages;

    private File workFolder;

    public ApplicationContext(String configFileName) {
        super(configFileName);

        markupStorages = new HashMap<>();
    }

    @Override
    protected void subscribe() {
        addEventListener(ApplicationEvents.WorkFolderChanged, value -> setWorkFolder(value));
    }

    public void reloadSelectionBoundaries(File folder) {
        createMarkupStorage(folder);
    }

    public List<Point> getSelectionBoundaries(File folder, String filename) {
        MarkupStorage markupStorage = markupStorages.get(folder);
        if (markupStorage == null) {
            markupStorage = createMarkupStorage(folder);
        }

        List<Point> selectionBoundaries = markupStorage.getSelectionBoundaries(filename);
        return selectionBoundaries;
    }

    public void saveSelectionBoundaries(File folder, String filename, List<Point> selectionBoundaries) {
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
}
