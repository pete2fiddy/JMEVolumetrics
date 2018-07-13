package mygame.ui;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mygame.input.VolumetricToolInput;
import mygame.ml.Segmenter;
import mygame.ml.SimilarityMetric;
import mygame.pointcloud.InteractivePointCloud;
import mygame.util.SegmenterUtils;
import org.jblas.DoubleMatrix;


public class SimilarityToSelectionPointCloudSegmenter implements Segmenter {
    private InteractivePointCloud pointCloud;
    private VolumetricToolInput toolInput;
    private Map<Integer, Integer> idToClusterMap;
    private double similarityThresholdChangePerPixelWeight = 0.05;
    private Set<Integer>[] clusterSets;
    
    private Set<Integer> currentSelectionIds = new HashSet<Integer>();
    private Set<Integer> segmentIds = new HashSet<Integer>();
    
    //should scale the sensitivity by the amount of zoom (zoom out with same mouse delta should have more tolerance than
    //zoomed in with same mouse delta)
    //has no convenient way to deal with the different ranges/bounds of different similarity metrics when thresholding
    public SimilarityToSelectionPointCloudSegmenter(InteractivePointCloud pointCloud, VolumetricToolInput toolInput, Map<Integer, Integer> idToClusterMap) {
        this.pointCloud = pointCloud;
        this.toolInput = toolInput;
        this.idToClusterMap = idToClusterMap;
        this.clusterSets = SegmenterUtils.convertIntoClusterSets(idToClusterMap);
    }
    
    
    private double getSimilarityThreshold() {
        double selectDeltaMag = toolInput.getCursorPos().distance(toolInput.getSelectPos());
        return 1.0/((selectDeltaMag) * similarityThresholdChangePerPixelWeight + 1);
    }
    
    
    
    @Override
    public Set<Integer> getSegmentedIds(DoubleMatrix simMatrix) {
        
        //1) find the nearest centroid to click
        //2) find its normal
        //3) add all clusters with cosine angle under threshold
        //4) let n be the id of the nearest cluster, then basically only traverse along the nth row of simMatrix (connections from n to all others)
        
        
        if(toolInput.getIfDiscreteAction("SELECT_TOGGLE")) {
            int nearestNeighborId = pointCloud.getNearestScreenNeighborId(toolInput.getSelectPos());
            if(nearestNeighborId >= 0) {
                segmentIds.removeAll(currentSelectionIds);
                currentSelectionIds = new HashSet<Integer>();
                double minSim = getSimilarityThreshold();
                int nearestNeighborClusterId = idToClusterMap.get(nearestNeighborId);
                for(int compareClusterId = 0; compareClusterId < simMatrix.columns; compareClusterId++) {
                    if(simMatrix.get(nearestNeighborClusterId, compareClusterId) > minSim) currentSelectionIds.addAll(clusterSets[compareClusterId]);
                }
                segmentIds.addAll(currentSelectionIds);
            }
        } else if(toolInput.getIfDiscreteAction("CLEAR_TOGGLE")) {
            segmentIds = new HashSet<Integer>();
            currentSelectionIds = new HashSet<Integer>();
        }else {
            segmentIds.addAll(currentSelectionIds);
            currentSelectionIds = new HashSet<Integer>();
        }
        return segmentIds;
        
    }
}
