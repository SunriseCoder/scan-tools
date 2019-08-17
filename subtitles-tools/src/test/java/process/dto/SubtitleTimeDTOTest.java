package process.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SubtitleTimeDTOTest {

    @Test
    public void testSubtitleTimeDTOLong() {
        assertEquals("00:00:00,015", new SubtitleTimeDTO(15).getAsString());
        assertEquals("00:00:05,015", new SubtitleTimeDTO(5015).getAsString());
        assertEquals("00:10:05,015", new SubtitleTimeDTO(605015).getAsString());
        assertEquals("03:10:05,015", new SubtitleTimeDTO(11405015).getAsString());
    }

    @Test
    public void testGetAsString() {
        assertEquals("01:02:03,004", new SubtitleTimeDTO(1, 2, 3, 4).getAsString());
    }

    @Test
    public void testGetAsMilliseconds() {
        assertEquals(3723004, new SubtitleTimeDTO(1, 2, 3, 4).getAsMilliseconds());
    }
}
