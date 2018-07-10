package mygame.util;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Owner
 */
public class MyBufferUtil {
    
    public static FloatBuffer createColorBuffer(ColorRGBA color, int nColors){
        ColorRGBA[] colors = new ColorRGBA[nColors];
        for(int i = 0; i < colors.length; i++){
            colors[i] = color;
        }
        return createColorBuffer(colors);
    }
    
    public static FloatBuffer createColorBuffer(ColorRGBA[] colors) {
        FloatBuffer out = BufferUtils.createFloatBuffer(colors.length*4);
        for(ColorRGBA color : colors) {
            out.put(color.r).put(color.g).put(color.b).put(color.a);
        }
        return out;
    }
    
    public static FloatBuffer createFloatBuffer(float[] floats) {
        FloatBuffer out = BufferUtils.createFloatBuffer(floats.length);
        for(float f : floats){
            out.put(f);
        }
        return out;
    }
    
    public static FloatBuffer createPointsBuffer(Vector3f[] points) {
        FloatBuffer out = BufferUtils.createFloatBuffer(points.length*3);
        for(Vector3f point : points) {
            out.put(point.x).put(point.y).put(point.z);
        }
        return out;
    }
}
