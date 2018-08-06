package mygame.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mygame.graph.FullGraph;
import mygame.graph.Graph;
import mygame.graph.GraphEdge;
import mygame.input.KeyboardSegmenterToolController;
import mygame.ml.Segmenter;
import mygame.ml.similarity.SimilarityMetric;
import mygame.pointcloud.InteractivePointCloudController;
import mygame.util.SegmenterUtils;
import org.jblas.DoubleMatrix;


public class SimilarityToSelectionPointCloudSegmenter implements Segmenter {
    private InteractivePointCloudController pointCloudController;
    private SegmenterToolControllerInterface toolController;
    private double similarityThresholdChangePerPixelWeight = 0.05;
    
    private Set<Integer> currentSelectionIds = new HashSet<Integer>();
    private Set<Integer> segmentIds = new HashSet<Integer>();
    
    //should scale the sensitivity by the amount of zoom (zoom out with same mouse delta should have more tolerance than
    //zoomed in with same mouse delta)
    //has no convenient way to deal with the different ranges/bounds of different similarity metrics when thresholding
    public SimilarityToSelectionPointCloudSegmenter(InteractivePointCloudController pointCloudController, SegmenterToolControllerInterface toolController) {
        this.pointCloudController = pointCloudController;
        this.toolController = toolController;
    }
    
    
    private double getSimilarityThreshold() {
        double selectDeltaMag = toolController.getCursorPos().distance(toolController.getSelectPos());
        return 1.0/((selectDeltaMag) * similarityThresholdChangePerPixelWeight + 1);
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
            int nearestNeighborId = pointCloudController.getNearestScreenNeighborId(toolController.getSelectPos());
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
