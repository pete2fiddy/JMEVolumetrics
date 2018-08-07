/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.control.ui;

import java.util.HashSet;
import java.util.Set;
import mygame.graph.Graph;
import mygame.ml.Segmenter;

/**
 *
 * @author Owner
 */
public class SinglePointCloudSegmenter implements Segmenter {
    private SegmenterController toolController;
    private HashSet<Integer> segmentIds = new HashSet<Integer>();
    
    public SinglePointCloudSegmenter(SegmenterController toolInput){ 
        this.toolController = toolInput;
    }
    
    @Override
    public Set<Integer> getSegmentedIds(Graph simGraph) {
        if(toolController.selectActive()) {
            int nearestNeighborId = toolController.getNearestScreenNeighborId(toolController.getCursorPos());
            if(nearestNeighborId >= 0) {
                if(toolController.eraseActive()) {
                    segmentIds.remove(nearestNeighborId);
                } else {
                    segmentIds.add(nearestNeighborId);
                }
            }
        }
        if(toolController.clearActive()) {
            segmentIds = new HashSet<Integer>();
        }
        return (Set<Integer>)segmentIds.clone();
    }

}
