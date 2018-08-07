/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.model.graph;

import java.util.ArrayList;
import java.util.List;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class FullGraph implements Graph {
    //uses DoubleMatrix instead of double[][] for ease of multiplying and performing matrix operations on the graph, should that
    //be implemented in the future;
    private DoubleMatrix graph;
    
    public FullGraph(int nNodes) {
        this.graph = DoubleMatrix.zeros(nNodes, nNodes);
    }

    @Override
    public void link(int id1, int id2, double weight) {
        graph.put(id1, id2, weight);
    }

    @Override
    public List<GraphEdge> getOutEdges(int id) {
        //not sure how slow instantiating GraphEdges like this is
        ArrayList<GraphEdge> out = new ArrayList<GraphEdge>();
        for(int i = 0; i < graph.columns; i++) {
            if(graph.get(id, i) == 0) {
                //assumes 0-weight edges are unconnected
                out.add(new GraphEdge(i, graph.get(id, i)));
            }
        }
        return out;
    }

    @Override
    public int numNodes() {
        return graph.rows;
    }

    @Override
    public void unlink(int id1, int id2) {
        graph.put(id1, id2, 0);
    }
    
}
