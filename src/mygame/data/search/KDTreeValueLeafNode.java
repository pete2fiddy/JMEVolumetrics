package mygame.data.search;

import java.util.PriorityQueue;
import java.util.Set;

public class KDTreeValueLeafNode implements KDTreeNode {
    private int leafId;
    
    @Override
    public void fit(double[][] X, Set<Integer> remainingIds) {
        assert(remainingIds.size() == 1);
        this.leafId = (remainingIds.toArray(new Integer[remainingIds.size()]))[0];
    }

    @Override
    public void setKNearestNeighbors(double[][] X, double[] searchPoint, int k, PriorityQueue<NeighborDistance> neighborDistQueue) {
        double sqrDistBetween = sqrDistBetween(X[leafId], searchPoint);
        if(neighborDistQueue.size() < k) {
            neighborDistQueue.add(new NeighborDistance(leafId, sqrDistBetween));
            return;
        }
        if(sqrDistBetween < neighborDistQueue.peek().DISTANCE) {
            neighborDistQueue.poll();
            neighborDistQueue.add(new NeighborDistance(leafId, sqrDistBetween));
        }
    }
    
    private static double sqrDistBetween(double[] v1, double[] v2) {
        double out = 0;
        for(int i = 0; i < v1.length; i++) {
            double sub = v2[i]-v1[i];
            out += sub*sub;
        }
        return out;
    }

    @Override
    public void setNeighborsWithinRadius(double[][] X, double[] centerPoint, double radius, double radiusSquared, Set<Integer> withinRadius) {
        if(sqrDistBetween(X[leafId], centerPoint) < radiusSquared) withinRadius.add(leafId);
    }

    @Override
    public String getPointPath(double[] p) {
        return "Return ID: " + Integer.toString(leafId);
    }
}
