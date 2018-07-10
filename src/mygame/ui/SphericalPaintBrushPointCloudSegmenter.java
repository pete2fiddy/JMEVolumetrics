/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ui;

import com.jme3.math.Vector3f;
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
public class SphericalPaintBrushPointCloudSegmenter implements Segmenter {
    private final double CENTROID_RADIUS_SEARCH_SLACK_MULTIPLIER = 3;
    private InteractivePointCloud pointCloud;
    private VolumetricToolInput toolInput;
    private Vector3f[] X;
    private Vector3f[] centroids;
    float brushRadius = 0.5f;
    Set<Integer>[] clusterSets;
    HashSet<Integer> segmentIds = new HashSet<Integer>();
    
    public SphericalPaintBrushPointCloudSegmenter(InteractivePointCloud pointCloud,
            Vector3f[] X, Vector3f[] centroids, Map<Integer, Integer> idToClusterMap, 
            VolumetricToolInput toolInput) {
        this.pointCloud = pointCloud;
        this.toolInput = toolInput;
        this.X = X;
        this.centroids = centroids;
        this.clusterSets = SegmenterUtils.convertIntoClusterSets(idToClusterMap);
    }
    
    
    @Override
    public Set<Integer> getSegmentedIds(DoubleMatrix simMatrix) {
        if(!toolInput.getIfDiscreteAction("SELECT_TOGGLE")) {
           
            segmentIds = new HashSet<Integer>();
            return (Set<Integer>)segmentIds.clone();
        }
        int nearestNeighborId = pointCloud.getNearestScreenNeighborId(toolInput.getCursorPos());
        if(nearestNeighborId >= 0) {
            addAllWithinRadius(nearestNeighborId, brushRadius);
        }
        return (Set<Integer>)segmentIds.clone();
    }
    
    private void addAllWithinRadius(int centerId, double radius) {
        for(int i = 0; i < centroids.length; i++) {
            if(X[centerId].distance(centroids[i]) < CENTROID_RADIUS_SEARCH_SLACK_MULTIPLIER*radius) {
                for(int id : clusterSets[i]) {
                    if(X[id].distance(X[centerId]) < radius) {
                        segmentIds.add(id);
                    }
                }
            }
        }
    }
    
}
