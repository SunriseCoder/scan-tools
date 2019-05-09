package process.dto;

public class SubtitleTimeDTO {
    private int hour;
    private int minute;
    private int second;
    private int millisecond;

    public SubtitleTimeDTO() {
        // Default Constructor
    }

    public SubtitleTimeDTO(int hour, int minute, int second, int millisecond) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millisecond = millisecond;
    }

    public SubtitleTimeDTO(long milliseconds) {
        hour = (int) (milliseconds / 3600000);
        minute = (int) (milliseconds / 60000 % 60);
        second = (int) (milliseconds / 1000 % 60);
        millisecond = (int) (milliseconds % 1000);
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getMillisecond() {
        return millisecond;
    }

    public void setMillisecond(int millisecond) {
        this.millisecond = millisecond;
    }

    public String getAsString() {
        String string = String.format("%02d:%02d:%02d,%03d", hour, minute, second, millisecond);
        return string;
    }

    public long getAsMilliseconds() {
        long milliseconds = 60 * 60 * 1000 * hour + 60 * 1000 * minute + 1000 * second + millisecond;
        return milliseconds;
    }
}
