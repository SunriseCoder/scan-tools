package filters;

public class FilenameFilterVideos extends CustomFilenameFilter {
    private static String[] VIDEO_EXTENSIONS = { "avi", "mkv", "mov", "mp4", "mts", "wmv" };

    public FilenameFilterVideos() {
        super(VIDEO_EXTENSIONS);
    }
}
