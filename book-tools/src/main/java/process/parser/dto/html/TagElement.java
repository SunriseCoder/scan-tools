package process.parser.dto.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import wrappers.GenericWrapper;

public class TagElement extends AbstractHtmlElement {
    private String name;
    private Map<String, String> attributes;
    private List<HtmlElement> content;

    public TagElement() {
        attributes = new TreeMap<>();
        content = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttributeValue(String name) {
        String attributeValue = attributes.get(name);
        return attributeValue;
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    public boolean isEmpty() {
        boolean isEmpty = content == null || content.isEmpty();
        return isEmpty;
    }

    public List<HtmlElement> getContent() {
        return content;
    }

    public void setContent(List<HtmlElement> content) {
        this.content = content;
    }

    public boolean containsAttribute(String name) {
        boolean contains = attributes.containsKey(name);
        return contains;
    }

    public void addTextContent(String value) {
        TextElement textElement = new TextElement();
        textElement.setValue(value);
        content.add(textElement);
    }

    public void clearContent() {
        content = new ArrayList<>();
    }

    public boolean hasNestedTags() {
        GenericWrapper<Boolean> hasNestedTags = new GenericWrapper<>();
        hasNestedTags.setValue(false);

        content.forEach(element -> {
            if (element instanceof TagElement) {
                hasNestedTags.setValue(true);
            }
        });

        return hasNestedTags.getValue().booleanValue();
    }

    public String getContentValue(String tagName) {
        for (HtmlElement element : content) {
            // Looking for the TagElement with given Name
            if (element != null && element instanceof TagElement && ((TagElement) element).getName().equals(tagName)) {
                TagElement tag = (TagElement) element;
                List<HtmlElement> tagContent = tag.getContent();
                // Checking that the Content is the only TextElement
                if (tagContent.size() == 1 && tagContent.get(0) instanceof TextElement) {
                    String value = ((TextElement) tagContent.get(0)).getValue();
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "<" + name + " />";
    }
}
