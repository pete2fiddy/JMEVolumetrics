package mygame.util;

import org.jblas.DoubleMatrix;

public class BoxUtil {
    public static boolean boxContains(double[][] bounds, DoubleMatrix p) {
        return boxContains(bounds, p.toArray());
    }
    
    public static boolean boxContains(double[][] bounds, double[] p) {
        if(p.length != bounds.length) throw new IllegalArgumentException();
        for(int i = 0; i < bounds.length; i++) {
            if(p[i] < bounds[i][0] || p[i] > bounds[i][1]) return false;
        }
        return true;
    }
}
