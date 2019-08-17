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

    public void parseTime(String time) {
        String[] timeParts = time.split(" --> ");
        start = parseSingleTime(timeParts[0]);
        end = parseSingleTime(timeParts[1]);
    }

    private SubtitleTimeDTO parseSingleTime(String timePart) {
        SubtitleTimeDTO subtitleTime = new SubtitleTimeDTO();

        String[] parts = timePart.split(":");
        subtitleTime.setHour(Integer.parseInt(parts[0]));

        subtitleTime.setMinute(Integer.parseInt(parts[1]));

        parts = parts[2].split(",");
        subtitleTime.setSecond(Integer.parseInt(parts[0]));

        subtitleTime.setMillisecond(Integer.parseInt(parts[1]));

        return subtitleTime;
    }

    @Override
    public String toString() {
        String string = getTimeAsString() + ": " + text;
        return string;
    }
}
