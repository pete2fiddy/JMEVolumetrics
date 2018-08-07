/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package defunct.segment;

import defunct.Segmenter;
import java.util.HashSet;
import java.util.Set;
import defunct.SegmenterController;
import mygame.model.graph.Graph;
import mygame.util.GraphUtil;



public class SimilarityThresholdedFloodfillCloudSegmenter implements Segmenter {
    private SegmenterController toolController;
    private Set<Integer> currentSelectedSegmentIds = new HashSet<Integer>();
    private Set<Integer> segmentIds = new HashSet<Integer>();
    
    //should scale the sensitivity by the amount of zoom (zoom out with same mouse delta should have more tolerance than
    //zoomed in with same mouse delta)
    //has no convenient way to deal with the different ranges/bounds of different similarity metrics when thresholding
    public SimilarityThresholdedFloodfillCloudSegmenter(SegmenterController toolController) {
        this.toolController = toolController;
    }
    
    
    private double getSimilarityThreshold() {
        double selectDeltaMag = toolController.getCursorPos().distance(toolController.getSelectPos());
        return 1.0/((selectDeltaMag) * toolController.getTolerance() + 1);
    }
    
    @Override
    public Set<Integer> getSegmentedIds(Graph simGraph) {
        if(toolController.selectActive()) {
            int nearestNeighborId = toolController.getNearestScreenNeighborId(toolController.getSelectPos());
            if(nearestNeighborId < 0) return segmentIds;
            
            segmentIds.removeAll(currentSelectedSegmentIds);
            currentSelectedSegmentIds = GraphUtil.thresholdedFloodfill(simGraph, nearestNeighborId, getSimilarityThreshold());
            segmentIds.addAll(currentSelectedSegmentIds);
        } else {
            segmentIds.addAll(currentSelectedSegmentIds);
            currentSelectedSegmentIds = new HashSet<Integer>();
        }
        if(toolController.clearActive()) {
            segmentIds = new HashSet<Integer>();
        }
        return segmentIds;
    }
}
