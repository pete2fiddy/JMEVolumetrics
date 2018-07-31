package mygame.graph;

import java.util.List;

/*
Does not extend and override the methods of any graph type so that any graph can be made symmetric (full, sparse, etc.).

Assumes the graph passed to the constructor is already symmetric, or is empty, and all graph construction will be done with the symmetric graph's methods
*/
public class SymmetricGraph implements Graph {
    private Graph graph;
    
    public SymmetricGraph(Graph g) {
        this.graph = g;
    }
    

    @Override
    public void link(int id1, int id2, double weight) {
        graph.link(id1, id2, weight);
        graph.link(id2, id1, weight);
    }

    @Override
    public List<GraphEdge> getOutEdges(int id) {
        return graph.getOutEdges(id);
    }

    @Override
    public int numNodes() {
        return graph.numNodes();
    }

    @Override
    public void unlink(int id1, int id2) {
        graph.unlink(id1, id2);
        graph.unlink(id2, id1);
    }
    
}
