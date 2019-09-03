package process.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class RawBookParserTest {
    private RawBookParser rawBookParser;

    @Before
    public void beforeTest() {
        rawBookParser = new RawBookParser();
    }

    @Test
    public void testParseCompositeCommand() throws Exception {
        String sourceText = "<a>TextA</a><b c=\"b\">TextB</b>";
        String transformationText = "curr{b c=\"b\"}->toLowerCase;concatToPrevious";

        String parsedText = rawBookParser.parse(sourceText, transformationText);

        assertEquals("<a>TextAtextb</a>\n", parsedText);
    }

    @Test
    public void testParseCommandWithEmptyLinesAndComments() throws Exception {
        String sourceText = "<a>TextA</a><b c=\"b\">TextB</b>";

        String transformationText =
                "curr{a b=\"1\"}->toLowerCase\n" + // Does not touch <a>
                "\n \n# Comment1\n # Comment 2\n" + // Empty Line, Line with trailing space, Comment Lines
                " curr{b c=\"b\"}->toLowerCase;concatToPrevious "; // Affects <b>, command with beginning and trailing spaces

        String parsedText = rawBookParser.parse(sourceText, transformationText);

        assertEquals("<a>TextAtextb</a>\n", parsedText);
    }
}
