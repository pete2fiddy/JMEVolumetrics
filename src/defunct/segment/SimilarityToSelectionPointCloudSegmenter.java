package defunct.segment;

import defunct.Segmenter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import defunct.SegmenterController;
import mygame.model.graph.Graph;
import mygame.model.graph.GraphEdge;

public class SimilarityToSelectionPointCloudSegmenter implements Segmenter {
    private SegmenterController toolController;
    
    private Set<Integer> currentSelectionIds = new HashSet<Integer>();
    private Set<Integer> segmentIds = new HashSet<Integer>();
    
    //should scale the sensitivity by the amount of zoom (zoom out with same mouse delta should have more tolerance than
    //zoomed in with same mouse delta)
    //has no convenient way to deal with the different ranges/bounds of different similarity metrics when thresholding
    public SimilarityToSelectionPointCloudSegmenter(SegmenterController toolController) {
        this.toolController = toolController;
    }
    
    
    private double getSimilarityThreshold() {
        double selectDeltaMag = toolController.getCursorPos().distance(toolController.getSelectPos());
        return 1.0/((selectDeltaMag) * toolController.getTolerance() + 1);
    }
    
    
    /*
    simGraph likely needs to be full for SimilarityToSelectionPointCloudSegmenter to operate as intended.
    */
    @Override
    public Set<Integer> getSegmentedIds(Graph simGraph) {
        
        //1) find the nearest centroid to click
        //2) find its normal
        //3) add all clusters with cosine angle under threshold
        //4) let n be the id of the nearest cluster,assert(simGraph instanceof FullGraph) : "";
        
        if(toolController.selectActive()) {
            int nearestNeighborId = toolController.getNearestScreenNeighborId(toolController.getSelectPos());
            if(nearestNeighborId < 0) return segmentIds;
            
            segmentIds.removeAll(currentSelectionIds);
            currentSelectionIds = new HashSet<Integer>();
            double minSim = getSimilarityThreshold();
            
            List<GraphEdge> nearestNeighborEdges = simGraph.getOutEdges(nearestNeighborId);
            
            for(GraphEdge nearestNeighborEdge : nearestNeighborEdges) {
                if(nearestNeighborEdge.WEIGHT > minSim) currentSelectionIds.add(nearestNeighborEdge.CHILD_ID);
            }
            segmentIds.addAll(currentSelectionIds);

        } else if(toolController.clearActive()) {
            segmentIds = new HashSet<Integer>();
            currentSelectionIds = new HashSet<Integer>();
        } else {
            segmentIds.addAll(currentSelectionIds);
            currentSelectionIds = new HashSet<Integer>();
        }
        return segmentIds;
        
    }
}
