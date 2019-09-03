package process.parser;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import process.parser.dto.html.HtmlElement;
import process.parser.dto.html.TagElement;
import process.parser.dto.html.TextElement;

public class HtmlRendererTest {
    private HtmlParser parser;
    private HtmlRenderer renderer;

    @Before
    public void beforeTest() {
        parser = new HtmlParser();
        renderer = new HtmlRenderer();
    }

    @Test
    public void testRenderEmptyContent() {
        String renderedText = renderer.render(new ArrayList<>());
        assertEquals("", renderedText);
    }

    @Test
    public void testRenderEmptyTag() {
        ArrayList<HtmlElement> content = new ArrayList<>();

        TagElement tag = new TagElement();
        tag.setName("span");
        content.add(tag);

        String renderedText = renderer.render(content);
        assertEquals("<span />", renderedText);
    }

    @Test
    public void testRenderNonEmptyTag() {
        ArrayList<HtmlElement> content = new ArrayList<>();

        TagElement tag = new TagElement();
        tag.setName("span");
        tag.addTextContent(" ");

        content.add(tag);

        String renderedText = renderer.render(content);
        assertEquals("<span> </span>", renderedText);
    }

    @Test
    public void testRenderEmptyTagWithAttributes() {
        ArrayList<HtmlElement> content = new ArrayList<>();

        TagElement tag = new TagElement();
        tag.setName("span");

        tag.setAttribute("attr", "value");

        content.add(tag);

        String renderedText = renderer.render(content);
        assertEquals("<span attr=\"value\" />", renderedText);
    }

    @Test
    public void testRenderNestedTags() {
        ArrayList<HtmlElement> content = new ArrayList<>();

        TagElement tag = new TagElement();
        tag.setName("span");
        tag.setAttribute("attr", "value");

        TagElement tag2 = new TagElement();
        tag2.setName("div");
        tag2.setAttribute("attr2", "value2");
        tag2.addTextContent("Hello!");
        tag.getContent().add(tag2);

        content.add(tag);

        String renderedText = renderer.render(content);
        assertEquals("<span attr=\"value\"><div attr2=\"value2\">Hello!</div></span>", renderedText);
    }

    @Test
    public void testRenderNestedTagsWithFormatting() {
        ArrayList<HtmlElement> content = new ArrayList<>();

        TagElement tag = new TagElement();
        tag.setName("span");
        tag.setAttribute("attr", "value");

        TextElement freeContent = new TextElement();
        freeContent.setValue("I'm free!");
        tag.getContent().add(freeContent);

        TagElement tag2 = new TagElement();
        tag2.setName("div");
        tag2.setAttribute("attr2", "value2");
        tag2.addTextContent("Hello!");
        tag2.addTextContent("Hello2!");
        tag.getContent().add(tag2);

        tag.getContent().add(freeContent);

        content.add(tag);

        String renderedText = renderer.render(content, true);
        String expected = "<span attr=\"value\">\n\tI'm free!\n\t<div attr2=\"value2\">Hello!Hello2!</div>\n\tI'm free!\n</span>\n";
        assertEquals(expected, renderedText);
    }

    @Test
    public void testRenderComplexStructureWithFormatting() throws ParseException {
        String sourceHtmlText = "<a><b><c>1</c><d></b></a>";
        String expectedHtmlText = "<a>\n\t<b>\n\t\t<c>1</c>\n\t\t<d />\n\t</b>\n</a>\n";

        List<HtmlElement> parsedHtml = parser.parse(sourceHtmlText);
        String renderedHtml = renderer.render(parsedHtml, true);

        assertEquals(expectedHtmlText, renderedHtml);
    }
}
