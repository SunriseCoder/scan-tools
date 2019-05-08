package processing.cleaning.preset;

import java.util.List;

public class Preset {
    private String name;
    private List<Replacement> replacements;

    public Preset(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Replacement> getReplacements() {
        return replacements;
    }

    public void setReplacements(List<Replacement> operations) {
        this.replacements = operations;
    }
}
