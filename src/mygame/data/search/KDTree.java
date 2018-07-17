package mygame.data.search;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import org.jblas.DoubleMatrix;

/*
Median picking may be slightly bugged, sometimes have splits of 5-0/2-0, etc. (would happen if all tie on the same axis
, for example, so not impossible, but maybe I made a mistake)
*/

public class KDTree {
    private double[][] X;
    private KDTreeNode head;
    
    public KDTree(double[][] X) {
        this.X = X;
        head = new KDTreeBranchNode(0);
        fit();
    }
    
    private void fit() {
        Set<Integer> remainingIds = new HashSet<Integer>();
        for(int i = 0; i < X.length; i++) remainingIds.add(i);
        head.fit(X, remainingIds);
    }
    
    public int getNearestNeighborId(double[] searchPoint) {
        return getNearestNeighborIds(searchPoint, 1)[0];
    }
    
    public int[] getNearestNeighborIds(double[] searchPoint, int k) {
        PriorityQueue<NeighborDistance> neighborDistQueue = new PriorityQueue(k, NeighborDistance.COMPARATOR);
        for(int i = 0; i < k; i++) neighborDistQueue.add(new NeighborDistance(-1, Double.MAX_VALUE));
        head.setKNearestNeighbors(X, searchPoint, k, neighborDistQueue);
        int[] neighbors = new int[k];
        NeighborDistance[] neighborDists = neighborDistQueue.toArray(new NeighborDistance[k]);
        for(int i = 0; i < neighborDists.length; i++) neighbors[i] = neighborDists[i].ID;
        return neighbors;
    }
    
    public Set<Integer> getIdsWithinRadius(double[] centerPoint, double radius) {
        HashSet<Integer> withinRadius = new HashSet<Integer>();
        head.setNeighborsWithinRadius(X, centerPoint, radius, radius*radius, withinRadius);
        return withinRadius;
    }
}