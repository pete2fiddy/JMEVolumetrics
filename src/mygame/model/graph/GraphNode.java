package mygame.model.graph;

import java.util.ArrayList;
import java.util.HashMap;

public class GraphNode {
    
    protected final int ID;
    protected HashMap<Integer, Double> outEdges = new HashMap<Integer, Double>();
    
    public GraphNode(int id) {
        this.ID = id;
    }
}
