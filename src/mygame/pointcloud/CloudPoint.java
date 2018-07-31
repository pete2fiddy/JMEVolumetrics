/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.pointcloud;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

/**
 *
 * @author Owner
 */
public class CloudPoint {
    public final float SIZE;
    public final ColorRGBA COLOR;
    public final Vector3f POINT;
    
    public CloudPoint(Vector3f point, ColorRGBA color, float size) {
        this.POINT = point;
        this.SIZE = size;
        this.COLOR = color;
    }
    
    public static Vector3f[] extractPoints(CloudPoint[] cPoints) {
        Vector3f[] points = new Vector3f[cPoints.length];
        for(int i = 0; i < points.length; i++){
            points[i] = cPoints[i].POINT;
        }
        return points.clone();
    }
    
    public static ColorRGBA[] extractColors(CloudPoint[] cPoints) {
        ColorRGBA[] colors = new ColorRGBA[cPoints.length];
        for(int i = 0; i < colors.length; i++) {
            colors[i] = cPoints[i].COLOR;
        }
        return colors;
    }
    
    public static float[] extractSizes(CloudPoint[] cPoints) {
        float[] sizes = new float[cPoints.length];
        for(int i = 0; i < sizes.length; i++){
            sizes[i] = cPoints[i].SIZE;
        }
        return sizes;
    }
    
    public CloudPoint copy() {
        return new CloudPoint(POINT.clone(), COLOR.clone(), SIZE);
    }
    
}
