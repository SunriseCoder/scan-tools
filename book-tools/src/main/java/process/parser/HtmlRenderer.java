package process.parser;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import process.parser.dto.html.HtmlElement;
import process.parser.dto.html.TagElement;
import process.parser.dto.html.TextElement;

public class HtmlRenderer {

    public String render(List<HtmlElement> content) {
        String renderedText = render(content, false);
        return renderedText;
    }

    public String render(List<HtmlElement> content, boolean format) {
        StringBuilder sb = new StringBuilder();

        int indent = 0;
        renderContentRecursively(content, sb, format, indent);

        return sb.toString();
    }

    private void renderContentRecursively(List<HtmlElement> content, StringBuilder sb, boolean format, int indent) {
        HtmlElement previousElement = null;
        for (HtmlElement element : content) {
            if (element instanceof TextElement) {
                String value = ((TextElement) element).getValue();
                // If it's first element inside the tag, but the tag has nested tags
                if (format && previousElement == null && content.stream().filter(e -> e instanceof TagElement).count() > 0) {
                    generateStrings("\t", sb, indent);
                }
                // If previous element was a tag
                if (format && previousElement != null && previousElement instanceof TagElement) {
                    generateStrings("\t", sb, indent);
                }
                sb.append(value);
            } else if (element instanceof TagElement) {
                TagElement tag = (TagElement) element;

                // If previous element was TextElement, need to start with new line and indent
                if (format && previousElement != null && previousElement instanceof TextElement) {
                    sb.append("\n");
                }

                renderTag(tag, sb, format, indent);

                renderContentRecursively(tag.getContent(), sb, format, indent + 1);

                // Closing non-empty Tag
                if (tag.getContent().size() != 0) {
                    // If tag has at least one nested tags (not text only), need \n before close
                    if (format && tag.hasNestedTags()) {
                        // If tag has one TagElement AND last element is TextElement, need new line
                        HtmlElement lastNestedElement = tag.getContent().get(tag.getContent().size() - 1);
                        if (lastNestedElement != null && lastNestedElement instanceof TextElement) {
                            sb.append("\n");
                        }

                        // Generating Indent for Closing Tag
                        generateStrings("\t", sb, indent);
                    }
                    sb.append("</").append(tag.getName()).append(">");
                    if (format) {
                        sb.append("\n");
                    }
                }
            }
            previousElement = element;
        }
    }

    private void renderTag(TagElement tag, StringBuilder sb, boolean format, int indent) {
        if (format) {
            generateStrings("\t", sb, indent);
        }

        sb.append("<").append(tag.getName());

        Map<String, String> attributes = tag.getAttributes();
        for (Entry<String, String> attribute : attributes.entrySet()) {
            if (attribute.getKey().equals(attribute.getValue())) {
                sb.append(" ").append(attribute.getKey());
            } else {
                String value = attribute.getValue().replaceAll("\"", "&quot;");
                sb.append(" ").append(attribute.getKey()).append("=\"").append(value).append("\"");
            }
        }

        if (tag.getContent().isEmpty()) {
            sb.append(" />");
            if (format) {
                sb.append("\n");
            }
        } else {
            sb.append(">");
        }

        if (format && tag.hasNestedTags()) {
            sb.append("\n");
        }
    }

    private void generateStrings(String string, StringBuilder sb, int amount) {
        for (int i = 0; i < amount; i++) {
            sb.append(string);
        }
    }
}
