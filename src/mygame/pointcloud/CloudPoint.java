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
    protected float size;
    protected ColorRGBA color;
    protected Vector3f point;
    
    public CloudPoint(Vector3f point, ColorRGBA color, float size) {
        this.point = point;
        this.size = size;
        this.color = color;
    }
    
    public static Vector3f[] extractPoints(CloudPoint[] cPoints) {
        Vector3f[] points = new Vector3f[cPoints.length];
        for(int i = 0; i < points.length; i++){
            points[i] = cPoints[i].point;
        }
        return points.clone();
    }
    
    public static ColorRGBA[] extractColors(CloudPoint[] cPoints) {
        ColorRGBA[] colors = new ColorRGBA[cPoints.length];
        for(int i = 0; i < colors.length; i++) {
            colors[i] = cPoints[i].color;
        }
        return colors;
    }
    
    public static float[] extractSizes(CloudPoint[] cPoints) {
        float[] sizes = new float[cPoints.length];
        for(int i = 0; i < sizes.length; i++){
            sizes[i] = cPoints[i].size;
        }
        return sizes;
    }
    
    public CloudPoint copy() {
        return new CloudPoint(point.clone(), color.clone(), size);
    }
    
}
