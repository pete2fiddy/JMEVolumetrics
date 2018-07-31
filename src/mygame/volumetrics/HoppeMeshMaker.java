package mygame.volumetrics;

import mygame.data.search.KDTree;
import org.jblas.DoubleMatrix;

public class HoppeMeshMaker implements ScalarField<DoubleMatrix> {
    private DoubleMatrix X, orientedNormals;
    private KDTree kdTree;
    
    public HoppeMeshMaker(DoubleMatrix X, DoubleMatrix orientedNormals, KDTree kdTree) {
        this.X = X;
        this.orientedNormals = orientedNormals;
        this.kdTree = kdTree;
    }

    @Override
    public double scalarValue(DoubleMatrix p) {
        int closestPointId = kdTree.getNearestNeighborId(p.toArray());
        return p.sub(X.getRow(closestPointId)).dot(orientedNormals.getRow(closestPointId));
    }
}
