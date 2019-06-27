package dto;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private int vertexIdCounter = 0;
    private int edgeIdCounter = 0;

    private List<Vertex> vertices;
    private List<Edge> edges;

    public Graph() {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void addVertex(Vertex vertex) {
        vertex.setId(vertexIdCounter++);
        vertices.add(vertex);
    }

    public void removeVertex(Vertex vertex) {
        vertices.remove(vertex);
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void addEdge(Edge edge) {
        edge.setId(edgeIdCounter++);
        edges.add(edge);
    }

    public void removeEdge(Edge edge) {
        edge.removeItselfFromVertices();
        edges.remove(edge);
    }
}
