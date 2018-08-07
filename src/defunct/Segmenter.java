/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package defunct;

import java.util.List;
import java.util.Set;
import mygame.model.graph.Graph;
import mygame.model.graph.SparseGraph;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public interface Segmenter {
    /*
    Uses the params of the implementer of Segmenter to return the ids of the segmented data 
    */
    public Set<Integer> getSegmentedIds(Graph simGraph);
}
