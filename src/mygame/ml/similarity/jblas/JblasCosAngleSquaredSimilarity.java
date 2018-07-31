package mygame.ml.similarity.jblas;

import mygame.ml.similarity.SimilarityMetric;
import org.jblas.DoubleMatrix;

public class JblasCosAngleSquaredSimilarity implements SimilarityMetric<DoubleMatrix> {
    @Override
    public double similarityBetween(DoubleMatrix v1, DoubleMatrix v2) {
        double dot = v1.dot(v2);
        return dot/(v1.norm2() * v2.norm2());
    }
    
}
