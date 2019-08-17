package process.parser.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import process.parser.dto.html.TagAttribute;

public class Command {
    private Map<Positions, Condition> conditions;
    private List<Action> actions;

    public Command() {
        conditions = new HashMap<>();
    }

    public Map<Positions, Condition> getConditions() {
        return conditions;
    }

    public Condition getCondition(Positions position) {
        Condition condition = conditions.get(position);
        return condition;
    }

    public void setConditions(Map<Positions, Condition> conditions) {
        this.conditions = conditions;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public static class Condition {
        private Positions position;
        private String tagName;
        private Map<String, TagAttribute> attributes;

        public Condition() {
            attributes = new HashMap<>();
        }

        public Positions getPosition() {
            return position;
        }

        public void setPosition(Positions position) {
            this.position = position;
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

        public boolean containsAttribute(String name) {
            boolean contains = attributes.containsKey(name);
            return contains;
        }

        public String getAttributeValue(String name) {
            TagAttribute attribute = attributes.get(name);
            String attributeValue = attribute == null ? null : attribute.getValue();
            return attributeValue;
        }
    }

    public enum Positions {
        Previous, Current, Next
    }
}
