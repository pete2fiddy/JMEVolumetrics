package mygame.model.data.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import org.jblas.DoubleMatrix;

/*
Median picking may be slightly bugged, sometimes have splits of 5-0/2-0, etc. (would happen if all tie on the same axis
, for example, so not impossible, but maybe I made a mistake)
*/
public interface NearestNeighborSearcher {
    
    public int[] getNearestNeighborIds(double[] searchPoint, int k);
    
    public Set<Integer> getIdsWithinRadius(double[] centerPoint, double radius);
}