package dto;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private List<Vertex> vertices;

    public Graph() {
        vertices = new ArrayList<>();
    }

    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public Graph cloneSelf() {
        Graph clone = new Graph();
        clone.vertices = new ArrayList<>(vertices);
        return clone;
    }
}
