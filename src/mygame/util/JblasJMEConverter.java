/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.util;

import com.jme3.math.Vector3f;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class JblasJMEConverter {
    
    public static DoubleMatrix toDoubleMatrix(Vector3f[] X) {
        double[][] doubleMatArr = new double[X.length][3];
        for(int i = 0; i < X.length; i++) {
            doubleMatArr[i] = new double[] {(double)X[i].x, (double)X[i].y, (double)X[i].z};
        }
        return new DoubleMatrix(doubleMatArr);
    }
    
    public static Vector3f[] toVector3f(DoubleMatrix X) {
        Vector3f[] out = new Vector3f[X.rows];
        for(int i = 0; i < out.length; i++) {
            out[i] = new Vector3f((float)X.get(i,0),
            (float)X.get(i,1),
            (float)X.get(i,2));
        }
        return out;
    }
}
