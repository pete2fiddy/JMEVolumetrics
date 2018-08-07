/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.model.data.ml.similarity.jme;

import com.jme3.math.Vector3f;
import mygame.model.data.ml.similarity.SimilarityMetric;

/**
 *
 * @author Owner
 */
public class JMEInvEuclidianSimilarity implements SimilarityMetric<Vector3f> {

    @Override
    public double similarityBetween(Vector3f v1, Vector3f v2) {
        return 1.0/v1.distance(v2);
    }
    
}
