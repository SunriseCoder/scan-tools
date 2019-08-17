package wrappers;

public class IntArrayWrapper {
    private int[] array;

    public IntArrayWrapper(int length) {
        array = new int[length];
    }

    public int[] getArray() {
        return array;
    }

    public double getLength() {
        return array.length;
    }
}
