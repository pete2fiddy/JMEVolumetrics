package mygame.ml.similarity.jme;

import com.jme3.math.Vector3f;
import mygame.ml.similarity.SimilarityMetric;
import org.jblas.DoubleMatrix;


public class JMECosAngleSquaredSimilarity implements SimilarityMetric<Vector3f> {
    @Override
    public double similarityBetween(Vector3f v1, Vector3f v2) {
        double dot = v1.dot(v2);
        return dot*dot/(v1.lengthSquared() * v2.lengthSquared());
    }
    
}
