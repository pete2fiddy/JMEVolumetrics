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
import mygame.graph.Graph;
import mygame.pointcloud.InteractivePointCloud;
import mygame.input.VolumetricToolInput;
import mygame.ml.Segmenter;
import mygame.util.GraphUtil;
import mygame.util.SegmenterUtils;
import org.jblas.DoubleMatrix;



public class SimilarityThresholdedFloodfillCloudSegmenter implements Segmenter {
    private InteractivePointCloud pointCloud;
    private VolumetricToolInput toolInput;
    private double similarityThresholdChangePerPixelWeight = 0.005;
    private Set<Integer> currentSelectedSegmentIds = new HashSet<Integer>();
    private Set<Integer> segmentIds = new HashSet<Integer>();
    
    //should scale the sensitivity by the amount of zoom (zoom out with same mouse delta should have more tolerance than
    //zoomed in with same mouse delta)
    //has no convenient way to deal with the different ranges/bounds of different similarity metrics when thresholding
    public SimilarityThresholdedFloodfillCloudSegmenter(InteractivePointCloud pointCloud, VolumetricToolInput toolInput) {
        this.pointCloud = pointCloud;
        this.toolInput = toolInput;
    }
    
    
    private double getSimilarityThreshold() {
        double selectDeltaMag = toolInput.getCursorPos().distance(toolInput.getSelectPos());
        return 1.0/((selectDeltaMag) * similarityThresholdChangePerPixelWeight + 1);
    }
    
    @Override
    public Set<Integer> getSegmentedIds(Graph simGraph) {
        if(toolInput.getIfDiscreteAction("SELECT_TOGGLE")) {
            int nearestNeighborId = pointCloud.getNearestScreenNeighborId(toolInput.getSelectPos());
            if(nearestNeighborId < 0) return segmentIds;
            
            segmentIds.removeAll(currentSelectedSegmentIds);
            currentSelectedSegmentIds = GraphUtil.thresholdedFloodfill(simGraph, nearestNeighborId, getSimilarityThreshold());
            segmentIds.addAll(currentSelectedSegmentIds);
        } else {
            segmentIds.addAll(currentSelectedSegmentIds);
            currentSelectedSegmentIds = new HashSet<Integer>();
        }
        if(toolInput.getIfDiscreteAction("CLEAR_TOGGLE")) {
            segmentIds = new HashSet<Integer>();
        }
        return segmentIds;
    }
}
