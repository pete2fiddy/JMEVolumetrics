package mygame.model.data.ml.similarity.jme;

import com.jme3.math.Vector3f;
import mygame.model.data.ml.similarity.SimilarityMetric;

public class JMERadialBasisSimilarity implements SimilarityMetric<Vector3f> {
    private double variance = 1;
    
    public JMERadialBasisSimilarity() {}
    
    public JMERadialBasisSimilarity(double stdDev) {
        this.variance = stdDev*stdDev;
    }
    
    @Override
    public double similarityBetween(Vector3f v1, Vector3f v2) {
        return Math.exp(-v1.distanceSquared(v2)/variance);
    }
    
}
