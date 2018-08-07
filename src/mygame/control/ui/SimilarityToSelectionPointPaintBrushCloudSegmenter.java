/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.control.ui;

import com.jme3.math.Vector3f;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.data.search.NearestNeighborSearcher;
import mygame.graph.Graph;
import mygame.graph.GraphEdge;

/*
allows the user to paint so long as the points to be painted are similar enough to the points first clicked on
*/
public class SimilarityToSelectionPointPaintBrushCloudSegmenter extends SphericalPaintBrushPointCloudSegmenter {
    
    public SimilarityToSelectionPointPaintBrushCloudSegmenter(Vector3f[] X, NearestNeighborSearcher kdTree, SegmenterController toolInput) {
        super(X, kdTree, toolInput);
    }
    
    /*
    For SimilarityToSelectionPointPaintBrushCloudSegmenter to work as intended, a FullGraph is likely required 
    (since all connections to centerId are necessary).
    */
    @Override
    protected Set<Integer> getAllWithinRadius(Graph simGraph, int centerId, double radius) {
        Set<Integer> withinRadius = super.getAllWithinRadius(simGraph, centerId, radius);
        int nearestSelectNeighborId = segmenterController.getNearestScreenNeighborId(segmenterController.getSelectPos());
        if(nearestSelectNeighborId < 0) return new HashSet<Integer>();
        
        List<GraphEdge> centerEdges = simGraph.getOutEdges(nearestSelectNeighborId);
        HashSet<Integer> out = new HashSet<Integer>();
        for(GraphEdge centerEdge : centerEdges) {
            if(centerEdge.WEIGHT >= segmenterController.getTolerance() && withinRadius.contains(centerEdge.CHILD_ID)) {
                out.add(centerEdge.CHILD_ID);
            }
        }
        return out;
    }
}
