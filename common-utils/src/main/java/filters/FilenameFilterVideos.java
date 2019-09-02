package filters;

public class FilenameFilterVideos extends CustomFilenameFilter {
    private static String[] VIDEO_EXTENSIONS = { "avi", "mp4", "mts", "mkv", "wmv" };

    public FilenameFilterVideos() {
        super(VIDEO_EXTENSIONS);
    }
}
