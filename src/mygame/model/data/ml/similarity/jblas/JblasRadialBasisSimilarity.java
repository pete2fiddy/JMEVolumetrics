package mygame.model.data.ml.similarity.jblas;

import mygame.model.data.ml.similarity.SimilarityMetric;
import org.jblas.DoubleMatrix;

public class JblasRadialBasisSimilarity implements SimilarityMetric<DoubleMatrix> {
    private double variance = 1;
    
    public JblasRadialBasisSimilarity(double stdDev) {
        this.variance = stdDev*stdDev;
    }
    
    public JblasRadialBasisSimilarity(){}

    @Override
    public double similarityBetween(DoubleMatrix v1, DoubleMatrix v2) {
        double numerator = v1.sub(v2).norm2();
        return Math.exp(-numerator*numerator/variance);
    }
}
