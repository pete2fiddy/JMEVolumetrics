/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ml;

/**
 *
 * @author Owner
 */
public interface CentroidClusterer <T> {
    
    public T[] getClusterCentroids(T[] data);
    public int[][] clusterIds(T[] centroids, T[] data);
}
