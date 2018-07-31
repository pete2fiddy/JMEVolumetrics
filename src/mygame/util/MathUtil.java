/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.util;

import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class MathUtil {
    public static DoubleMatrix crossProd3d(DoubleMatrix u, DoubleMatrix v) {
        return new DoubleMatrix(new double[][] {{u.get(1)*v.get(2) - u.get(2)*v.get(1),
        u.get(2)*v.get(0) - u.get(0)*v.get(2),
        u.get(0)*v.get(1) - u.get(1)*v.get(0)}});
    }
}
