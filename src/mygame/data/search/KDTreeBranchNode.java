package mygame.data.search;

import mygame.util.SelectUtil;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;


public class KDTreeBranchNode implements KDTreeNode {
    private final int SPLIT_AXIS;
    private double splitValue;
    private KDTreeNode[] children = new KDTreeNode[2];
    
    public KDTreeBranchNode(int splitAxis) {
        this.SPLIT_AXIS = splitAxis;
    }

    
    @Override
    public void setNeighborsWithinRadius(double[][] X, double[] centerPoint, double radius, double radiusSquared, Set<Integer> withinRadius) {
        double[] childMinDists = regionsMinPossibleDistToPoint(centerPoint);
        for(int i = 0; i < childMinDists.length; i++) {
            if(children[i] != null && childMinDists[i] < radius) {
                children[i].setNeighborsWithinRadius(X, centerPoint, radius, radiusSquared, withinRadius);
            }
        }
    }
    
    @Override
    public void setKNearestNeighbors(double[][] X, double[] searchPoint, int k, PriorityQueue<NeighborDistance> neighborDistQueue) {
        double[] childMinDists = regionsMinPossibleDistToPoint(searchPoint); 
        if(childMinDists[0] < childMinDists[1]){
            //childMinDists[0] is 0
            if(children[0] != null) children[0].setKNearestNeighbors(X, searchPoint, k, neighborDistQueue);
            if(children[1] != null && childMinDists[1] < neighborDistQueue.peek().DISTANCE) children[1].setKNearestNeighbors(X, searchPoint, k, neighborDistQueue);
        } else {
            //childMinDists[1] is 0
            if(children[1] != null) children[1].setKNearestNeighbors(X, searchPoint, k, neighborDistQueue);
            if(children[0] != null && childMinDists[0] < neighborDistQueue.peek().DISTANCE) children[0].setKNearestNeighbors(X, searchPoint, k, neighborDistQueue);
        }
    }
    
    private double[] regionsMinPossibleDistToPoint(double[] searchPoint) {
        if(searchPoint[SPLIT_AXIS] < splitValue) {
            return new double[] {0, Math.abs(searchPoint[SPLIT_AXIS] - splitValue)};
        }
        return new double[] {Math.abs(searchPoint[SPLIT_AXIS] - splitValue), 0};
    }
    
    @Override
    public void fit(double[][] X, Set<Integer> remainingIds) {
        double[][] XRemaining = getXSubset(X, remainingIds);
        this.splitValue = getSplitValue(XRemaining);
        Set<Integer>[] childSplits = childSplit(X, remainingIds);
        for(int i = 0; i < childSplits.length; i++) {
            if(childSplits[i].size() > 1) {
                //is not a leaf
                children[i] = new KDTreeBranchNode((SPLIT_AXIS+1)%X[0].length);
            } else {
                //is a leaf
                if(childSplits[i].size() == 1) {
                    children[i] =  new KDTreeValueLeafNode();
                } else {
                    children[i] = null;
                }
            }
            if(children[i] != null) children[i].fit(X, childSplits[i]);
        }
    }
    
    private static double[][] getXSubset(double[][]X, Set<Integer> subsetInds) {
        double[][] out = new double[subsetInds.size()][X[0].length];
        int i = 0;
        for(int subsetInd : subsetInds) {
            out[i++] = X[subsetInd];
        }
        return out;
    }
    
    private double getSplitValue(double[][] XRemaining) {
        Comparator<double[]> c = new Comparator<double[]>() {
            @Override
            public int compare(double[] t, double[] t1) {
                if(t[SPLIT_AXIS] < t1[SPLIT_AXIS]) return -1;
                if(t[SPLIT_AXIS] > t1[SPLIT_AXIS]) return 1;
                return 0;
            }
        };
        double median = XRemaining[SelectUtil.quickSelect(XRemaining, c, (XRemaining.length/2)+1)][SPLIT_AXIS];
        
        if(XRemaining.length%2 == 0) {
            median += XRemaining[SelectUtil.quickSelect(XRemaining, c, XRemaining.length/2 - 1)][SPLIT_AXIS];
            median /= 2.0;
        }
        return median;
    }
    
    private Set<Integer>[] childSplit(double[][] X, Set<Integer> remainingIds) {
        Set<Integer>[] childSplits = new Set[2];
        for(int i = 0; i < childSplits.length; i++) {
            childSplits[i] = new HashSet<Integer>();
        }
        for(int id : remainingIds) {
            //not sure if this should be < or <= -- was crashing on cases with 
            //two in the subset if they were almost exactly identical points to each other,
            //but with some lossiness (duplicate removal doesn't remove them, but they are considered 
            //close enough to be considered equal)
            if(X[id][SPLIT_AXIS] < splitValue) {
                childSplits[0].add(id);
            } else {
                childSplits[1].add(id);
            }
        }
        return childSplits;
    }

    @Override
    public String getPointPath(double[] p) {
        String out = "";
        out += "p[" + Integer.toString(SPLIT_AXIS) +"] = " + Double.toString(p[SPLIT_AXIS]) + " ";
        if(p[SPLIT_AXIS] < splitValue) {
            out += "< " + Double.toString(splitValue) + " --> ";
            if(children[0] == null) return "BRANCH NULL, TERMINATING!";
            out += children[0].getPointPath(p);
        } else {
            out += "> " + Double.toString(splitValue) + " --> ";
            if(children[1] == null) return "BRANCH NULL, TERMINATING!";
            out += children[1].getPointPath(p);
        }
        return out;
    }
}
