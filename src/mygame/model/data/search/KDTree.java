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
public class KDTree implements NearestNeighborSearcher {
    private final double[][] X;
    private KDTreeNode head;
    
    /*
    assumes X contains no duplicates
    */
    public KDTree(double[][] X) {
        //do not alter X from the value passed in the constructor -- will mess up indexing that is returned by the methods of the tree!
        //(A reminder because I did this once to remove duplicates from X, forgetting it would change the original ordering of X)
        this.X = X;
        head = new KDTreeBranchNode(0);
        fit();
    }
    
    
    private void fit() {
        head.fit(X, getUniquePointIds(X));
    }
    
    private Set<Integer> getUniquePointIds(double[][] points) {
        //DoubleMatrix used as type for uniques for sake of working hashcode and equals
        Set<DoubleMatrix> uniques = new HashSet<DoubleMatrix>();
        Set<Integer> out = new HashSet<Integer>();
        for(int i = 0; i < points.length; i++) {
            if(!uniques.contains(new DoubleMatrix(points[i]))) {
                uniques.add(new DoubleMatrix(points[i]));
                out.add(i);
            }
        }
        return out;
    }
    
    /*
    public int getNearestNeighborId(double[] searchPoint) {
        return getNearestNeighborIds(searchPoint, 1)[0];
    }
    */
    
    @Override
    public int[] getNearestNeighborIds(double[] searchPoint, int k) {
        PriorityQueue<NeighborDistance> neighborDistQueue = new PriorityQueue(k, NeighborDistance.COMPARATOR);
        for(int i = 0; i < k; i++) {
            neighborDistQueue.add(new NeighborDistance(-1, Double.MAX_VALUE));
        }
        head.setKNearestNeighbors(X, searchPoint, k, neighborDistQueue);
        int[] neighbors = new int[k];
        NeighborDistance[] neighborDists = neighborDistQueue.toArray(new NeighborDistance[k]);
        for(int i = 0; i < neighborDists.length; i++) neighbors[i] = neighborDists[i].ID;
        return neighbors;
    }
    
    @Override
    public Set<Integer> getIdsWithinRadius(double[] centerPoint, double radius) {
        HashSet<Integer> withinRadius = new HashSet<Integer>();
        head.setNeighborsWithinRadius(X, centerPoint, radius, radius*radius, withinRadius);
        return withinRadius;
    }
    
    public String getPointPath(double[] p) {
        return head.getPointPath(p);
    }
}