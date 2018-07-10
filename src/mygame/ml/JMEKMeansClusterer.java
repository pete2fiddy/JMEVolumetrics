/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ml;

import com.jme3.math.Vector3f;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Owner
 */
public class JMEKMeansClusterer implements CentroidClusterer <Vector3f> {
    private final int N_CENTROIDS;
    private final int N_ITER;
    
    public JMEKMeansClusterer(int nCentroids, int nIter) {
        this.N_CENTROIDS = nCentroids;
        this.N_ITER = nIter;
    }


    @Override
    public Vector3f[] getClusterCentroids(Vector3f[] data) {
        return JMEKMeans.calcKMeansCentroids(data, N_CENTROIDS, N_ITER);
    }

    @Override
    public Map<Integer, Integer> clusterIds(Vector3f[] centroids, Vector3f[] data) {
        LinkedList<Integer>[] clusterLists = JMEKMeans.assignIdsToClusters(centroids, data);
        HashMap<Integer, Integer> out = new HashMap<Integer, Integer>();
        for(int i = 0; i < clusterLists.length; i++) {
            for(int id : clusterLists[i]) {
                out.put(id,i);
            }
        }
        return out;
        /*
        int[][] clusters = new int[clusterLists.length][];
        for(int i = 0; i < clusters.length; i++) {
            clusters[i] = new int[clusterLists[i].size()];
            int j = 0;
            for(int id : clusterLists[i]) {
                clusters[i][j] = id;
                j++;
            }
        }
        return clusters;
        */
    }
}
