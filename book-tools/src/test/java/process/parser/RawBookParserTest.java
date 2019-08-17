package process.parser;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

public class RawBookParserTest {
    private RawBookParser rawBookParser;

    @Before
    public void beforeTest() {
        rawBookParser = new RawBookParser();
    }

    @Test
    public void testParseFindByTagAndDelete() throws ParseException {
        String sourceText = "<a>TextA</a><b>TextB</b><c>TextC</c>";
        String transformationText = "curr{b}->delete";

        String parsedText = rawBookParser.parse(sourceText, transformationText);

        assertEquals("<a>TextA</a>\n<c>TextC</c>\n", parsedText);
    }

    @Test
    public void testParseCompositeCommand() throws ParseException {
        String sourceText = "<a>TextA</a><b c=\"b\">TextB</b>";
        String transformationText = "curr{b c=\"b\"}->toLowerCase;concatToPrevious";

        String parsedText = rawBookParser.parse(sourceText, transformationText);

        assertEquals("<a>TextAtextb</a>\n", parsedText);
    }

    @Test
    public void testParseCommandWithEmptyLinesAndComments() throws ParseException {
        String sourceText = "<a>TextA</a><b c=\"b\">TextB</b>";

        String transformationText = "curr{a b=\"1\"}->toLowerCase\n" // Does not touch <a>
                + "\n \n# Comment1\n # Comment 2\n" // Empty Line, Line with trailing space, Comment Lines
                + " curr{b c=\"b\"}->toLowerCase;concatToPrevious "; // Affects <b>, command with beginning and trailing spaces

        String parsedText = rawBookParser.parse(sourceText, transformationText);

        assertEquals("<a>TextAtextb</a>\n", parsedText);
    }
}
