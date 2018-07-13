/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ui;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mygame.data.search.JblasKDTree;
import mygame.pointcloud.InteractivePointCloud;
import mygame.input.VolumetricToolInput;
import mygame.ml.Segmenter;
import mygame.util.JblasJMEConverter;
import mygame.util.SegmenterUtils;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class SphericalPaintBrushPointCloudSegmenter implements Segmenter {
    protected InteractivePointCloud pointCloud;
    protected VolumetricToolInput toolInput;
    protected Vector3f[] X;
    protected float brushRadius = 0.25f;
    Set<Integer>[] clusterSets;
    HashSet<Integer> segmentIds = new HashSet<Integer>();
    private JblasKDTree kdTree;
    
    public SphericalPaintBrushPointCloudSegmenter(InteractivePointCloud pointCloud, Vector3f[] X, JblasKDTree kdTree, 
            VolumetricToolInput toolInput) {
        this.X = X;
        this.pointCloud = pointCloud;
        this.toolInput = toolInput;
        this.kdTree = kdTree;
    }
    
    
    @Override
    public Set<Integer> getSegmentedIds(DoubleMatrix simMatrix) {
        if(toolInput.getIfDiscreteAction("SELECT_TOGGLE")) {
            int nearestNeighborId = pointCloud.getNearestScreenNeighborId(toolInput.getCursorPos());
            if(nearestNeighborId >= 0) {
                Set<Integer> withinRadius = getAllWithinRadius(simMatrix, nearestNeighborId, brushRadius);
                if (toolInput.getIfDiscreteAction("ERASE_TOGGLE")) {
                    segmentIds.removeAll(withinRadius);
                } else {
                    segmentIds.addAll(withinRadius);
                }
            }
        }
        if(toolInput.getIfDiscreteAction("CLEAR_TOGGLE")) {
            segmentIds = new HashSet<Integer>();
        }
        return (Set<Integer>)segmentIds.clone();
    }
    /*simMatrix is passed for extensions to override how this method operates*/
    protected Set<Integer> getAllWithinRadius(DoubleMatrix simMatrix, int centerId, double radius) {
        return kdTree.getIdsWithinRadius(JblasJMEConverter.toDoubleMatrix(X[centerId]), radius);
        /*
        HashSet<Integer> out = new HashSet<Integer>();
        double radiusSqr = radius*radius;
        for(int i = 0; i < centroids.length; i++) {
            if(X[centerId].distance(centroids[i]) < CENTROID_RADIUS_SEARCH_SLACK_MULTIPLIER*radius) {
                for(int id : clusterSets[i]) {
                    if(X[id].distanceSquared(X[centerId]) < radiusSqr) {
                        out.add(id);
                    }
                }
            }
        }
        return out;
        */
    }
}
