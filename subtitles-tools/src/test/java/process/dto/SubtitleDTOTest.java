package process.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SubtitleDTOTest {

    @Test
    public void testGetTimeAsString() {
        SubtitleTimeDTO start = new SubtitleTimeDTO(12, 34, 56, 789);
        SubtitleTimeDTO end = new SubtitleTimeDTO(3, 57, 1, 65);
        SubtitleDTO subtitle = new SubtitleDTO(start , end, "Stub");

        assertEquals("12:34:56,789 --> 03:57:01,065", subtitle.getTimeAsString());
    }

    @Test
    public void testParseTime() {
        String time = "12:34:56,789 --> 03:57:01,065";

        SubtitleDTO subtitle = new SubtitleDTO();
        subtitle.parseTime(time);

        assertEquals(time, subtitle.getTimeAsString());

        SubtitleTimeDTO start = subtitle.getStart();
        assertEquals(12, start.getHour());
        assertEquals(34, start.getMinute());
        assertEquals(56, start.getSecond());
        assertEquals(789, start.getMillisecond());

        SubtitleTimeDTO end = subtitle.getEnd();
        assertEquals(3, end.getHour());
        assertEquals(57, end.getMinute());
        assertEquals(1, end.getSecond());
        assertEquals(65, end.getMillisecond());
    }
}
