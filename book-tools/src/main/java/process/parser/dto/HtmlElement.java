package process.parser.dto;

import java.util.HashMap;
import java.util.Map;

public class HtmlElement {
    private String tagName;
    private Map<String, TagAttribute> attributes;
    private String content;

    public HtmlElement() {
        attributes = new HashMap<>();
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Map<String, TagAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, TagAttribute> attributes) {
        this.attributes = attributes;
    }

    public boolean isEmpty() {
        boolean isEmpty = content == null || content.isEmpty();
        return isEmpty;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean containsAttribute(String name) {
        boolean contains = attributes.containsKey(name);
        return contains;
    }

    public TagAttribute getAttribute(String name) {
        TagAttribute attribute = attributes.get(name);
        return attribute;
    }

    public String getAttributeValue(String name) {
        TagAttribute attribute = attributes.get(name);
        String attributeValue = attribute == null ? null : attribute.getValue();
        return attributeValue;
    }
}
