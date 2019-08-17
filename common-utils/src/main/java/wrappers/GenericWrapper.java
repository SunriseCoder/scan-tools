package wrappers;

public class GenericWrapper<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean isNotNull() {
        return value != null;
    }
}
