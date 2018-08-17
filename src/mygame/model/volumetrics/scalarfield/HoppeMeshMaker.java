package mygame.model.volumetrics.scalarfield;

import mygame.model.data.search.NearestNeighborSearcher;
import org.jblas.DoubleMatrix;

public class HoppeMeshMaker implements ScalarField<DoubleMatrix> {
    private DoubleMatrix X, orientedNormals;
    private NearestNeighborSearcher kdTree;
    
    public HoppeMeshMaker(DoubleMatrix X, DoubleMatrix orientedNormals, NearestNeighborSearcher kdTree) {
        this.X = X;
        this.orientedNormals = orientedNormals;
        this.kdTree = kdTree;
    }

    @Override
    public double scalarValue(DoubleMatrix p) {
        int closestPointId = kdTree.getNearestNeighborIds(p.toArray(), 1)[0];
        return p.sub(X.getRow(closestPointId)).dot(orientedNormals.getRow(closestPointId));
    }
}
