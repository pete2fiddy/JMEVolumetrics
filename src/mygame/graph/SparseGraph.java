package mygame.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SparseGraph implements Graph {
    private GraphNode[] nodes;
    
    public SparseGraph(int nNodes) {
        this.nodes = new GraphNode[nNodes];
        for(int i = 0; i < this.nodes.length; i++) {
            this.nodes[i] = new GraphNode(i);
        }
    }
    
    public SparseGraph(GraphNode... nodes) {
        this.nodes = nodes;
    }
    
    @Override
    public void link(int id1, int id2, double weight) {
        nodes[id1].outEdges.add(new GraphEdge(id2, weight));
    }
    
    @Override
    public List<GraphEdge> getOutEdges(int id) {
        return nodes[id].outEdges;
    }
}
