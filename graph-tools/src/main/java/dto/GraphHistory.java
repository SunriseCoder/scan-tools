package dto;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

import utils.CloneUtils;

public class GraphHistory {
    private List<Graph> graphs;
    private int position;

    public GraphHistory() {
        graphs = new ArrayList<>();
    }

    @Transient
    public Graph getCurrentGraph() {
        Graph graph = null;
        if (position < graphs.size()) {
            graph = graphs.get(position);
        }

        if (graph == null) {
            graph = new Graph();
            graphs.add(graph);
        }

        return graph;
    }

    public Graph createNewVersion() {
        Graph currentGraph = getCurrentGraph();

        // Trim end of the history list
        while (graphs.size() > position + 1) {
            graphs.remove(graphs.size() - 1);
        }

        // Cloning Graph
        Graph newGraph = (Graph) CloneUtils.clone(currentGraph);
        graphs.add(newGraph);
        position++;

        return newGraph;
    }
}
