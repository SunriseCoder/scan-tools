package dto;

public class EdgeEndpoint {
    private Point point;
    private Vertex vertex;

    public EdgeEndpoint() {
        // Default Constructor
    }

    public EdgeEndpoint(Point point, Vertex vertex) {
        this.point = point;
        this.vertex = vertex;
    }

    public Point getPoint() {
        return point;
    }

    public Vertex getVertex() {
        return vertex;
    }
}
