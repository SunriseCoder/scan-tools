package crop;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import crop.dto.Point;
import utils.JSONUtils;

public class MarkupStorage {
    private Map<String, FileEntry> storage;
    private File storageFile;

    public MarkupStorage(File folder) {
        storageFile = new File(folder, "page-crop.json");
        storage = new TreeMap<>();
        loadData();
    }

    private void loadData() {
        if (!storageFile.exists()) {
            return;
        }

        try {
            TypeReference<List<FileEntry>> typeReference = new TypeReference<List<FileEntry>>() {};
            List<FileEntry> data = JSONUtils.loadFromDisk(storageFile, typeReference);
            storage.putAll(data.stream().collect(Collectors.toMap(e -> e.filename, e -> e)));
        } catch (Exception e) {
            System.out.println("Error due to load data from storage file " + storageFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public List<FileEntry> getAllBoundaries() {
        return new ArrayList<>(storage.values());
    }

    public List<Point> getSelectionBoundaries(String filename) {
        FileEntry entry = storage.get(filename);
        List<Point> result = entry == null ? null : entry.points;
        return result;
    }

    public void saveInfo(String filename, List<Point> points) {
        storage.put(filename, new FileEntry(filename, points, new Date()));
        saveStorageToDisk();
    }

    private void saveStorageToDisk() {
        try {
            JSONUtils.saveToDisk(storage.values(), storageFile);
        } catch (Exception e) {
            // TODO Show this exception to user
            throw new RuntimeException(e);
        }
    }

    public static class FileEntry {
        public String filename;
        public List<Point> points;
        public Date date;

        public FileEntry() {

        }

        public FileEntry(String filename, List<Point> points, Date date) {
            this.filename = filename;
            this.points = points;
            this.date = date;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "[filename=" + filename +
                    ", points=" + points + ", date=" + date + "]";
        }
    }
}
