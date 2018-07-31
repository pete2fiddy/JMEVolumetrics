package mygame.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SparseGraph implements Graph {
    protected GraphNode[] nodes;
    
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
        nodes[id1].outEdges.put(id2, weight);
    }
    
    @Override
    public List<GraphEdge> getOutEdges(int id) {
        ArrayList<GraphEdge> out = new ArrayList<GraphEdge>();
        for(int childId : nodes[id].outEdges.keySet()) {
            out.add(new GraphEdge(childId, nodes[id].outEdges.get(childId)));
        }
        return out;
    }

    @Override
    public int numNodes() {
        return nodes.length;
    }

    @Override
    public void unlink(int id1, int id2) {
        nodes[id1].outEdges.remove(id2);
    }
}
