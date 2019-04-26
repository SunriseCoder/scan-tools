package process.dto;

public class Point {
    public double x;
    public double y;

    public Point() {
        // Default constructor for deserialization
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + x + ", " + y + "]";
    }
}
