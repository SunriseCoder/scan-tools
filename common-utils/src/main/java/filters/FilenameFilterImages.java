package filters;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import utils.FileUtils;

public class FilenameFilterImages implements FilenameFilter {
    private String[] extensions = { "bmp", "jpg", "png", "gif", "tif", "tiff" };

    private Set<String> set;

    public FilenameFilterImages() {
        set = new HashSet<>();
        set.addAll(Arrays.asList(extensions));
    }

    @Override
    public boolean accept(File dir, String name) {
        if (new File(dir, name).isDirectory()) {
            return false;
        }

        String extension = FileUtils.getFileExtension(name);
        extension = extension.toLowerCase();
        boolean result = set.contains(extension);
        return result;
    }
}
