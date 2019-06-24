package dto;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private List<Vertex> vertices;

    public Graph() {
        vertices = new ArrayList<>();
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    public void removeVertex(Vertex vertex) {
        vertices.remove(vertex);
    }

    public Graph cloneSelf() {
        Graph clone = new Graph();
        clone.vertices = new ArrayList<>(vertices);
        return clone;
    }
}
