package process.parser.dto;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Command {
    private String sourceText;
    private Map<Positions, Condition> conditions;
    private List<Action> actions;

    public Command(String sourceText) {
        this.sourceText = sourceText;
        conditions = new TreeMap<>();
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
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

    @Override
    public String toString() {
        return sourceText;
    }

    public static class Condition {
        private Positions position;
        private String tagName;
        private Map<String, String> attributes;

        public Condition() {
            attributes = new TreeMap<>();
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

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        public boolean containsAttribute(String name) {
            boolean contains = attributes.containsKey(name);
            return contains;
        }

        public String getAttributeValue(String name) {
            String attributeValue = attributes.get(name);
            return attributeValue;
        }

        public void setAttributeValue(String name, String value) {
            attributes.put(name, value);
        }
    }

    public enum Positions {
        Previous, Current, Next
    }
}
