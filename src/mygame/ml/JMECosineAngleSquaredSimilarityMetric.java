package mygame.ml;

import com.jme3.math.Vector3f;
import org.jblas.DoubleMatrix;


public class JMECosineAngleSquaredSimilarityMetric implements SimilarityMetric<Vector3f> {
    @Override
    public double similarityBetween(Vector3f v1, Vector3f v2) {
        double dot = v1.dot(v2);
        return dot*dot/(v1.lengthSquared() * v2.lengthSquared());
    }
    
}
