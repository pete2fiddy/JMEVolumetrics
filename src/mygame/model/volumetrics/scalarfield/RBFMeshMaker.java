package mygame.model.volumetrics.scalarfield;

import mygame.model.data.ml.similarity.SimilarityMetric;
import mygame.util.PointUtil;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;

//see: https://arxiv.org/pdf/1305.5179.pdf
public class RBFMeshMaker implements ScalarField <DoubleMatrix> {
    private static final double MARGIN_VALUE = 1;
    private static final double MARGIN_WIDTH_BBOX_MULTIPLIER = 0.01;
    public static final double ISO_VALUE = 0;
    private final double MARGIN_WIDTH;
    private DoubleMatrix points, normals, constants;
    private SimilarityMetric<DoubleMatrix> rbfFunc;
    
    public RBFMeshMaker(DoubleMatrix points, DoubleMatrix normals, SimilarityMetric<DoubleMatrix> rbfFunc) {
        this.points = points;
        this.normals = normals;
        this.rbfFunc = rbfFunc;
        this.MARGIN_WIDTH = calcMarginWidth();
    }
    
    private double calcMarginWidth() {
        double[][] bbox = PointUtil.getPointBounds3d(points);
        double biggestBound = -Double.MAX_VALUE;
        for(double[] bounds : bbox) {
            double bound = Math.abs(bounds[1] - bounds[0]);
            if(bound > biggestBound) {
                biggestBound = bound;
            }
        }
        return MARGIN_WIDTH_BBOX_MULTIPLIER*biggestBound;
    }
    
    public void fit() {
        DoubleMatrix mat = calcDesignMatrix();
        DoubleMatrix target = calcTargetVector();
        constants = Solve.solveLeastSquares(mat, target);
        
        DoubleMatrix result = mat.mmul(constants);
        for(int i = 0; i < result.rows; i++) {
            double expectedVal = ISO_VALUE-MARGIN_VALUE;
            if(i >= points.rows && i < points.rows*2) {
                expectedVal = ISO_VALUE;
            } else if(i >= points.rows*2) {
                expectedVal = ISO_VALUE*MARGIN_VALUE;
            }
            //System.out.println("expected: " + expectedVal +", actual: " + result.get(i));
        }
    }
    
    private DoubleMatrix calcDesignMatrix() {
        DoubleMatrix out = DoubleMatrix.zeros(points.rows*3, points.rows);
        for(int i = 0; i < out.rows; i++) {
            double normalMult = -MARGIN_WIDTH;
            if(i >= points.rows && i < points.rows*2) {
                normalMult = 0;
            } else if(i >= points.rows*2) {
                normalMult = MARGIN_WIDTH;
            }
            for(int j = 0; j < points.rows; j++) {
                DoubleMatrix p1 = points.getRow(i%points.rows);
                p1 = p1.add(normals.getRow(i%normals.rows).mul(normalMult));
                DoubleMatrix p2 = points.getRow(j);
                out.put(i, j, rbfFunc.similarityBetween(p1, p2));
            }
        }
        return out;
    }
    
    private DoubleMatrix calcTargetVector() {
        DoubleMatrix out = DoubleMatrix.zeros(points.rows*3);
        for(int i = 0; i < out.rows; i++) {
            double val = ISO_VALUE-MARGIN_VALUE;
            if(i >= points.rows && i < points.rows*2) {
                val = ISO_VALUE;
            } else if(i >= points.rows*2) {
                val = ISO_VALUE+MARGIN_VALUE;
            }
            out.put(i, val);
        }
        return out;
    }
    
    @Override
    public double scalarValue(DoubleMatrix x) {
        if(constants == null) throw new UnsupportedOperationException("fit() not called on RBFMeshMaker before calculating scalar values!");
        double out = 0;
        for(int i = 0; i < points.rows; i++) {
            out += constants.get(i)*rbfFunc.similarityBetween(points.getRow(i), x);
        }
        return out;
    }
}
