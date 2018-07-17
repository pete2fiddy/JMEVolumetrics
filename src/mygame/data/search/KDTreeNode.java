package mygame.data.search;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import org.jblas.DoubleMatrix;



public interface KDTreeNode {
    //X in order [point num, axis]
    public void fit(double[][] X, Set<Integer> remainingIds);
    
    public void setKNearestNeighbors(double[][] X, double[] searchPoint, int k, PriorityQueue<NeighborDistance> neighborDistQueue);
    
    //both radius and radiusSquared given since both will be used fairly often
    public void setNeighborsWithinRadius(double[][] X, double[] centerPoint, double radius, double radiusSquared, Set<Integer> withinRadius); 
    
}