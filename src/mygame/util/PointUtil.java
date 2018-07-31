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
import org.jblas.DoubleMatrix;

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
    
    /*
    returns a double[][] where out[i] is corersponds to the bounds of the ith axis of X, out[i][0] is the lower bound of the ith axis,
    out[i][1] is the upper bound of the ith axis
    */
    public static double[][] getPointBounds3d(DoubleMatrix X) {
        assert(X.columns == 3);
        double[][] out = new double[3][2];
        for(int i = 0; i < out.length; i++) {
            out[i][0] = X.get(0, i);
            out[i][1] = X.get(0, i);
        }
        
        for(int i = 0; i < X.rows; i++) {
            for(int j = 0; j < out.length; j++) {
                if(X.get(i,j) < out[j][0]) out[j][0] = X.get(i,j);
                if(X.get(i,j) > out[j][1]) out[j][1] = X.get(i,j);
            }
        }
        return out;
    }
}
