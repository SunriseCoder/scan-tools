package process.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import process.parser.dto.Chain;
import process.parser.dto.Command;
import process.parser.dto.Variable;
import process.parser.dto.html.HtmlElement;

public class TransformatorTest {
    private HtmlParser htmlParser;
    private CommandParser commandParser;
    private Transformator transformator;
    private HtmlRenderer renderer;

    @Before
    public void beforeTest() {
        htmlParser = new HtmlParser();
        commandParser = new CommandParser();
        transformator = new Transformator();
        renderer = new HtmlRenderer();
    }

    @Test
    public void testIsElementMatches() throws Exception {
        assertTrue(isElementMatches("<span>", "curr{span}->stub"));
        assertFalse(isElementMatches("<span class=\"c1\">", "curr{span}->stub"));
        assertTrue(isElementMatches("<span class=\"c1\">", "curr{span class=\"c1\"}->stub"));
        assertFalse(isElementMatches("<span>", "curr{span class=\"c1\"}->stub"));
        assertFalse(isElementMatches("<span class=\"c1\" style=\"s1\">", "curr{span class=\"c1\"}->stub"));
        assertTrue(isElementMatches("<span class=\"c1\" style=\"s1\">", "curr{span class=\"c1\" style=\"s1\"}->stub"));

        // Different Attributes Order
        assertTrue(isElementMatches("<span class=\"c1\" style=\"s1\">", "curr{span style=\"s1\" class=\"c1\"}->stub"));
    }

    private boolean isElementMatches(String sourceText, String transformationText) throws Exception {
        List<HtmlElement> parsedContent = htmlParser.parse(sourceText);
        Chain<HtmlElement> parsedChain = htmlParser.toChain(parsedContent);
        Command command = commandParser.parseCommands(transformationText).get(0);
        boolean matches = transformator.isElementMatches(parsedChain, command);
        return matches;
    }

    @Test
    public void testActionAdd() throws Exception {
        String result = testAction("<a><b><c>",
                "curr{a}->add\n" +
                "curr{c}->add");
        assertEquals("<a /><b /><c />", result);
    }

    @Test
    public void testActionAddAsParent() throws Exception {
        String result = testAction("<a><b><c>",
                "curr{a}->add\n" +
                "curr{b}->addAsParent\n" +
                "curr{c}->add");
        assertEquals("<a /><b><c /></b>", result);
    }

    @Test
    public void testActionDemoteParent() throws Exception {
        String result = testAction("<a><b><c><d><e>",
                "curr{a}->add\n" +
                "curr{b}->addAsParent\n" +
                "curr{c}->add\n" +
                "curr{d}->demoteParent;add\n" +
                "curr{e}->add");
        assertEquals("<a /><b><c /></b><d /><e />", result);
    }

    @Test
    public void testActionCreateTagBefore() throws Exception {
        String result = testAction("<a><b><c>",
                "curr{a}->add\n" +
                "curr{b}->createTag(br);add\n" +
                "curr{c}->add\n");
        assertEquals("<a /><br /><b /><c />", result);
    }

    @Test
    public void testActionCreateParentTag() throws Exception {
        String result = testAction("<a><b><c>",
                "curr{a}->add\n" +
                "curr{b}->createParentTag(div);add\n" +
                "curr{c}->demoteParent;add\n");
        assertEquals("<a /><div><b /></div><c />", result);
    }

    @Test
    public void testActionCloseParentTagIfOpen() throws Exception {
        String result = testAction("<a><b><c>",
                "curr{a}->createParentTag(ab)\n" +
                "curr{b}->add\n" +
                "curr{c}->closeParentIfOpen(ab);add\n");
        assertEquals("<ab><a /><b /></ab><c />", result);

        result = testAction("<ab><b><c>",
                "curr{a}->add\n" +
                "curr{b}->add\n" +
                "curr{c}->closeParentIfOpen(ab);add\n");
        assertEquals("<ab /><b /><c />", result);
    }

    @Test
    public void testActionChangeTagTo() throws Exception {
        String result = testAction("<a><b><c>",
                "curr{a}->add\n" +
                "curr{b}->changeTagTo(div);add\n" +
                "curr{c}->add\n");
        assertEquals("<a /><div /><c />", result);
    }

    @Test
    public void testActionClearAttributes() throws Exception {
        String result = testAction("<a a=1><b b=2><c c=3>",
                "curr{b b=2}->clearAttributes\n");
        assertEquals("<a a=\"1\" /><b /><c c=\"3\" />", result);
    }

    @Test
    public void testActionConcatToPrevious() throws Exception {
        String result = testAction("<a>1</a><b>2</b><c>3</c><d>3<e></d>",
                "curr{a}->add\n" +
                "curr{b}->concatToPrevious\n" +
                "curr{c}->add\n" +
                "curr{d}->concatToPrevious\n");
        assertEquals("<a>12</a><c>33<e /></c>", result);
    }

    @Test
    public void testActionDelete() throws Exception {
        String result = testAction("<a><b><c>",
                "curr{b}->delete\n");
        assertEquals("<a /><c />", result);
    }

    @Test
    public void testActionDeleteNestedTag() throws Exception {
        String result = testAction("<a>1<b>2</a>",
                "curr{a}->deleteNestedTag(b);add\n");
        assertEquals("<a>12</a>", result);
    }

    @Test
    public void testActionInsertAtStart() throws Exception {
        String result = testAction("<a>123</a>",
                "curr{a}->insertAtStart(0);add\n");
        assertEquals("<a>0123</a>", result);
    }

    @Test
    public void testActionInsertAtEnd() throws Exception {
        String result = testAction("<a>123</a>",
                "curr{a}->insertAtEnd(4);add\n");
        assertEquals("<a>1234</a>", result);
    }

    @Test
    public void testActionReplaceNestedTagWithTag() throws Exception {
        String result = testAction("<a><b></a>",
                "curr{a}->replaceNestedTagWithTag(b,c);add\n");
        assertEquals("<a><c /></a>", result);
    }

    @Test
    public void testActionReplaceNestedTagWithString() throws Exception {
        String result = testAction("<a><b></a>",
                "curr{a}->replaceNestedTagWithString(b,hello);add\n");
        assertEquals("<a>hello</a>", result);
    }

    @Test
    public void testActionReplaceSubstrings() throws Exception {
        String result = testAction("<a>Hehi</a>",
                "define table1 = StringArray[\"e\",\"o\", \"i\",\"a\"]\n" +
                "curr{a}->replaceSubstrings(table1);add\n");
        assertEquals("<a>Hoha</a>", result);
    }

    @Test
    public void testActionToLowerCase() throws Exception {
        String result = testAction("<a>MessAge</a>",
                "curr{a}->toLowerCase;add\n");
        assertEquals("<a>message</a>", result);
    }

    @Test
    public void testActionToUpperCase() throws Exception {
        String result = testAction("<a>MessAge</a>",
                "curr{a}->toUpperCase;add\n");
        assertEquals("<a>MESSAGE</a>", result);
    }

    @Test
    public void testActionToCamelCase() throws Exception {
        String result = testAction("<a>MessAge about CAMEL</a>",
                "curr{a}->toCamelCase;add\n");
        assertEquals("<a>Message About Camel</a>", result);
    }

    @Test
    public void testActionTrim() throws Exception {
        String result = testAction("<a>  MessAge  To you  </a>",
                "curr{a}->trim;add\n");
        assertEquals("<a>MessAge  To you</a>", result);
    }

    private String testAction(String sourceText, String transformationText) throws Exception {
        List<HtmlElement> parsedContent = htmlParser.parse(sourceText);
        Chain<HtmlElement> parsedChain = htmlParser.toChain(parsedContent);

        List<Command> commands = commandParser.parseCommands(transformationText);
        Map<String, Variable> variables = commandParser.parseVariables(transformationText);

        List<HtmlElement> transformationResult = transformator.transform(parsedChain, commands, variables);
        String renderedResult = renderer.render(transformationResult);

        return renderedResult;
    }
}
