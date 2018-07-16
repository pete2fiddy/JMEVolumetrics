/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.graph;

public class GraphEdge {
    public final int CHILD_ID;
    public final double WEIGHT;

    public GraphEdge(int childId, double weight) {
        this.CHILD_ID = childId;
        this.WEIGHT = weight;
    }
}