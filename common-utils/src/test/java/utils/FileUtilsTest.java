package utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FileUtilsTest {

    @Test
    public void testGetFileExtension() {
        assertEquals("txt", FileUtils.getFileExtension("file.txt"));
    }

    @Test
    public void testGetFileExtensionWithNoDot() {
        assertEquals("filetxt", FileUtils.getFileExtension("filetxt"));
    }

    @Test
    public void testGetFileExtensionWithNoName() {
        assertEquals("filetxt", FileUtils.getFileExtension(".filetxt"));
    }

    @Test
    public void testGetFileName() {
        assertEquals("file", FileUtils.getFileName("file.txt"));
    }

    @Test
    public void testGetFileNameWithNoName() {
        assertEquals("", FileUtils.getFileName(".txt"));
    }
}
