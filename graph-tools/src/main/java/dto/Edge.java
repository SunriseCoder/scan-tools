package dto;

public class Edge extends GraphElement {
    private EdgeEndpoint edgeStart;
    private EdgeEndpoint edgeEnd;

    public Edge() {
        // Default Constructor
    }

    public Edge(EdgeEndpoint edgeStart, EdgeEndpoint edgeEnd) {
        this.edgeStart = edgeStart;
        this.edgeEnd = edgeEnd;
        updateVertices();
    }

    public EdgeEndpoint getEdgeStart() {
        return edgeStart;
    }

    public EdgeEndpoint getEdgeEnd() {
        return edgeEnd;
    }

    public void setEdgeStart(EdgeEndpoint edgeStart) {
        this.edgeStart = edgeStart;
        updateVertices();
    }

    public void setEdgeEnd(EdgeEndpoint edgeEnd) {
        this.edgeEnd = edgeEnd;
        updateVertices();
    }

    private void updateVertices() {
        edgeStart.getVertex().addOutgoingEdge(this);
        edgeEnd.getVertex().addIncomingEdge(this);
    }

    public static Point getAbsolutePosition(EdgeEndpoint endpoint) {
        Vertex vertex = endpoint.getVertex();
        Point vertexPosition = vertex.getPosition();
        Point vertexSize = vertex.getSize();

        Point point = endpoint.getPoint();

        double x = vertexPosition.x + point.x * vertexSize.x;
        double y = vertexPosition.y + point.y * vertexSize.y;

        Point absolutePosition = new Point(x, y);
        return absolutePosition;
    }
}
