package wrappers;

public class LongWrapper {
    private long value;

    public LongWrapper() {
        // Default constructor
    }

    public LongWrapper(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long postIncrement() {
        return value++;
    }
}
