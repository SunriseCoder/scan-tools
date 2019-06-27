package dto;

import java.util.HashSet;
import java.util.Set;

public class Vertex extends GraphElement {
    private String text;
    private String details;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
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

    public void setSize(double width, double height) {
        size.x = width;
        size.y = height;
    }

    public void addIncomingEdge(Edge edge) {
        incomingEdges.add(edge);
    }

    public void addOutgoingEdge(Edge edge) {
        outgoingEdges.add(edge);
    }
}
