/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ml;

import java.util.Map;

/**
 *
 * @author Owner
 */
public interface CentroidClusterer <T> {
    
    public T[] getClusterCentroids(T[] data);
    //returns a map where the key is the data id, and the value is the cluster id it belongs to
    public Map<Integer, Integer> clusterIds(T[] centroids, T[] data);
}
