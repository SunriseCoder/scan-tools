package process.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import process.parser.dto.Chain;
import process.parser.dto.Command;
import process.parser.dto.html.HtmlElement;

public class TransformatorTest {
    private HtmlParser htmlParser;
    private CommandParser commandParser;
    private Transformator transformator;

    @Before
    public void beforeTest() {
        htmlParser = new HtmlParser();
        commandParser = new CommandParser();
        transformator = new Transformator();
    }

    @Test
    public void testIsElementMatches() throws ParseException {
        assertTrue(isElementMatches("<span>", "curr{span}->stub"));
        assertFalse(isElementMatches("<span class=\"c1\">", "curr{span}->stub"));
        assertTrue(isElementMatches("<span class=\"c1\">", "curr{span class=\"c1\"}->stub"));
        assertFalse(isElementMatches("<span>", "curr{span class=\"c1\"}->stub"));
        assertFalse(isElementMatches("<span class=\"c1\" style=\"s1\">", "curr{span class=\"c1\"}->stub"));
        assertTrue(isElementMatches("<span class=\"c1\" style=\"s1\">", "curr{span class=\"c1\" style=\"s1\"}->stub"));
    }

    private boolean isElementMatches(String sourceText, String transformationText) throws ParseException {
        Chain<HtmlElement> chain = htmlParser.parse(sourceText);
        Command command = commandParser.parseCommands(transformationText).get(0);
        boolean matches = transformator.isElementMatches(chain, command);
        return matches;
    }

    // TODO Test all possible commands and exception if command not found
}
