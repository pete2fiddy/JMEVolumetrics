package mygame.volumetrics;

import java.util.HashSet;
import java.util.Set;
import mygame.util.MathUtil;
import org.jblas.DoubleMatrix;

public class Facet {
    private DoubleMatrix points;
    private DoubleMatrix normal;
    
    public Facet(DoubleMatrix points) {
        assert(points.rows == 3);
        this.points = points;
        this.normal = getNormal();
    }

    private DoubleMatrix getNormal() {
        //u is v0 to v1
        //v is v1 to v2
        DoubleMatrix u = points.getRow(1).sub(points.getRow(0));
        DoubleMatrix v = points.getRow(2).sub(points.getRow(1));
        DoubleMatrix cross = MathUtil.crossProd3d(u, v);
        return cross.div(cross.norm2());
    }
    

    public double signedDistanceToPoint(DoubleMatrix p) {
        return p.sub(points.getRow(0)).dot(normal);
    }
    
    
    public int getFarthestPoint(DoubleMatrix X) {
        int maxInd = 0;
        double maxDist = -Double.MAX_VALUE;
        for(int xInd = 0; xInd < X.rows; xInd++) {
            double indDist = signedDistanceToPoint(X.getRow(xInd));
            if(indDist > maxDist) {
                maxDist = indDist;
                maxInd = xInd;
            }
        }
        //returns -1 if no points are on the exterior of the facet
        return (maxDist > 0)? maxInd : -1;
    }
    
    public DoubleMatrix getPointClones(int... inds) {
        if(inds.length == 1) {
            DoubleMatrix out = DoubleMatrix.zeros(points.columns);
            return out.copy(points.getRow(inds[0]));
        }
        DoubleMatrix out = DoubleMatrix.zeros(inds.length, points.columns);
        return out.copy(points.getRows(inds));
    }
    
    public DoubleMatrix getNormalClone() {
        DoubleMatrix out = DoubleMatrix.zeros(normal.getLength());
        return out.copy(normal);
    }
    
    public int numPoints() {return points.rows;}
}
