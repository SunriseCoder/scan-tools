package process.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import process.parser.dto.Chain;
import process.parser.dto.html.HtmlElement;

public class HtmlParserTest {
    private HtmlParser parser;

    @Before
    public void beforeTest() {
        parser = new HtmlParser();
    }

    @Test
    public void testParseEmptyTag() throws ParseException {
        Chain<HtmlElement> elements = parser.parse("<span class=\"fontstyle13\"></span>");
        assertNull(elements.getNextChain());

        HtmlElement element = elements.getValue();
        assertEquals("", element.getContent());
        assertTrue(element.isEmpty());

        assertEquals("fontstyle13", element.getAttributeValue("class"));
        assertFalse(element.containsAttribute("style"));
    }

    @Test
    public void testParseFullTag() throws ParseException {
        Chain<HtmlElement> elements = parser.parse("<span class=\"fontstyle15\" style=\"font-size:12pt;\">Content<br>Text</span>");
        assertNull(elements.getNextChain());

        HtmlElement element = elements.getValue();
        assertEquals("Content<br>Text", element.getContent());
        assertFalse(element.isEmpty());

        assertEquals("fontstyle15", element.getAttributeValue("class"));
        assertEquals("font-size:12pt;", element.getAttributeValue("style"));
    }

    @Test
    public void testMultipleTags() throws ParseException {
        Chain<HtmlElement> elements = parser.parse("<span>Content1</span><span>Content2</span>");
        assertNotNull(elements.getNextChain());

        HtmlElement element1 = elements.getValue();
        assertEquals("Content1", element1.getContent());

        elements = elements.getNextChain();
        assertNull(elements.getNextChain());

        HtmlElement element2 = elements.getValue();
        assertEquals("Content2", element2.getContent());
    }

    @Test(expected = Exception.class)
    public void testWrongClosingTag() throws ParseException {
        parser.parse("<span>Content1</wrong>");
    }

    @Test
    public void testParseTagWithNestedTag() throws ParseException {
        Chain<HtmlElement> elements = parser.parse("<span>Content1<b>Content2</b></span>");
        assertNull(elements.getNextChain());

        HtmlElement element1 = elements.getValue();
        assertEquals("span", element1.getTagName());
        assertEquals("Content1<b>Content2</b>", element1.getContent());
    }

    @Test
    public void testParseNonClosedBrBetweenTags() throws ParseException {
        Chain<HtmlElement> elements = parser.parse("<span>Content1</span><br><span>Content2</span>");

        HtmlElement element1 = elements.getValue();
        assertEquals("span", element1.getTagName());
        assertEquals("Content1", element1.getContent());

        elements = elements.getNextChain();
        assertNotNull(elements);
        HtmlElement element2 = elements.getValue();
        assertEquals("br", element2.getTagName());
        assertNull(element2.getContent());

        elements = elements.getNextChain();
        assertNotNull(elements);
        HtmlElement element3 = elements.getValue();
        assertEquals("span", element3.getTagName());
        assertEquals("Content2", element3.getContent());

        assertNull(elements.getNextChain());
    }
}
