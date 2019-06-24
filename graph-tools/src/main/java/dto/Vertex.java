package dto;

public class Vertex {
    private Point position;
    private Point size;

    public Vertex() {
        position = new Point();
        size = new Point();
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(double x, double y) {
        position.x = x;
        position.y = y;
    }

    public Point getSize() {
        return size;
    }

    public void setSize(double x, double y) {
        size.x = x;
        size.y = y;
    }
}
