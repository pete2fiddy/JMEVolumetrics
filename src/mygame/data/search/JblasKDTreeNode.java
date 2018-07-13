package mygame.data.search;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.jblas.DoubleMatrix;

public class JblasKDTreeNode {
    private final Comparator VEC_COMPARATOR;
    private final int SPLIT_AXIS;
    private double splitValue;
    private int leafId;
    private JblasKDTreeNode[] children = null;
    
    public JblasKDTreeNode(int splitAxis) {
        this.SPLIT_AXIS = splitAxis;
        this.VEC_COMPARATOR = new Comparator<double[]>() {
            @Override
            public int compare(double[] t, double[] t1) {
                if(t[SPLIT_AXIS] < t1[SPLIT_AXIS]) return -1;
                if(t[SPLIT_AXIS] > t1[SPLIT_AXIS]) return 1;
                return 0;
            }
        };
    }
   
    protected boolean isValueLeaf() {return children == null && leafId != -1;}
    
    protected boolean isLeaf() {return children == null;}
    
    public Set<Integer> getIdsWithinRadius(DoubleMatrix X, DoubleMatrix searchPoint, double radius) {
        Set<Integer> out = new HashSet<Integer>();
        setIdsWithinRadius(X, searchPoint, radius, out);
        return out;
    }
    
    
    protected void setIdsWithinRadius(DoubleMatrix X, DoubleMatrix searchPoint, double radius, Set<Integer> inRadius) {
        if(isLeaf()) {
            if(isValueLeaf()) {
                DoubleMatrix leafVec = X.getRow(leafId);
                double searchDistToLeafVec = leafVec.distance2(searchPoint);
                if(searchDistToLeafVec < radius) inRadius.add(leafId);
            }
            return;
        }
        double[] childMinDists = regionsMinPossibleDistToPoint(searchPoint.toArray());
        for(int i = 0; i < childMinDists.length; i++) {
            if(childMinDists[i] < radius) children[i].setIdsWithinRadius(X, searchPoint, radius, inRadius);
        }
    }
    
    public int[] getNNearestNeighborIds(DoubleMatrix X, DoubleMatrix searchPoint, int n) {
        int[] neighbors = new int[n];
        double[] distances = new double[n];
        for(int i = 0; i < neighbors.length; i++) {
            neighbors[i] = -1;
            distances[i] = Double.MAX_VALUE;
        }
        setNNearestNeighborIds(X, searchPoint.toArray(), neighbors, distances);
        return neighbors;
    }
    
    
    protected void setNNearestNeighborIds(DoubleMatrix X, double[] searchPoint, int[] neighbors, double[] distances) {
        int maxDistNeighbor = getMaxDistNeighbor(distances);
        double maxDist = distances[maxDistNeighbor];
        if(isLeaf()) {
            if(isValueLeaf()) {
                DoubleMatrix leafVec = X.getRow(leafId);
                double searchDistToLeafVec = leafVec.distance2(new DoubleMatrix(new double[][] {searchPoint}));
                if(searchDistToLeafVec < maxDist) {
                    neighbors[maxDistNeighbor] = leafId;
                    distances[maxDistNeighbor] = searchDistToLeafVec;
                }
            }
            return;
        }
        
        double[] childMinDists = regionsMinPossibleDistToPoint(searchPoint);
        if(childMinDists[0] < maxDist) children[0].setNNearestNeighborIds(X, searchPoint, neighbors, distances);
        
        //max dist neighbor and max dist need to be recalculated as the call above may have altered it to make it smaller
        maxDistNeighbor = getMaxDistNeighbor(distances);
        maxDist = distances[maxDistNeighbor];
        if(childMinDists[1] < maxDist) children[1].setNNearestNeighborIds(X, searchPoint, neighbors, distances);
    }
    
    private int getMaxDistNeighbor(double[] distances) {
        int maxDistNeighbor = 0;
        for(int i = 0; i < distances.length; i++) {
            if(distances[i] > distances[maxDistNeighbor]) maxDistNeighbor = i;
        }
        return maxDistNeighbor;
    }
            
    protected double[] regionsMinPossibleDistToPoint(double[] searchPoint) {
        double[] out = new double[2];
        out[0] = (splitValueCompare(searchPoint) < 0)? 0:Math.abs(searchPoint[SPLIT_AXIS] - splitValue);
        out[1] = (splitValueCompare(searchPoint) >= 0)? 0:Math.abs(searchPoint[SPLIT_AXIS] - splitValue);
        return out;
    }
      
    
    protected void fit(double[][] X, Set<Integer> remainingIds) {
        if(remainingIds.size() < 2) {
            if(remainingIds.size() == 1) {
                leafId = (int)remainingIds.toArray()[0];
            }
            return;
        }
        splitValue = getSplitValue(getXRemainingArr(X, remainingIds));
        Set<Integer>[] splits = split(X, remainingIds);
        children = new JblasKDTreeNode[2];
        for(int i = 0; i < 2; i++) {
            children[i] = new JblasKDTreeNode((SPLIT_AXIS+1)%X[0].length);
            children[i].fit(X, splits[i]);
        }
    }
    
    private Set<Integer>[] split(double[][] X, Set<Integer> remainingIds) {
        Set<Integer>[] splits = new HashSet[]{new HashSet<Integer>(), new HashSet<Integer>()};
        for(int id : remainingIds){
            if(splitValueCompare(X[id]) <= 0) {
                splits[0].add(id);
            } else {
                splits[1].add(id);
            }
        }
        return splits;
    }
    
    
    private int splitValueCompare(double[] t) {
        if(t[SPLIT_AXIS] < splitValue) return -1;
        if(t[SPLIT_AXIS] > splitValue) return 1;
        return 0;
    }
    
    private double[][] getXRemainingArr(double[][] X, Set<Integer> remainingIds) {
        double[][] out = new double[remainingIds.size()][X[0].length];
        int i = 0;
        for(int id : remainingIds) {
            out[i++] = X[id];
        }
        return out;
    }
    
    private double getSplitValue(double[][] XRemaining) {
        double median = XRemaining[Select.quickSelect(XRemaining, VEC_COMPARATOR, XRemaining.length/2)][SPLIT_AXIS];
        if(XRemaining.length%2 == 0){
            median += XRemaining[Select.quickSelect(XRemaining, VEC_COMPARATOR, XRemaining.length/2 - 1)][SPLIT_AXIS];
            median /= 2.0;
        }
        return median;
    }
}
