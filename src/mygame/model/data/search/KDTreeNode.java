package mygame.model.data.search;

import java.util.PriorityQueue;
import java.util.Set;


public interface KDTreeNode {
    //X in order [point num, axis]
    public void fit(double[][] X, Set<Integer> remainingIds);
    
    public void setKNearestNeighbors(double[][] X, double[] searchPoint, int k, PriorityQueue<NeighborDistance> neighborDistQueue);
    
    //both radius and radiusSquared given since both will be used fairly often
    public void setNeighborsWithinRadius(double[][] X, double[] centerPoint, double radius, double radiusSquared, Set<Integer> withinRadius); 
    
    //prints the contribution of the tree node to the final path of the point as it trickles down the tree. Can remove later if I ever implement a new KDTreeNode, but is used for testing for now
    public String getPointPath(double[] p);
}