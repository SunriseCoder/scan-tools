package process.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import process.parser.dto.html.HtmlElement;
import process.parser.dto.html.TagElement;
import process.parser.dto.html.TextElement;

public class HtmlParserTest {
    private HtmlParser parser;
    private HtmlRenderer renderer;

    @Before
    public void beforeTest() {
        parser = new HtmlParser();
        renderer = new HtmlRenderer();
    }

    @Test
    public void testParseNull() throws ParseException {
        List<HtmlElement> content = parser.parse(null);

        assertNotNull(content);
        assertEquals(0, content.size());
    }

    @Test
    public void testParseEmptyText() throws ParseException {
        List<HtmlElement> content = parser.parse("");

        assertNotNull(content);
        assertEquals(0, content.size());
    }

    @Test(expected = ParseException.class)
    public void testEmptyTagError() throws ParseException {
        parser.parse("<>");
    }

    @Test
    public void testParseJustText() throws ParseException {
        List<HtmlElement> content = parser.parse("Text");

        assertNotNull(content);
        assertEquals(1, content.size());

        HtmlElement element1 = content.get(0);
        assertTrue(element1 instanceof TextElement);

        TextElement text1 = (TextElement) element1;
        assertEquals("Text", text1.getValue());
        assertNull(text1.getParentContent());
        assertNull(text1.getParentTag());
    }

    @Test
    public void testEmptyTag() throws ParseException {
        List<HtmlElement> content = parser.parse("<span />");

        assertEquals(1, content.size());

        HtmlElement element1 = content.get(0);
        assertTrue(element1 instanceof TagElement);

        TagElement tag1 = (TagElement) element1;
        assertEquals("span", tag1.getName());
        assertEquals(0, tag1.getAttributes().size());
        assertEquals(0, tag1.getContent().size());
    }

    @Test
    public void testTagWithSpaces() throws ParseException {
        List<HtmlElement> content = parser.parse(" < span > < / span > ");

        assertEquals(3, content.size());

        assertTrue(content.get(0) instanceof TextElement);
        assertEquals(" ", ((TextElement) content.get(0)).getValue());

        assertTrue(content.get(1) instanceof TagElement);
        TagElement tag = (TagElement) content.get(1);
        assertEquals("span", tag.getName());
        assertEquals(0, tag.getAttributes().size());
        assertEquals(1, tag.getContent().size());
        assertTrue(tag.getContent().get(0) instanceof TextElement);
        assertEquals(" ", ((TextElement) tag.getContent().get(0)).getValue());

        assertTrue(content.get(2) instanceof TextElement);
        assertEquals(" ", ((TextElement) content.get(2)).getValue());
    }

    @Test
    public void testParseTagWithAttributes() throws ParseException {
        List<HtmlElement> content = parser.parse("<tag attr=\"value\" />");

        assertEquals(1, content.size());

        HtmlElement element1 = content.get(0);
        assertTrue(element1 instanceof TagElement);

        TagElement tag1 = (TagElement) element1;
        assertEquals("tag", tag1.getName());
        assertEquals(0, tag1.getContent().size());
        assertEquals(1, tag1.getAttributes().size());
        assertEquals("value", tag1.getAttributeValue("attr"));
    }

    @Test
    public void testParseComplexStructures() throws ParseException {
        complexTest(" <span at=\"atv\" v2> <div> <br /> </div> </span> ");
        complexTest("<span><br></span>", "<span><br /></span>");
        complexTest("<span><br><br><br></span>", "<span><br /><br /><br /></span>");
        complexTest(" < span at = atv v2 > < div/>< / span >", " <span at=\"atv\" v2> <div /></span>");
        complexTest("<a><b><c>", "<a /><b /><c />");
        complexTest("<a a=1><b b=2>", "<a a=\"1\" /><b b=\"2\" />");
    }

    @Test
    public void testParseFormattedText() throws ParseException {
        complexTest("<a>\n\t<b />\n</a>", "<a><b /></a>");
        complexTest("<a>\n\tHello1\n\t<b />\n\tHello2\n</a>", "<a>\n\tHello1\n\t<b />\n\tHello2\n</a>\n", true);
    }

    private void complexTest(String sourceText) throws ParseException {
        complexTest(sourceText, sourceText, false);
    }

    private void complexTest(String sourceText, String expectedText) throws ParseException {
        complexTest(sourceText, expectedText, false);
    }

    private void complexTest(String sourceText, String expectedText, boolean format) throws ParseException {
        List<HtmlElement> parsedContent = parser.parse(sourceText);
        String renderedText = renderer.render(parsedContent, format);
        assertEquals(expectedText, renderedText);
    }

    @Test(expected = Exception.class)
    public void testWrongClosingTag() throws ParseException {
        parser.parse("<span>Content1</wrong>");
    }
}
