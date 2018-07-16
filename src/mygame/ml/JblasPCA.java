package mygame.ml;

import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

public class JblasPCA {
    
    /*
    output[0] is a matrix with principal components as columns (smallest to largest eigenvalue order),
    output[1] is a Diagonal matrix with eigenvalues for the corresponding principal components. Both are sorted by eigenvalue, 
    smallest to largest.
    */
    public static DoubleMatrix[] getPrincipalComponents(DoubleMatrix X) {
        DoubleMatrix meanPoint = X.columnMeans();
        DoubleMatrix covarMat = DoubleMatrix.zeros(X.columns, X.columns);//X.mmul(X.transpose()).div(X.rows);
        for(int i = 0; i < X.rows; i++) {
            DoubleMatrix meanSubtractedPoint = X.getRow(i).sub(meanPoint);
            covarMat.addi(meanSubtractedPoint.transpose().mmul(meanSubtractedPoint));
        }
        covarMat = covarMat.div(X.rows);
        return Eigen.symmetricEigenvectors(covarMat);
    }
}
