/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.util;

import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class JblasJMEConverter {
    
    public static DoubleMatrix toDoubleMatrix(Vector3f... X) {
        double[][] doubleMatArr = new double[X.length][3];
        for(int i = 0; i < X.length; i++) {
            doubleMatArr[i] = new double[] {(double)X[i].x, (double)X[i].y, (double)X[i].z};
        }
        return new DoubleMatrix(doubleMatArr);
    }
    
    public static double[][] toArr(Vector3f... X) {
        double[][] out = new double[X.length][3];
        for(int i = 0; i < out.length; i++){
            out[i] = new double[] {(double)X[i].x, (double)X[i].y, (double)X[i].z};
        }
        return out;
    }
    
    
    public static Vector3f[] toVector3f(DoubleMatrix X) {
        if(X.columns == 1) {
            return new Vector3f[] {new Vector3f((float)X.get(0), (float)X.get(1), (float)X.get(2))};
        }
        Vector3f[] out = new Vector3f[X.rows];
        for(int i = 0; i < out.length; i++) {
            out[i] = new Vector3f((float)X.get(i,0),
            (float)X.get(i,1),
            (float)X.get(i,2));
        }
        return out;
    }
    
    public static DoubleMatrix toDoubleMatrix(Matrix3f mat) {
        DoubleMatrix out = DoubleMatrix.zeros(3, 3);
        for(int i = 0; i < out.rows; i++) {
            for(int j = 0; j < out.columns; j++) {
                out.put(i,j,mat.get(i,j));
            }
        }
        return out;
    }
}
