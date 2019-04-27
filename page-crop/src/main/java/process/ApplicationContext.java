package process;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import process.dto.EventListener;
import process.dto.Point;
import utils.JSONUtils;

public class ApplicationContext {
    public enum Parameters {
        SplitPaneDivider,
        StartFolder
    }

    public enum Events {
        // Image
        CenterImage,
        SaveImage,

        // Control
        SensorControl,

        // Files
        WorkFileSelected,
        WorkFileSelectNext,
        WorkFolderChanged,
        WorkFolderRefresh
    }

    private File configurationFile;

    private Map<Parameters, String> storage;
    private Map<File, MarkupStorage> markupStorages;
    private Map<Events, Set<EventListener>> eventListeners;

    private File workFolder;

    public ApplicationContext() {
        configurationFile = new File("page-crop-config.json");
        storage = new HashMap<>();
        markupStorages = new HashMap<>();
        eventListeners = new HashMap<>();
        subscribe();
        loadData();
    }

    private void subscribe() {
        addEventListener(Events.WorkFolderChanged, value -> setWorkFolder(value));
    }

    private void loadData() {
        if (!configurationFile.exists()) {
            return;
        }

        try {
            TypeReference<Map<Parameters, String>> typeReference = new TypeReference<Map<Parameters, String>>() {};
            Map<Parameters, String> data = JSONUtils.loadFromDisk(configurationFile, typeReference);
            storage.putAll(data);
        } catch (Exception e) {
            showError("Error due to load data from configuration file " + configurationFile.getAbsolutePath(), e);
        }
    }

    public String getParameterValue(Parameters parameter) {
        String value = storage.get(parameter);
        return value;
    }

    public void setParameterValue(Parameters parameter, String value) {
        storage.put(parameter, value);
        saveStorageToDisk();
    }

    private void saveStorageToDisk() {
        try {
            JSONUtils.saveToDisk(storage, configurationFile);
        } catch (Exception e) {
            showError("Error due to save configuration to disk", e);
        }
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

    public void addEventListener(Events event, EventListener listener) {
        Set<EventListener> listeners = eventListeners.get(event);
        if (listeners == null) {
            listeners = new HashSet<>();
            eventListeners.put(event, listeners);
        }

        listeners.add(listener);
    }

    public void fireEvent(Events event, Object value) {
        Set<EventListener> listeners = eventListeners.get(event);
        if (listeners == null) {
            return;
        }

        listeners.forEach(listener -> {
            System.out.println(event + " -> " + listener);
            listener.fireEvent(value);
        });

    }

    public File getWorkFolder() {
        return workFolder;
    }

    private void setWorkFolder(Object value) {
        File workFolder = (File) value;
        this.workFolder = workFolder;
    }

    public void showMessage(String message) {
        showMessage(AlertType.INFORMATION, message, null);
    }

    public void showError(String message, Exception e) {
        showMessage(AlertType.ERROR, message, e);
    }

    public void showWarning(String message, Exception e) {
        showMessage(AlertType.WARNING, message, e);
    }

    // TODO Improve this method to be able to see stacktrace somehow (log or on UI)
    private void showMessage(AlertType alertType, String message, Exception e) {
        if (e != null) {
            message += ": " + e.getMessage();
            e.printStackTrace();
        }
        showMessageOnUI(alertType, message);
    }

    private void showMessageOnUI(AlertType alertType, String message) {
        Platform.runLater(() -> new Alert(alertType, message).show());
    }
}
