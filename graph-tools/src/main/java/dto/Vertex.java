package dto;

public class Vertex {
    private Point position;

    public Vertex() {
        position = new Point();
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(double x, double y) {
        position.x = x;
        position.y = y;
    }
}
