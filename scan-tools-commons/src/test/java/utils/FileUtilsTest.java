package utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FileUtilsTest {

    @Test
    public void testGetFileExtension() {
        assertEquals("txt", FileUtils.getFileExtension("file.txt"));
    }

    @Test
    public void testGetFileName() {
        assertEquals("file", FileUtils.getFileName("file.txt"));
    }
}
