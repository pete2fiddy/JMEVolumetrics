/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ui;

import com.jme3.math.Vector3f;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.pointcloud.PointCloud;
import mygame.graph.Graph;
import mygame.graph.SparseGraph;
import mygame.input.KeyboardSegmenterToolController;
import mygame.ml.Segmenter;
import mygame.pointcloud.InteractivePointCloudController;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class SinglePointCloudSegmenter implements Segmenter {
    private SegmenterToolControllerInterface toolInput;
    private HashSet<Integer> segmentIds = new HashSet<Integer>();
    private InteractivePointCloudController pointCloudController;
    
    public SinglePointCloudSegmenter(InteractivePointCloudController pointCloudController, SegmenterToolControllerInterface toolInput){ 
        this.pointCloudController = pointCloudController;
        this.toolInput = toolInput;
    }
    
    @Override
    public Set<Integer> getSegmentedIds(Graph simGraph) {
        if(toolInput.selectActive()) {
            int nearestNeighborId = pointCloudController.getNearestScreenNeighborId(toolInput.getCursorPos());
            if(nearestNeighborId >= 0) {
                if(toolInput.eraseActive()) {
                    segmentIds.remove(nearestNeighborId);
                } else {
                    segmentIds.add(nearestNeighborId);
                }
            }
        }
        if(toolInput.clearActive()) {
            segmentIds = new HashSet<Integer>();
        }
        return (Set<Integer>)segmentIds.clone();
    }

}
