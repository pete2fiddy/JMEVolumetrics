/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ml;

import com.jme3.math.Vector3f;

/**
 *
 * @author Owner
 */
public class JMERadialBasisSimilarity implements SimilarityMetric<Vector3f> {

    @Override
    public double similarityBetween(Vector3f v1, Vector3f v2) {
        return Math.exp(-v1.distanceSquared(v2));
    }
    
}
