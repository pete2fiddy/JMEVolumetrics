package mygame.data.search;

import java.util.Comparator;

public class NeighborDistance {
    public final int ID;
    public final double DISTANCE;

    public NeighborDistance(int id, double distance) {
        this.ID = id;
        this.DISTANCE = distance;
    }
    
    public static final Comparator<NeighborDistance> COMPARATOR = new Comparator<NeighborDistance>() {
        @Override
        public int compare(NeighborDistance t, NeighborDistance t1) {
            //usual compareTo order is reversed so that the "least" element of the priority queue contains the 
            //maximum distance
            if(t.DISTANCE < t1.DISTANCE) return 1;
            if(t.DISTANCE > t1.DISTANCE) return -1;
            return 0;
        }
    };
}
