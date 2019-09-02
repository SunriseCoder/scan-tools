package filters;

public class FilenameFilterVideos extends CustomFilenameFilter {
    private String[] extensions = { "avi", "mp4", "mts", "mkv", "wmv" };

    public FilenameFilterVideos() {
        super();

        addExtensions(extensions);
    }
}
