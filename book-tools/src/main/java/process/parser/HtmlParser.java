package process.parser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import process.parser.dto.Chain;
import process.parser.dto.html.HtmlElement;
import process.parser.dto.html.TagElement;
import process.parser.dto.html.TextElement;

public class HtmlParser {
    private static final String REGEX_ALPHA_NUMERIC = "[A-Za-z0-9]";

    public List<HtmlElement> parse(String htmlText) throws ParseException {
        List<HtmlElement> rootContent = new ArrayList<>();

        if (htmlText == null) {
            return rootContent;
        }

        StringBuilder buffer = new StringBuilder();
        TagElement currentTag = null;
        String attributeName = null;
        List<HtmlElement> currentContent = rootContent;
        ParsingModes parsingMode = ParsingModes.Start;
        for (int i = 0; i < htmlText.length(); i++) {
            String symbol = htmlText.substring(i, i + 1);

            switch (parsingMode) {

            case Start:
                if ("<".equals(symbol)) {
                    parsingMode = ParsingModes.ParsingOpeningTag;
                } else {
                    parsingMode = ParsingModes.ParsingText;
                    if (!"\t".equals(symbol) && !"\n".equals(symbol)) {
                        buffer.append(symbol);
                    }
                }
                break;

            case ParsingText:
                if ("<".equals(symbol)) {
                    if (!buffer.toString().replaceAll("\n", "").replaceAll("\t", "").isEmpty()) {
                        createTextElement(buffer, currentContent);
                    }

                    parsingMode = ParsingModes.ParsingOpeningTag;
                    buffer = new StringBuilder();
                } else {
                    if (!"\t".equals(symbol) && !"\n".equals(symbol)) {
                        buffer.append(symbol);
                    }
                }
                break;

            case ParsingOpeningTag:
                if ("/".equals(symbol)) {
                    if (buffer.length() > 0) {
                        // Creating New TagElememnt
                        currentTag = createTagElement(currentTag, buffer.toString());
                        currentTag.setParentContent(currentContent);
                        currentContent.add(currentTag);

                        // Replacing current Content with new child Tag Content
                        currentContent = currentTag.getContent();

                        parsingMode = ParsingModes.ParsingEmptyTag;
                        buffer = new StringBuilder();
                    } else {
                        parsingMode = ParsingModes.ParsingClosingTag;
                    }
                } else if (">".equals(symbol)) {
                    if (buffer.length() == 0) {
                        throw new ParseException("Empty opening Tag at: " + i, i);
                    }

                    // Creating New TagElememnt
                    currentTag = createTagElement(currentTag, buffer.toString());
                    currentTag.setParentContent(currentContent);
                    currentContent.add(currentTag);

                    // Replacing current Content with new child Tag Content
                    currentContent = currentTag.getContent();

                    parsingMode = ParsingModes.Start;
                    buffer = new StringBuilder();
                } else if (symbol.matches(REGEX_ALPHA_NUMERIC)) {
                    buffer.append(symbol);
                } else if (" ".equals(symbol)) {
                    if (buffer.length() > 0) {
                        // Creating New TagElememnt
                        currentTag = createTagElement(currentTag, buffer.toString());
                        currentTag.setParentContent(currentContent);
                        currentContent.add(currentTag);

                        // Replacing current Content with new child Tag Content
                        currentContent = currentTag.getContent();

                        parsingMode = ParsingModes.ParsingAttributeName;
                        buffer = new StringBuilder();
                    }
                } else {
                    throw new ParseException("Illegal Symbol: " + symbol + " at " + i, i);
                }
                break;

            case ParsingAttributeName:
                if (" ".equals(symbol)) {
                    if (buffer.length() > 0) {
                        parsingMode = ParsingModes.ParsingForAssignOrNewAttribute;
                    }
                } else if ("=".equals(symbol)) {
                    attributeName = buffer.toString();
                    parsingMode = ParsingModes.ParsingAttributeValue;
                    buffer = new StringBuilder();
                } else if ("/".equals(symbol)) {
                    if (buffer.length() > 0) {
                        currentTag.setAttribute(buffer.toString(), buffer.toString());
                    }
                    parsingMode = ParsingModes.ParsingEmptyTag;
                    buffer = new StringBuilder();
                } else if (">".equals(symbol)) {
                    if (buffer.length() > 0) {
                        currentTag.setAttribute(buffer.toString(), buffer.toString());
                    }
                    parsingMode = ParsingModes.Start;
                    buffer = new StringBuilder();
                } else if (symbol.matches(REGEX_ALPHA_NUMERIC)) {
                    buffer.append(symbol);
                } else {
                    throw new ParseException("Illegal symbol: " + symbol, i);
                }
                break;

            case ParsingForAssignOrNewAttribute:
                if ("=".equals(symbol)) {
                    attributeName = buffer.toString();
                    parsingMode = ParsingModes.ParsingAttributeValue;
                    buffer = new StringBuilder();
                } else if (">".equals(symbol)) {
                    attributeName = buffer.toString();
                    currentTag.setAttribute(attributeName, attributeName);

                    parsingMode = ParsingModes.Start;
                    buffer = new StringBuilder();
                } else if (symbol.matches(REGEX_ALPHA_NUMERIC)) {
                    attributeName = buffer.toString();
                    currentTag.setAttribute(attributeName, attributeName);
                    parsingMode = ParsingModes.ParsingAttributeName;
                    buffer = new StringBuilder();
                    buffer.append(symbol);
                } else {
                    throw new ParseException("Illegal symbol: " + symbol + " at " + i, i);
                }
                break;

            case ParsingAttributeValue:
                if ("\"".equals(symbol)) {
                    parsingMode = ParsingModes.ParsingAttributeValueQuoteOpen;
                } else if (" ".equals(symbol)) {
                    if (buffer.length() > 0) {
                        String attributeValue = buffer.toString();
                        currentTag.setAttribute(attributeName, attributeValue);
                        parsingMode = ParsingModes.ParsingAttributeName;
                        buffer = new StringBuilder();
                    }
                } else if ("\\".equals(symbol)) {
                    parsingMode = ParsingModes.ParsingAttributeValueEscaped;
                } else if (">".equals(symbol)) {
                    if (buffer.length() > 0) {
                        String attributeValue = buffer.toString();
                        currentTag.setAttribute(attributeName, attributeValue);
                        parsingMode = ParsingModes.Start;
                        buffer = new StringBuilder();
                    } else {
                        throw new ParseException("Illegal symbol " + symbol + " at " + i, i);
                    }
                } else {
                    buffer.append(symbol);
                }
                break;

            case ParsingAttributeValueQuoteOpen:
                if ("\\".equals(symbol)) {
                    parsingMode = ParsingModes.ParsingAttributeValueQuoteOpenEscaped;
                } else if ("\"".equals(symbol)) {
                    currentTag.setAttribute(attributeName, buffer.toString());
                    parsingMode = ParsingModes.ParsingAttributeName;
                    buffer = new StringBuilder();
                } else {
                    buffer.append(symbol);
                }
                break;

            case ParsingAttributeValueQuoteOpenEscaped:
                buffer.append(symbol);
                parsingMode = ParsingModes.ParsingAttributeValueQuoteOpen;
                break;

            case ParsingAttributeValueEscaped:
                buffer.append(symbol);
                parsingMode = ParsingModes.ParsingAttributeValue;
                break;

            case ParsingEmptyTag:
                if (">".equals(symbol)) {
                    currentContent = currentTag.getParentContent();
                    currentTag = currentTag.getParentTag();

                    parsingMode = ParsingModes.Start;
                    buffer = new StringBuilder();
                }
                break;

            case ParsingClosingTag:
                if (">".equals(symbol)) {
                    if (currentTag == null) {
                        throw new ParseException("Closing non-existing Tag: " + buffer, i);
                    }

                    while (currentTag != null && !buffer.toString().trim().equals(currentTag.getName())) {
                        currentContent = currentTag.getParentContent();

                        List<HtmlElement> childContent = currentTag.getContent();
                        currentContent.addAll(childContent);
                        currentTag.clearContent();

                        currentTag = currentTag.getParentTag();
                    }

                    currentContent = currentTag.getParentContent();
                    currentTag = currentTag.getParentTag();

                    parsingMode = ParsingModes.Start;
                    buffer = new StringBuilder();
                } else if (" ".equals(symbol)) {
                    // Just skip space symbol inside close tag definition
                } else {
                    buffer.append(symbol);
                }
                break;

            default:
                throw new IllegalStateException("Illegal Parsing State: " + parsingMode);
            }
        }

        if (parsingMode == ParsingModes.ParsingText && buffer.length() > 0) {
            createTextElement(buffer, rootContent);
        }

        // Tags, which were never closed
        while (currentTag != null) {
            currentContent = currentTag.getParentContent();

            List<HtmlElement> childContent = currentTag.getContent();
            currentContent.addAll(childContent);
            currentTag.clearContent();

            currentTag = currentTag.getParentTag();
        }

        return rootContent;
    }

    private void createTextElement(StringBuilder buffer, List<HtmlElement> content) {
        TextElement textElement = new TextElement();
        textElement.setValue(buffer.toString());
        content.add(textElement);
    }

    private TagElement createTagElement(TagElement parentTag, String tagName) {
        TagElement tagElement = new TagElement();
        tagElement.setParentTag(parentTag);
        tagElement.setName(tagName);

        return tagElement;
    }

    private enum ParsingModes {
        Start, ParsingText, ParsingOpeningTag, ParsingOpeningTagName, ParsingEmptyTag, ParsingClosingTag,
        ParsingAttributeName, ParsingForAssignOrNewAttribute, ParsingAttributeValue, ParsingAttributeValueQuoteOpen,
        ParsingAttributeValueQuoteOpenEscaped, ParsingAttributeValueEscaped
    }

    public Chain<HtmlElement> toChain(List<HtmlElement> elements) {
        Chain<HtmlElement> firstChain = null;
        Chain<HtmlElement> previousChain = null;

        for (int i = 0; i < elements.size(); i++) {
            HtmlElement element = elements.get(i);
            Chain<HtmlElement> currentChain = new Chain<>(element);
            currentChain.linkPrevious(previousChain);

            if (firstChain == null) {
                firstChain = currentChain;
            }

            previousChain = currentChain;
        }

        return firstChain;
    }
}
