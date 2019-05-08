package filters;

public class FilenameFilterImages extends CustomFilenameFilter {
    private String[] extensions = { "bmp", "jpg", "png", "gif", "tif", "tiff" };

    public FilenameFilterImages() {
        super();

        addExtensions(extensions);
    }
}
