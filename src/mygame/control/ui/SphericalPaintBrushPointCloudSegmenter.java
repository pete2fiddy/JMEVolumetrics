/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.control.ui;

import com.jme3.math.Vector3f;
import java.util.HashSet;
import java.util.Set;
import mygame.data.search.NearestNeighborSearcher;
import mygame.graph.Graph;
import mygame.ml.Segmenter;
import mygame.util.JblasJMEConverter;

/**
 *
 * @author Owner
 */
public class SphericalPaintBrushPointCloudSegmenter implements Segmenter {
    //protected InteractivePointCloudController pointCloudController;
    protected SegmenterController segmenterController;
    protected Vector3f[] X;
    Set<Integer>[] clusterSets;
    HashSet<Integer> segmentIds = new HashSet<Integer>();
    private NearestNeighborSearcher kdTree;
    
    public SphericalPaintBrushPointCloudSegmenter(Vector3f[] X, NearestNeighborSearcher kdTree, 
            SegmenterController toolInput) {
        this.X = X;
        this.segmenterController = toolInput;
        this.kdTree = kdTree;
    }
    
    
    @Override
    public Set<Integer> getSegmentedIds(Graph simGraph) {
        if(segmenterController.selectActive()) {
            int nearestNeighborId = segmenterController.getNearestScreenNeighborId(segmenterController.getCursorPos());
            if(nearestNeighborId >= 0) {
                Set<Integer> withinRadius = getAllWithinRadius(simGraph, nearestNeighborId, segmenterController.getSelectionRadius());
                if (segmenterController.eraseActive()) {
                    segmentIds.removeAll(withinRadius);
                } else {
                    segmentIds.addAll(withinRadius);
                }
            }
        }
        if(segmenterController.clearActive()) {
            segmentIds = new HashSet<Integer>();
        }
        return (Set<Integer>)segmentIds.clone();
    }
    /*simGraph is passed for extensions to override how this method operates*/
    protected Set<Integer> getAllWithinRadius(Graph simGraph, int centerId, double radius) {
        return kdTree.getIdsWithinRadius(JblasJMEConverter.toArr(X[centerId])[0], radius);
    }
}
