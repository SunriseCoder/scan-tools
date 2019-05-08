package processing.cleaning.preset;

public class Condition {
    private Range red;
    private Range green;
    private Range blue;

    public Condition(Range red, Range green, Range blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Range getRed() {
        return red;
    }

    public void setRed(Range red) {
        this.red = red;
    }

    public Range getGreen() {
        return green;
    }

    public void setGreen(Range green) {
        this.green = green;
    }

    public Range getBlue() {
        return blue;
    }

    public void setBlue(Range blue) {
        this.blue = blue;
    }
}
