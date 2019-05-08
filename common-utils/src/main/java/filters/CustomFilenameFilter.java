package filters;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import utils.FileUtils;

public class CustomFilenameFilter implements FilenameFilter {
    private Set<String> extensions;

    public CustomFilenameFilter() {
        extensions = new HashSet<>();
    }

    protected void addExtensions(String... extensions) {
        this.extensions.addAll(Arrays.asList(extensions));
    }

    @Override
    public boolean accept(File dir, String name) {
        if (new File(dir, name).isDirectory()) {
            return false;
        }

        String extension = FileUtils.getFileExtension(name);
        extension = extension.toLowerCase();
        boolean result = extensions.contains(extension);
        return result;
    }
}
