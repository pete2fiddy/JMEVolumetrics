package mygame.ml;

import com.jme3.math.Vector3f;
import org.jblas.DoubleMatrix;


public class JblasCosineAngleSquaredSimilarityMetric implements SimilarityMetric<DoubleMatrix> {

    @Override
    public double similarityBetween(DoubleMatrix v1, DoubleMatrix v2) {
        double cosAngle = v1.dot(v2)/(v1.norm2() * v2.norm2());
        return cosAngle*cosAngle;
    }
    
}
