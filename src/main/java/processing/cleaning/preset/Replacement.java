package processing.cleaning.preset;

import java.awt.Color;

public class Replacement {
    private String name;
    private Condition condition;
    private Color replacement;

    public Replacement(String name) {
        this.name = name;
    }

    public Replacement(String name, Condition condition, Color replacement) {
        this.name = name;
        this.condition = condition;
        this.replacement = replacement;
    }

    public String getName() {
        return name;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Color getReplacement() {
        return replacement;
    }

    public void setReplacement(Color replacement) {
        this.replacement = replacement;
    }
}
