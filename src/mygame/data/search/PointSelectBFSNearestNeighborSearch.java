package mygame.data.search;

import mygame.data.search.BFSNearestNeighborScreenSearch;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.Set;

public class PointSelectBFSNearestNeighborSearch extends BFSNearestNeighborScreenSearch {
    
    public PointSelectBFSNearestNeighborSearch(Camera cam, Vector3f[] X) {
        super(cam, X);
    }
    
    public int getNearestNeighborId(Vector3f screenSearchPoint, int depthToSearch) {
        Set<Integer> nearestNeighbors = super.getNearestNeighborIds(new int[] {(int)screenSearchPoint.getX(),
            (int)screenSearchPoint.getY()}, depthToSearch);
        
        float minDist = -1;
        int minId = -1;
        for(int id : nearestNeighbors) {
            float newDist = cam.distanceToNearPlane(pointTransform.mult(X[id], null));
            if(minDist < 0 || newDist < minDist) {
                minDist = newDist;
                minId = id;
            }
        }
        return minId;
    }
    
}
