package wrappers;

public class IntWrapper {
    private int value;

    public IntWrapper() {
        // Default constructor
    }

    public IntWrapper(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int postIncrement() {
        return value++;
    }
}
