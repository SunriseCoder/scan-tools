package dto;

import java.util.HashSet;
import java.util.Set;

public class Vertex {
    private Point position;
    private Point size;

    private Set<Edge> incomingEdges;
    private Set<Edge> outgoingEdges;

    public Vertex() {
        position = new Point();
        size = new Point();
        incomingEdges = new HashSet<>();
        outgoingEdges = new HashSet<>();
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

    public void addIncomingEdge(Edge edge) {
        incomingEdges.add(edge);
    }

    public void addOutgoingEdge(Edge edge) {
        outgoingEdges.add(edge);
    }
}
