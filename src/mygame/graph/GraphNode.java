package mygame.graph;

import java.util.ArrayList;

public class GraphNode {
    
    protected final int ID;
    protected ArrayList<GraphEdge> outEdges = new ArrayList<GraphEdge>();
    
    public GraphNode(int id) {
        this.ID = id;
    }
}
