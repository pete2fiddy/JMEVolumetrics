/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.volumetrics;

import java.util.Arrays;
import mygame.data.search.KDTree;
import mygame.ml.JblasPCA;
import mygame.util.JblasJMEConverter;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class CloudNormal {
    /*
    unoriented cloud normals calculated as follows:
    
    dataset of DoubleMatrix, X, fed in.
    for each point, the nearest 2 points are found (put X into a KDTree, don't worry about transforming it, since is not necessary).
    normal axis is found by cross product is found between the two found points subtracted by the original point
    
    or:
    
    dataset of DoubleMatrix, X, fed in
    for each point, the nearest N points (or all points within a radius) are found (put X into KDTree, don't worry about transforming it, since is not necessary).
    axis of miniminum variance is found by PCA on the near points
    
    
    use graph traversal + cosine angle between to flip neighboring normals (flip in if cosine angle is negative -- if magnitude of cosine
    angle is small, means it is unknown if is good flip)
    
    possibly perform process multiple times and choose the result that is most consistent across all attempts
    */
    
    
    public static DoubleMatrix getUnorientedPCANormals(DoubleMatrix X, KDTree kdTree, int nNeighbors) {
        
        DoubleMatrix normals = DoubleMatrix.zeros(X.rows, X.columns);
        for(int i = 0; i < X.rows; i++) {
            int[] nearestNeighborIds = kdTree.getNearestNeighborIds(X.getRow(i).toArray(), nNeighbors);
            DoubleMatrix neighborVecs = X.getRows(nearestNeighborIds);
            normals.putRow(i, JblasPCA.getPrincipalComponents(neighborVecs)[0].getColumn(0));
        }
        return normals;
    }
}
