package mygame.graph;

import java.util.ArrayList;
import java.util.List;


public interface Graph {
    
    public void link(int id1, int id2, double weight);
    public List<GraphEdge> getOutEdges(int id);
}
