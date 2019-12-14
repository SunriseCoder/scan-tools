package dto;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TaskParameters {
    private Map<String, Boolean> booleans;
    private Map<String, Class<?>> classes;
    private Map<String, Double> doubles;
    private Map<String, File> files;
    private Map<String, Integer> integers;
    private Map<String, String> strings;

    public TaskParameters() {
        booleans = new HashMap<>();
        classes = new HashMap<>();
        doubles = new HashMap<>();
        integers = new HashMap<>();
        files = new HashMap<>();
        strings = new HashMap<>();
    }

    public boolean getBoolean(String name) {
        boolean value = booleans.get(name);
        return value;
    }

    public void setBoolean(String name, boolean value) {
        booleans.put(name, value);
    }

    public Class<?> getClass(String name) {
        Class<?> value = classes.get(name);
        return value;
    }

    public void setClass(String name, Class<?> value) {
        classes.put(name, value);
    }

    public double getDouble(String name) {
        double value = doubles.get(name);
        return value;
    }

    public void setDouble(String name, double value) {
        doubles.put(name, value);
    }

    public File getFile(String name) {
        File value = files.get(name);
        return value;
    }

    public void setFile(String name, File value) {
        files.put(name, value);
    }

    public int getInt(String name) {
        int value = integers.get(name);
        return value;
    }

    public void setInt(String name, int value) {
        integers.put(name, value);
    }

    public String getString(String name) {
        String value = strings.get(name);
        return value;
    }

    public void setString(String name, String value) {
        strings.put(name, value);
    }
}
