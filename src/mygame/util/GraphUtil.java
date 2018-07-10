/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.util;

import mygame.ml.SimilarityMetric;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class GraphUtil {
    
    public static <DataType> DoubleMatrix constructSimilarityGraph(DataType[] X, SimilarityMetric<DataType> metric) {
        double[][] mat = new double[X.length][X.length];
        for(int i = 0; i < X.length; i++) {
            //doesn't need to iterate all the way given similarity graphs are symettric by definition
            for(int j = 0; j <= i; j++) {
                mat[i][j] = metric.similarityBetween(X[i], X[j]);
                mat[j][i] = mat[i][j];
            }
        }
        return new DoubleMatrix(mat);
    }
}
