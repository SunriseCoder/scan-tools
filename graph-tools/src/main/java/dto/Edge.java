package dto;

public class Edge {
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
}
