/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ui;

import com.jme3.math.Vector3f;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mygame.data.search.JblasKDTree;
import mygame.input.VolumetricToolInput;
import mygame.pointcloud.InteractivePointCloud;
import org.jblas.DoubleMatrix;

/*
allows the user to paint so long as the points to be painted are similar enough to the points first clicked on
*/
public class SimilarityToSelectionPointPaintBrushCloudSegmenter extends SphericalPaintBrushPointCloudSegmenter {
    private double tolerance = .6;
    private Map<Integer, Integer> idToClusterMap;
    
    public SimilarityToSelectionPointPaintBrushCloudSegmenter(InteractivePointCloud pointCloud, Vector3f[] X, JblasKDTree kdTree, VolumetricToolInput toolInput, Map<Integer, Integer> idToClusterMap) {
        super(pointCloud, X, kdTree, toolInput);
        this.idToClusterMap = idToClusterMap;
    }
    
    
    
    @Override
    protected Set<Integer> getAllWithinRadius(DoubleMatrix simMatrix, int centerId, double radius) {
        Set<Integer> out = super.getAllWithinRadius(simMatrix, centerId, radius);
        int nearestSelectPosNeighbor = pointCloud.getNearestScreenNeighborId(toolInput.getSelectPos());
        if(nearestSelectPosNeighbor < 0) return new HashSet<Integer>();
        int startSelectCluster = idToClusterMap.get(nearestSelectPosNeighbor);
        Set<Integer> removeIds = new HashSet<Integer>();
        for(int id : out) {
            if(simMatrix.get(startSelectCluster, idToClusterMap.get(id)) < tolerance) removeIds.add(id);
        }
        out.removeAll(removeIds);
        return out;
    }
}
