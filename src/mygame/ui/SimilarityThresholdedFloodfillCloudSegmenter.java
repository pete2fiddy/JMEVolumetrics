/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mygame.InteractivePointCloud;
import mygame.input.VolumetricToolInput;
import mygame.ml.Segmenter;
import mygame.util.SegmenterUtils;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class SimilarityThresholdedFloodfillCloudSegmenter implements Segmenter {
    private InteractivePointCloud pointCloud;
    private VolumetricToolInput toolInput;
    private Map<Integer, Integer> idToClusterMap;
    private double similarityThresholdChangePerPixel = 0.005;
    private Set<Integer>[] clusterSets;
    
    //should scale the sensitivity by the amount of zoom (zoom out with same mouse delta should have more tolerance than
    //zoomed in with same mouse delta)
    //has no convenient way to deal with the different ranges/bounds of different similarity metrics when thresholding
    public SimilarityThresholdedFloodfillCloudSegmenter(InteractivePointCloud pointCloud, VolumetricToolInput toolInput, Map<Integer, Integer> idToClusterMap) {
        this.pointCloud = pointCloud;
        this.toolInput = toolInput;
        this.idToClusterMap = idToClusterMap;
        this.clusterSets = SegmenterUtils.convertIntoClusterSets(idToClusterMap);
    }
    
    
    private double getSimilarityThreshold() {
        double selectDeltaMag = toolInput.getCursorPos().distance(toolInput.getSelectPos());
        return 1.0/((selectDeltaMag) * similarityThresholdChangePerPixel + 1);
    }
    
    //if mouse held down, but camera transformed, then the select point can attach to a different point in the cloud
    //assumes high values are very similar, low values are very dissimilar
    @Override
    public Set<Integer> getSegmentedIds(DoubleMatrix simMatrix) {
        if(!toolInput.getIfDiscreteAction("SELECT_TOGGLE")) {
            return new HashSet<Integer>();
        }
        double minSim = getSimilarityThreshold();
        int nearestNeighborId = pointCloud.getNearestScreenNeighborId(toolInput.getSelectPos());
        if(nearestNeighborId < 0) {
            return new HashSet<Integer>();
        }
        System.out.println("MIN SIMILARITY: " + minSim);
        int nearestNeighborClusterId = idToClusterMap.get(nearestNeighborId);
        HashSet<Integer> floodfilledClusters = new HashSet<Integer>();
        threshClusterFloodfill(simMatrix, nearestNeighborClusterId, new boolean[clusterSets.length], floodfilledClusters, minSim);
        
        HashSet<Integer> floodfilledIds = new HashSet<Integer>();
        for(int floodfillCluster : floodfilledClusters) {
            floodfilledIds.addAll(clusterSets[floodfillCluster]);
        }
        return floodfilledIds;
    }
    
    private void threshClusterFloodfill(DoubleMatrix simMatrix, int clusterId, boolean[] visited, Set<Integer> selections, double minSimilarity) {
        visited[clusterId] = true;
        selections.add(clusterId);
        for(int compareClusterId = 0; compareClusterId < simMatrix.rows; compareClusterId++) {
            if(!visited[compareClusterId]) {
                if(simMatrix.get(clusterId, compareClusterId) > minSimilarity) {
                    threshClusterFloodfill(simMatrix, compareClusterId, visited, selections, minSimilarity);
                }
            }
        }
    }
    
}
