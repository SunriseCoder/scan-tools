package process.parser;

import java.text.ParseException;
import java.util.Stack;

import process.parser.dto.Chain;
import process.parser.dto.html.HtmlElement;
import process.parser.dto.html.TagAttribute;

public class HtmlParser {

    public Chain<HtmlElement> parse(String string) throws ParseException {
        Chain<HtmlElement> firstChain = null;
        Chain<HtmlElement> previousChain = null;

        Modes mode = Modes.Free;
        StringBuilder tag = new StringBuilder();
        StringBuilder content = new StringBuilder();
        HtmlElement element = null;
        Stack<String> tagHierarchy = new Stack<>();
        for (int i = 0; i < string.length(); i++) {
            String symbol = string.substring(i, i + 1);

            switch (mode) {

            case Free:
                if (symbol.equals("<")) {
                    mode = Modes.TagNameStart;
                    tag = new StringBuilder();
                }
                break;

            case TagNameStart:
                if (symbol.equals("/")) {
                    mode = Modes.CloseTagName;
                } else {
                    mode = Modes.OpeningTagName;
                    tag.append(symbol);
                }
                break;

            case OpeningTagName:
                if (symbol.equals(">")) {
                    if ("br".equals(tag.toString()) && tagHierarchy.size() == 0) {
                        element = createHtmlElement(tag);
                        previousChain = addElementToChain(element, previousChain);
                        if (firstChain == null) {
                            firstChain = previousChain;
                        }

                        mode = Modes.Free;
                        break;
                    }
                    // Saving TagName without attributes
                    tagHierarchy.push(tag.toString().split(" ")[0]);

                    if (tagHierarchy.size() > 1) {
                        content.append("<").append(tag).append(">");
                        mode = Modes.TagContent;
                        break;
                    } else {
                        element = createHtmlElement(tag);

                        previousChain = addElementToChain(element, previousChain);
                        if (firstChain == null) {
                            firstChain = previousChain;
                        }

                        mode = Modes.TagContent;
                        content = new StringBuilder();
                    }
                } else {
                    tag.append(symbol);
                }
                break;

            case CloseTagName:
                if (symbol.equals(">")) {
                    boolean found = false;

                    while (tagHierarchy.size() > 0) {
                        String storedTag = tagHierarchy.pop();
                        if (storedTag.equals(tag.toString())) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        String message = "Main Tag \"" + element + "\" was not properly closed";
                        throw new ParseException(message, string.length());
                    }

                    if (tagHierarchy.size() == 0) {
                        element.setContent(content.toString());
                        mode = Modes.Free;
                    } else {
                        content.append("</").append(tag).append(">");
                        mode = Modes.TagContent;
                    }
                } else {
                    tag.append(symbol);
                }
                break;

            case TagContent:
                if (symbol.equals("<")) {
                    mode = Modes.TagNameStart;
                    tag = new StringBuilder();
                } else {
                    content.append(symbol);
                }
                break;

            default:
                break;
            }
        }

        return firstChain;
    }

    private HtmlElement createHtmlElement(StringBuilder tag) {
        HtmlElement element = new HtmlElement();

        String[] attributes = tag.toString().split(" ");
        String tagName = attributes[0];
        element.setTagName(tagName);

        for (int i = 1; i < attributes.length; i++) {
            String attribute = attributes[i];

            String[] attributeParts = attribute.split("=");
            String attributeName = attributeParts[0];
            String attributeValue = attributeParts[1].replaceAll("\"", "");

            TagAttribute tagAttribute = new TagAttribute();
            tagAttribute.setName(attributeName);
            tagAttribute.setValue(attributeValue);

            element.getAttributes().put(attributeName, tagAttribute);
        }

        return element;
    }

    private Chain<HtmlElement> addElementToChain(HtmlElement element, Chain<HtmlElement> currentChain) {
        Chain<HtmlElement> newChain = new Chain<>(element);
        newChain.linkPrevious(currentChain);
        return newChain;
    }

    public enum Modes {
        Free, TagNameStart, CloseTagName, OpeningTagName, TagContent
    }
}
