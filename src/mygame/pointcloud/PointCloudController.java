package mygame.pointcloud;

import mygame.ui.Updatable;

public class PointCloudController implements Updatable {
    
    protected PointCloud pointCloud;
    private boolean doUpdatePoints = true, doUpdateSizes = true, doUpdateColors = true;
    
    
    public PointCloudController(PointCloud pointCloud) {
        this.pointCloud = pointCloud;
    }
    
    
    
    public void setPoint(int index, CloudPoint newPoint) {
        if(!newPoint.COLOR.equals(pointCloud.getPoint(index).COLOR)) doUpdateColors = true;
        if(!newPoint.POINT.equals(pointCloud.getPoint(index).POINT)) doUpdatePoints = true;
        if(newPoint.SIZE != pointCloud.getPoint(index).SIZE) doUpdateSizes = true;
        this.pointCloud.points[index] = newPoint;
    }
    
    
    @Override
    public void update(float timePerFrame) {
        if(doUpdatePoints) {
            updatePointBuffer();
        }
        if(doUpdateColors) {
            updateColors();
        }
        if(doUpdateSizes) {
            updateSizes();
        }
    }
    
    protected void updatePointBuffer() {
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
