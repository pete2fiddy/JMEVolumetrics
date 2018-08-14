package mygame.model.graph;

/*
Used as a container for all the information a graph edge can represent. Are useful for some graph algorithms, and are constructed within their implementation.
*/
public class ParentChildGraphEdge implements Comparable<ParentChildGraphEdge>{
    public final int PARENT_ID, CHILD_ID;
    public final double WEIGHT;
    
    public ParentChildGraphEdge(int parent, int child, double weight) {
        PARENT_ID = parent;
        CHILD_ID = child;
        WEIGHT = weight;
    }

    @Override
    public int compareTo(ParentChildGraphEdge t) {
        if(WEIGHT < t.WEIGHT) return -1;
        if(WEIGHT > t.WEIGHT) return 1;
        return 0;
    }
    
    @Override
    public String toString() {
        return "{PARENT: " + Integer.toString(PARENT_ID) + ", CHILD: " + Integer.toString(CHILD_ID) + ", WEIGHT: " + Double.toString(WEIGHT) + "}";
    }
}
