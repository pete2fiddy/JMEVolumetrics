/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ml.similarity;

/**
 *
 * @author Owner
 */
public interface SimilarityMetric <D> {
    public double similarityBetween(D v1, D v2);
}
