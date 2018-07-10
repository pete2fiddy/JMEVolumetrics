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
import mygame.InteractivePointCloud;
import mygame.PointCloud;
import mygame.Updatable;
import mygame.input.VolumetricToolInput;
import mygame.ml.Segmenter;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class SinglePointCloudSegmenter implements Segmenter {
    private VolumetricToolInput toolInput;
    private HashSet<Integer> segmentIds = new HashSet<Integer>();
    private InteractivePointCloud pointCloud;
    
    public SinglePointCloudSegmenter(InteractivePointCloud pointCloud, VolumetricToolInput toolInput){ 
        this.pointCloud = pointCloud;
        this.toolInput = toolInput;
    }
    
    @Override
    public Set<Integer> getSegmentedIds(DoubleMatrix simMatrix) {
        if(!toolInput.getIfDiscreteAction("SELECT_TOGGLE")) {
           
            segmentIds = new HashSet<Integer>();
            return (Set<Integer>)segmentIds.clone();
        }
        int nearestNeighborId = pointCloud.getNearestScreenNeighborId(toolInput.getCursorPos());
        if(nearestNeighborId >= 0) {
            segmentIds.add(nearestNeighborId);
        }
        return (Set<Integer>)segmentIds.clone();
    }

}