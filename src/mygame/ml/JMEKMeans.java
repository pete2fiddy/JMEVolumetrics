/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ml;

import com.jme3.math.Vector3f;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Owner
 */
public class JMEKMeans {
    
    
    public static Vector3f[] calcKMeansCentroids(Vector3f[] X, int nCentroids, int nIter) {
        Vector3f[] centroids = selectNRandomPoints(X, nCentroids);
        for(int iter = 0; iter < nIter; iter++) {
            LinkedList<Vector3f>[] clusters = assignToClusters(centroids, X);
            for(int centroidId = 0; centroidId < centroids.length; centroidId++){
                if(clusters[centroidId].size() > 0) {
                    //assign centroid to mean of the cluster
                    centroids[centroidId] = calcAvgVec(clusters[centroidId]);
                } else {
                    centroids[centroidId] = pickRandomPoint(X);
                }
            }
        }
        return centroids;
    }
    
    private static Vector3f calcAvgVec(List<Vector3f> vecs) {
        Iterator<Vector3f> iterator = vecs.iterator();
        Vector3f avg = new Vector3f(0f,0f,0f);
        while(iterator.hasNext()) {
            avg = avg.add(iterator.next());
        }
        return avg.divide(vecs.size());
    }
    
    
    
    public static LinkedList<Vector3f>[] assignToClusters(Vector3f[] centroids, Vector3f[] X) {
        LinkedList<Vector3f>[] clusters = new LinkedList[centroids.length];
        for(int i = 0; i < clusters.length; i++) {
            clusters[i] = new LinkedList<Vector3f>();
        }
        for(Vector3f x : X) {
            int minCentroidId = getMinDistCentroidId(centroids, x);
            clusters[minCentroidId].add(x);
        }
        return clusters;
    }
    
    
    
    public static int getMinDistCentroidId(Vector3f[] centroids, Vector3f x) {
        float minDistSqr = centroids[0].distanceSquared(x);
        int minCentroidId = 0;
        for(int centroidId = 1; centroidId < centroids.length; centroidId++) {
            float newDistSqr = centroids[centroidId].distanceSquared(x);
            if(newDistSqr < minDistSqr) {
                minDistSqr = newDistSqr;
                minCentroidId = centroidId;
            }
        }
        return minCentroidId;
    }
    
    
    

    private static Vector3f[] selectNRandomPoints(Vector3f[] X, int nPoints) {
        Vector3f[] out = new Vector3f[nPoints];
        for(int i = 0; i < out.length; i++) {
            out[i] = pickRandomPoint(X);
        }
        return out;
    }
    
    private static Vector3f pickRandomPoint(Vector3f[] X) {
        return X[(int)(Math.random()*X.length)];
    }
}
