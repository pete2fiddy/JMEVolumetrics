package mygame.model.data.ml;

import java.util.LinkedList;
import org.jblas.DoubleMatrix;

public class KMeans {
    
    public static DoubleMatrix calcKMeansCentroids(DoubleMatrix X, int nCentroids, int nIter){
        DoubleMatrix centroids = selectNRandomPoints(X, nCentroids);
        for(int iter = 0; iter < nIter; iter++) {
            DoubleMatrix[] clusters = assignToClusters(centroids, X);
            for(int centroidId = 0; centroidId < centroids.rows; centroidId++){
                if(clusters[centroidId].rows > 0) {
                    //calculate the mean of the cluster and set clusters[centroidId] to that average
                    centroids.putRow(centroidId, clusters[centroidId].columnMeans());
                } else {
                    //reassign centroids[centroidId] since no points got assigned to it
                    centroids.putRow(centroidId, pickRandomPoint(X));
                }
            }
        }
        return centroids;
    }
    
    public static LinkedList<Integer>[] assignIdsToClusters(DoubleMatrix centroids, DoubleMatrix X) {
        LinkedList<Integer>[] clusters = new LinkedList[centroids.rows];
        for(int i = 0; i < clusters.length; i++){
            clusters[i] = new LinkedList<Integer>();
        }
        for(int id = 0; id < X.rows; id++) {
            DoubleMatrix x = X.getRow(id);
            int minCentroidId = getMinDistCentroidId(centroids, x);
            clusters[minCentroidId].add(id);
        }
        return clusters;
    }
    
    public static DoubleMatrix[] assignToClusters(DoubleMatrix centroids, DoubleMatrix X) {
        DoubleMatrix[] clusters = new DoubleMatrix[centroids.rows];
        for(int i = 0; i < clusters.length; i++) {
            clusters[i] = DoubleMatrix.zeros(0,X.columns);
        }
        for(int i = 0; i < X.rows; i++) {
            DoubleMatrix x = X.getRow(i);
            int minCentroidId = getMinDistCentroidId(centroids, x);
            clusters[minCentroidId] = DoubleMatrix.concatVertically(clusters[minCentroidId], x);
        }
        return clusters;
    }
    
    public static int getMinDistCentroidId(DoubleMatrix centroids, DoubleMatrix x) {
        DoubleMatrix xRelToCentroidsVecs = centroids.subRowVector(x);
        double minDistSqr = xRelToCentroidsVecs.getRow(0).dot(xRelToCentroidsVecs.getRow(0));
        int minCentroidId = 0;
        for(int centroidId = 1; centroidId < xRelToCentroidsVecs.rows; centroidId++) {
            DoubleMatrix relCentroid = xRelToCentroidsVecs.getRow(centroidId);
            double newDistSqr = relCentroid.dot(relCentroid);
            if(newDistSqr < minDistSqr) {
                minDistSqr = newDistSqr;
                minCentroidId = centroidId;
            }
        }
        return minCentroidId;
    }
    
    private static DoubleMatrix selectNRandomPoints(DoubleMatrix X, int nPoints) {
        DoubleMatrix out = DoubleMatrix.zeros(nPoints, X.columns);
        for(int i = 0; i < out.rows; i++){
            out.putRow(i, pickRandomPoint(X));
        }
        return out;
    }
    
    private static DoubleMatrix pickRandomPoint(DoubleMatrix X) {
        return X.getRow((int)(Math.random()*X.rows));
    }
}
