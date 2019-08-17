package dto;

public class FileListEntry {
    private String filename;
    private boolean saved;

    public FileListEntry(String filename, boolean saved) {
        this.setFilename(filename);
        this.saved = saved;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
