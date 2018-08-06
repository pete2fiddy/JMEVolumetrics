/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ui;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mygame.data.search.NearestNeighborSearcher;
import mygame.graph.Graph;
import mygame.graph.SparseGraph;
import mygame.input.KeyboardSegmenterToolController;
import mygame.ml.Segmenter;
import mygame.pointcloud.InteractivePointCloudController;
import mygame.util.JblasJMEConverter;
import mygame.util.SegmenterUtils;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class SphericalPaintBrushPointCloudSegmenter implements Segmenter {
    protected InteractivePointCloudController pointCloudController;
    protected SegmenterToolControllerInterface toolInput;
    protected Vector3f[] X;
    protected float brushRadius = 0.25f;
    Set<Integer>[] clusterSets;
    HashSet<Integer> segmentIds = new HashSet<Integer>();
    private NearestNeighborSearcher kdTree;
    
    public SphericalPaintBrushPointCloudSegmenter(InteractivePointCloudController pointCloudController, Vector3f[] X, NearestNeighborSearcher kdTree, 
            SegmenterToolControllerInterface toolInput) {
        this.X = X;
        this.pointCloudController = pointCloudController;
        this.toolInput = toolInput;
        this.kdTree = kdTree;
    }
    
    
    @Override
    public Set<Integer> getSegmentedIds(Graph simGraph) {
        if(toolInput.selectActive()) {
            int nearestNeighborId = pointCloudController.getNearestScreenNeighborId(toolInput.getCursorPos());
            if(nearestNeighborId >= 0) {
                Set<Integer> withinRadius = getAllWithinRadius(simGraph, nearestNeighborId, brushRadius);
                if (toolInput.eraseActive()) {
                    segmentIds.removeAll(withinRadius);
                } else {
                    segmentIds.addAll(withinRadius);
                }
            }
        }
        if(toolInput.clearActive()) {
            segmentIds = new HashSet<Integer>();
        }
        return (Set<Integer>)segmentIds.clone();
    }
    /*simGraph is passed for extensions to override how this method operates*/
    protected Set<Integer> getAllWithinRadius(Graph simGraph, int centerId, double radius) {
        return kdTree.getIdsWithinRadius(JblasJMEConverter.toArr(X[centerId])[0], radius);
    }
}
