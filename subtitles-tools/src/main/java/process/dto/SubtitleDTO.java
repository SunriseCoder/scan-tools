package process.dto;

public class SubtitleDTO {
    private SubtitleTimeDTO start;
    private SubtitleTimeDTO end;
    private String text;

    public SubtitleDTO() {
        // Default constructor
    }

    public SubtitleDTO(SubtitleTimeDTO start, SubtitleTimeDTO end, String text) {
        this.start = start;
        this.end = end;
        this.text = text;
    }

    public SubtitleTimeDTO getStart() {
        return start;
    }

    public void setStart(SubtitleTimeDTO start) {
        this.start = start;
    }

    public SubtitleTimeDTO getEnd() {
        return end;
    }

    public void setEnd(SubtitleTimeDTO end) {
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimeAsString() {
        String string = start.getAsString() + " --> " + end.getAsString();
        return string;
    }
}
