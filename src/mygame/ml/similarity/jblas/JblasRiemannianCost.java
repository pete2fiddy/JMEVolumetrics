package mygame.ml.similarity.jblas;

import mygame.ml.similarity.SimilarityMetric;
import org.jblas.DoubleMatrix;

public class JblasRiemannianCost implements SimilarityMetric<DoubleMatrix>{

    @Override
    public double similarityBetween(DoubleMatrix v1, DoubleMatrix v2) {
        return 1 - Math.abs(v1.dot(v2));
    }
    
}
