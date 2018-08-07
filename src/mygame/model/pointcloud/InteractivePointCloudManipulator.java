package mygame.model.pointcloud;

import com.jme3.math.ColorRGBA;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import defunct.SegmenterController;
import mygame.view.pointcloud.CloudPoint;
import mygame.view.pointcloud.PointCloud;

/*
A wrapper for a point cloud that grants the ability to denote sets of points as "selected" 
by painting them differently
*/
public class InteractivePointCloudManipulator extends PointCloudManipulator {
    
    
    private static final float POINT_SELECT_SIZE_MULTIPLIER = 3f;
    private SegmenterController toolInput;
    private Map<Integer, CloudPoint> selectedPoints = new HashMap<Integer, CloudPoint>();
    
    public InteractivePointCloudManipulator(PointCloud pointCloud) {
        super(pointCloud);
       
    }
    
    
    public void selectPoints(Set<Integer> newSelections) {
        unselectAllUnselectedPoints(newSelections);
        for(int id : newSelections) {
            selectPoints(id);
        }
        
    }
    
   
    private void unselectAllUnselectedPoints(Set<Integer> newSegmentIds){
        LinkedList<Integer> unselectedIds = new LinkedList<Integer>();
        for(int oldId : selectedPoints.keySet()) {
            if(!newSegmentIds.contains(oldId)) {
                unselectedIds.add(oldId);
            }
        }
        
        for(int unselectedId : unselectedIds) {
            unselectPoint(unselectedId);
        }
    }
    
    private void selectPoints(int... ids) {
        for(int id : ids) {
            if(!selectedPoints.containsKey(id)){
                selectedPoints.put(id, pointCloud.getPointClones(id)[0].copy());
                setPoint(id, new CloudPoint(pointCloud.getPointClones(id)[0].POINT, new ColorRGBA(1f,1f,1f,1f), pointCloud.getPointClones(id)[0].SIZE*POINT_SELECT_SIZE_MULTIPLIER));
            }
        }
    }
    
    private void unselectPoint(int... ids) {
        for(int id : ids) {
            CloudPoint unselectPoint = selectedPoints.remove(id);
            setPoint(id, unselectPoint);
        }
    }
}
