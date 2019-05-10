package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

    @Test
    void testTrimEndSymbols() {
        assertEquals(null, StringUtils.trimEndSymbols(null, null));
        assertEquals("", StringUtils.trimEndSymbols("", ""));

        assertEquals("text", StringUtils.trimEndSymbols("text", ""));

        assertEquals("tex", StringUtils.trimEndSymbols("text", "t"));
        assertEquals("tex", StringUtils.trimEndSymbols("texttt", "t"));

        assertEquals("text", StringUtils.trimEndSymbols("text\n", "\n"));
        assertEquals("text", StringUtils.trimEndSymbols("text\n\n\n", "\n"));
    }
}
