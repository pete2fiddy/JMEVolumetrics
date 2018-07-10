/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.util;

import com.jme3.math.Matrix4f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 *
 * @author Owner
 */
public class PointUtil {
    
    public static int getNearestNeighborId(Vector3f[] points, Vector3f point) {
        int minId = 0;
        float minDist = point.distance(points[0]);
        for(int i = 1; i < points.length; i++) {
            float newDist = point.distanceSquared(points[i]);
            if(newDist < minDist) {
                minDist = newDist;
                minId = i;
            }
        }
        return minId;
    } 
    
    public static int getNearestScreenNeighborId(Vector3f[] points, Vector2f point, Matrix4f pretransform, Camera cam) {
        int minId = 0;
        float minDist = point.distanceSquared(getScreenPos(pretransform.mult(points[0], null), cam));
        for(int i = 1; i < points.length; i++) {
            float newDist = point.distanceSquared(getScreenPos(pretransform.mult(points[i], null), cam));
            if(newDist < minDist){// || pretransform.mult(points[minId],null).getZ() < cam.getFrustumNear()){
               
                minDist = newDist;
                minId = i;
            }
        }
        return minId;
    }
    
    public static Vector2f getScreenPos(Vector3f p, Camera cam) {
        Vector3f pos = cam.getScreenCoordinates(p);
        return new Vector2f(pos.getX(), pos.getY());
    }
}
