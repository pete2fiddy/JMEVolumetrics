package mygame.model.pointcloud;

import com.jme3.math.Vector3f;
import mygame.control.ui.Updatable;
import mygame.view.pointcloud.CloudPoint;
import mygame.view.pointcloud.PointCloud;

public class PointCloudManipulator implements Updatable {
    
    protected PointCloud pointCloud;
    private boolean doUpdatePoints = true, doUpdateSizes = true, doUpdateColors = true;
    
    
    public PointCloudManipulator(PointCloud pointCloud) {
        this.pointCloud = pointCloud;
    }
    
    
    
    public void setPoint(int index, CloudPoint newPoint) {
        if(!newPoint.COLOR.equals(pointCloud.getPointClones(index)[0].COLOR)) doUpdateColors = true;
        if(!newPoint.POINT.equals(pointCloud.getPointClones(index)[0].POINT)) doUpdatePoints = true;
        if(newPoint.SIZE != pointCloud.getPointClones(index)[0].SIZE) doUpdateSizes = true;
        this.pointCloud.setPoint(index, newPoint);
    }
    
    
    @Override
    public void update(float timePerFrame) {
        if(doUpdatePoints) {
            updatePoints();
        }
        if(doUpdateColors) {
            updateColors();
        }
        if(doUpdateSizes) {
            updateSizes();
        }
    }
    
    public PointCloud getCloud() {
        return pointCloud;
    }
    
    public Vector3f[] getPointClones() {
        Vector3f[] points = CloudPoint.extractPoints(pointCloud.getPointClones());
        Vector3f[] out = new Vector3f[points.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = points[i].clone();
        }
        return out;
    }
    
    protected void updatePoints() {
        pointCloud.bufferPoints();
        doUpdatePoints = false;
    }
    
    protected void updateColors() {
        pointCloud.bufferColors();
        doUpdateColors = false;
    }
    
    protected void updateSizes() {
        pointCloud.bufferSizes();
        doUpdateSizes = false;
    }
}
