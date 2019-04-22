package crop;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import utils.JSONUtils;

public class SystemConfiguration {
    public enum Parameters {
        SplitPaneDivider,
        StartFolder
    }

    private File configurationFile;
    private Map<Parameters, String> storage;

    public SystemConfiguration() {
        configurationFile = new File("page-crop-config.json");
        storage = new HashMap<>();
        loadData();
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
            System.out.println("Error due to load data from configuration file " + configurationFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public String getValue(Parameters parameter) {
        String value = storage.get(parameter);
        return value;
    }

    public void setValue(Parameters parameter, String value) {
        storage.put(parameter, value);
        saveStorageToDisk();
    }

    private void saveStorageToDisk() {
        try {
            JSONUtils.saveToDisk(storage, configurationFile);
        } catch (Exception e) {
            // TODO Show this exception to user
            throw new RuntimeException(e);
        }
    }
}
