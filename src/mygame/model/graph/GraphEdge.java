package mygame.model.graph;

public class GraphEdge {
    public final int CHILD_ID;
    public final double WEIGHT;

    public GraphEdge(int childId, double weight) {
        this.CHILD_ID = childId;
        this.WEIGHT = weight;
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof GraphEdge)) return false;
        return ((GraphEdge)o).CHILD_ID == CHILD_ID && ((GraphEdge)o).WEIGHT == WEIGHT;
    }
    
    @Override
    public String toString() {
        return "Graph Edge: " + Integer.toString(CHILD_ID) + ", " + Double.toString(WEIGHT);
    }
}