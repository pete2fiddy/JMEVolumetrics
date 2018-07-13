package mygame.data.search;

import java.util.HashSet;
import java.util.Set;
import org.jblas.DoubleMatrix;

/*
Median picking may be slightly bugged, sometimes have splits of 5-0/2-0, etc. (would happen if all tie on the same axis
, for example, so not impossible, but maybe I made a mistake)
*/
public class JblasKDTree {
    private DoubleMatrix X;
    private JblasKDTreeNode head;
    
    
    public JblasKDTree(DoubleMatrix X) {
        this.X = X;
        head = new JblasKDTreeNode(0);
        fit();
    }
    
    private void fit() {
        double[][] XArr = X.toArray2();
        Set<Integer> remainingIds = new HashSet<Integer>();
        for(int i = 0; i < X.rows; i++) {
            remainingIds.add(i);
        }
        head.fit(XArr, remainingIds);
    }
    
    public int getNearestNeighborId(DoubleMatrix searchPoint) {
        return head.getNNearestNeighborIds(X, searchPoint, 1)[0];
    }
    
    public int[] getNearestNeighborIds(DoubleMatrix searchPoint, int nNeighbors) {
        return head.getNNearestNeighborIds(X, searchPoint, nNeighbors);
    }
    
    public Set<Integer> getIdsWithinRadius(DoubleMatrix searchPoint, double radius) {
        return head.getIdsWithinRadius(X, searchPoint, radius);
    }
}
