package dto;

public class IntPoint {
    public int x;
    public int y;

    public IntPoint() {
        // Default constructor for deserialization
    }

    public IntPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + x + ", " + y + "]";
    }
}
