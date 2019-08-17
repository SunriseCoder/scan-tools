package dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Edge.class)
public class Edge extends GraphElement {
    private int id;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        if (edgeStart != null && edgeStart.getVertex() != null) {
            edgeStart.getVertex().addOutgoingEdge(this);
        }

        if (edgeEnd != null && edgeEnd.getVertex() != null) {
            edgeEnd.getVertex().addIncomingEdge(this);
        }
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

    public void removeItselfFromVertices() {
        if (edgeStart != null && edgeStart.getVertex() != null) {
            edgeStart.getVertex().removeOutgoingEdge(this);
            edgeStart = null;
        }

        if (edgeEnd != null && edgeEnd.getVertex() != null) {
            edgeEnd.getVertex().removeIncomingEdge(this);
            edgeEnd = null;
        }
    }
}
