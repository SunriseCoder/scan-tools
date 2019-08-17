package dto;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Vertex.class)
public class Vertex extends GraphElement {
    private int id;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public void removeIncomingEdge(Edge edge) {
        incomingEdges.remove(edge);
    }

    public void removeOutgoingEdge(Edge edge) {
        outgoingEdges.remove(edge);
    }
}
