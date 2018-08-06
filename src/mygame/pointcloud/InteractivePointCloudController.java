package mygame.pointcloud;

import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import mygame.data.search.KDTree;
import mygame.data.search.NearestNeighborSearcher;
import mygame.data.search.PointSelectBFSNearestNeighborSearch;
import mygame.graph.Graph;
import mygame.graph.OnTheFlySimilarityGraph;
import mygame.graph.SparseGraph;
import mygame.input.KeyboardSegmenterToolController;
import mygame.input.SegmenterToolControllerImpl;
import mygame.ml.CurvatureSimilarityGraphConstructor;
import mygame.ml.Segmenter;
import mygame.ml.similarity.SimilarityMetric;
import mygame.ml.similarity.jme.JMECosAngleSquaredSimilarity;
import mygame.ml.similarity.jme.JMERadialBasisSimilarity;
import mygame.ui.SegmenterToolControllerInterface;
import mygame.ui.SelectionDisabledSegmenter;
import mygame.ui.SimilarityThresholdedFloodfillCloudSegmenter;
import mygame.ui.SimilarityToSelectionPointCloudSegmenter;
import mygame.ui.SimilarityToSelectionPointPaintBrushCloudSegmenter;
import mygame.ui.SinglePointCloudSegmenter;
import mygame.ui.SphericalPaintBrushPointCloudSegmenter;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import mygame.volumetrics.CloudNormal;
import org.jblas.DoubleMatrix;

/*
A wrapper for a point cloud that allows it to be maniuplated by a controller for UI tasks, such as point selection, etc.

Interfaces with the functionality of KeyboardSegmenterToolController
*/
public class InteractivePointCloudController extends PointCloudController {
    
    public final Segmenter PAINTBRUSH, SIMILARITY_THRESHOLDED_FLOODFILL, SIMILARITY_TO_SELECTION, SINGLE_POINT, SIMILARITY_TO_SELECTION_CONSTRAINED_PAINTBRUSH;
    public final Graph NEAREST_NEIGHBOR_SPARSE_ANGLE, NEAREST_NEIGHBOR_SPARSE_DISTANCE;
    public final OnTheFlySimilarityGraph ON_THE_FLY_ANGLE;
    private static final float POINT_SELECT_SIZE_MULTIPLIER = 3f;
    private static float selectionRadius = .5f;
    private SegmenterToolControllerInterface toolInput;
    private Set<Integer> updatedPoints = new HashSet<Integer>();
    private Camera cam;
    private PointSelectBFSNearestNeighborSearch nnSelectSearch;
    private NearestNeighborSearcher kdTree;
    private Segmenter pointSegmenter;
    private Graph activeSimGraph;
    private HashMap<Integer, CloudPoint> selectedPoints = new HashMap<Integer, CloudPoint>();
    
    public InteractivePointCloudController(PointCloud pointCloud, Camera cam, InputManager inputManager) {
        super(pointCloud);
        initParams(cam, inputManager);
        
        
        
        Vector3f[] extractedPoints = CloudPoint.extractPoints(pointCloud.points);
        PAINTBRUSH = new SphericalPaintBrushPointCloudSegmenter(this, extractedPoints, kdTree, toolInput);
        SIMILARITY_THRESHOLDED_FLOODFILL = new SimilarityThresholdedFloodfillCloudSegmenter(this, toolInput);
        SIMILARITY_TO_SELECTION = new SimilarityToSelectionPointCloudSegmenter(this, toolInput);
        SINGLE_POINT = new SinglePointCloudSegmenter(this, toolInput);
        SIMILARITY_TO_SELECTION_CONSTRAINED_PAINTBRUSH = new SimilarityToSelectionPointPaintBrushCloudSegmenter(this, extractedPoints, kdTree, toolInput);
        
        
        
        //set to null since points will be updated in updateGraphs() anyway
        ON_THE_FLY_ANGLE = new OnTheFlySimilarityGraph(null, new JMECosAngleSquaredSimilarity());
        NEAREST_NEIGHBOR_SPARSE_ANGLE = new SparseGraph(pointCloud.points.length);
        NEAREST_NEIGHBOR_SPARSE_DISTANCE = new SparseGraph(pointCloud.points.length);
        
        
        updateGraphs();
        
        this.pointSegmenter = new SelectionDisabledSegmenter();
    }
    
    private void updateGraphs() {
        // a VERY slow way to update graphs when point added, but kind of necessary since if there are sparsity constraints onthe graph, there is no guarantee a moved point
        //will have the same neighbors it had before when the sparsity graph was constructed
        //could still be sped up by only altering the affectd row and column of the graph, but not a high priority currently. Code mostly here just so that
        //confusing bugs don't occur if points moved.
        Vector3f[] extractedPoints = CloudPoint.extractPoints(pointCloud.points);
        DoubleMatrix pointsMat = JblasJMEConverter.toDoubleMatrix(extractedPoints);
        DoubleMatrix normals = CloudNormal.getUnorientedPCANormals(pointsMat, kdTree, 10);
        ON_THE_FLY_ANGLE.setData(JblasJMEConverter.toVector3f(normals));
        GraphUtil.eraseAndCloneInto(CurvatureSimilarityGraphConstructor.constructRiemannianPCASimilarityGraph(pointsMat, normals, kdTree, 10), NEAREST_NEIGHBOR_SPARSE_ANGLE);
        GraphUtil.eraseAndCloneInto(GraphUtil.constructSparseSimilarityGraph(extractedPoints, new JMERadialBasisSimilarity(), kdTree, 5), NEAREST_NEIGHBOR_SPARSE_DISTANCE);
    }
    
    
    private void initParams(Camera cam, InputManager inputManager) {
        this.cam = cam;
        this.nnSelectSearch = new PointSelectBFSNearestNeighborSearch(cam, CloudPoint.extractPoints(pointCloud.points));
        this.kdTree = new KDTree(JblasJMEConverter.toArr(CloudPoint.extractPoints(pointCloud.points)));
        this.toolInput = new SegmenterToolControllerImpl(inputManager);
        
        this.nnSelectSearch.setDoUpdate(true);
        Thread t = new Thread(this.nnSelectSearch);
        //with JME, threads must be daemon or else they will not close when application is closed
        t.setDaemon(true);
        t.start();
    }
    
    public void setSegmenter(Segmenter s) {
        this.pointSegmenter = s;
    }
    
    public void setSimGraph(Graph g) {
        this.activeSimGraph = g;
    }
    
    public int getNearestScreenNeighborId(Vector2f point) {
        return nnSelectSearch.getNearestNeighborId(new Vector3f(point.getX(), point.getY(), cam.getFrustumNear()), 10);
    }
    
    private void updateBFSCamAndTransform() {
        nnSelectSearch.setTransform(pointCloud.getCloudNode().getWorldTransform().toTransformMatrix());
    }
    
    
    public void setPoint(int index, CloudPoint newPoint) {
        if(!newPoint.COLOR.equals(pointCloud.getPoint(index).COLOR)) updatedPoints.add(index);
        super.setPoint(index, newPoint);
    }
    
    @Override
    protected void updatePoints() {
        super.updatePoints();
        updateGraphs();
    }
    
    @Override        
    public void update(float timePerFrame) {
        super.update(timePerFrame);
        updateBFSCamAndTransform();
        selectAndUnselectPoints();
    }
    
    private void selectAndUnselectPoints() {
        Set<Integer> segmentIds = pointSegmenter.getSegmentedIds(activeSimGraph);
        unselectAllUnselectedPoints(segmentIds);
        for(int id : segmentIds) {
            selectPoint(id);
        }
    }
    
    private void unselectAllUnselectedPoints(Set<Integer> newSegmentIds){
        LinkedList<Integer> unselectedIds = new LinkedList<Integer>();
        for(int oldId : selectedPoints.keySet()) {
            if(!newSegmentIds.contains(oldId)) {
                unselectedIds.add(oldId);
            }
        }
        
        for(int unselectedId : unselectedIds) {
            unselectPoint(unselectedId);
        }
    }
    
    private void selectPoint(int id) {
        if(!selectedPoints.containsKey(id)){
            selectedPoints.put(id, pointCloud.points[id].copy());
            setPoint(id, new CloudPoint(pointCloud.points[id].POINT, new ColorRGBA(1f,1f,1f,1f), pointCloud.points[id].SIZE*POINT_SELECT_SIZE_MULTIPLIER));
        }
    }
    
    private void unselectPoint(int id) {
        CloudPoint unselectPoint = selectedPoints.remove(id);
        setPoint(id, unselectPoint);
    }
}
