package mygame.pointcloud;

import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import mygame.data.search.KDTree;
import mygame.data.search.PointSelectBFSNearestNeighborSearch;
import mygame.graph.Graph;
import mygame.graph.OnTheFlySimilarityGraph;
import mygame.input.VolumetricToolInput;
import mygame.ml.Segmenter;
import mygame.ml.similarity.jme.JMECosAngleSquaredSimilarity;
import mygame.ui.SimilarityToSelectionPointPaintBrushCloudSegmenter;
import mygame.util.JblasJMEConverter;
import mygame.volumetrics.CloudNormal;
import org.jblas.DoubleMatrix;

/*
A wrapper for a point cloud that allows it to be maniuplated by a controller for UI tasks, such as point selection, etc.

Interfaces with the functionality of VolumetricToolInput
*/
public class InteractivePointCloudController extends PointCloudController {
    private static final float POINT_SELECT_SIZE_MULTIPLIER = 3f;
    
    private VolumetricToolInput toolInput;
    
    private Camera cam;
    private PointSelectBFSNearestNeighborSearch nnSelectSearch;
    private KDTree kdTree;
    private Segmenter pointSegmenter;
    private Graph activeSimGraph;
    private HashMap<Integer, CloudPoint> selectedPoints = new HashMap<Integer, CloudPoint>();
    
    public InteractivePointCloudController(PointCloud pointCloud, Camera cam, InputManager inputManager) {
        super(pointCloud);
        initParams(cam, inputManager);
    }
    
    
    private void initParams(Camera cam, InputManager inputManager) {
        this.cam = cam;
        this.nnSelectSearch = new PointSelectBFSNearestNeighborSearch(cam, CloudPoint.extractPoints(pointCloud.points));
        this.kdTree = new KDTree(JblasJMEConverter.toArr(CloudPoint.extractPoints(pointCloud.points)));
        this.toolInput = new VolumetricToolInput(inputManager);
        //this.pointSegmenter = new SphericalPaintBrushPointCloudSegmenter(this, CloudPoint.extractPoints(points), kdTree, toolInput);
        //this.pointSegmenter = new SimilarityThresholdedFloodfillCloudSegmenter(this, toolInput);
        //this.pointSegmenter = new SimilarityToSelectionPointCloudSegmenter(this, toolInput);
        //this.pointSegmenter = new SinglePointCloudSegmenter(this, toolInput);
        this.pointSegmenter = new SimilarityToSelectionPointPaintBrushCloudSegmenter(this, CloudPoint.extractPoints(pointCloud.points), kdTree, toolInput);
        
        
        
        Vector3f[] extractedPoints = CloudPoint.extractPoints(pointCloud.points);
        DoubleMatrix pointsMat = JblasJMEConverter.toDoubleMatrix(extractedPoints);
        
        //CentroidClusterer<Vector3f> centroidClusterer = new JMEKMeansClusterer(5000, 5);
        //Vector3f[] centroids = centroidClusterer.getClusterCentroids(CloudPoint.extractPoints(points));
        
        
        activeSimGraph = new OnTheFlySimilarityGraph(JblasJMEConverter.toVector3f(CloudNormal.getUnorientedPCANormals(pointsMat, kdTree, 10)), new JMECosAngleSquaredSimilarity());
        
        
        
        /*activeSimGraph = GraphUtil.constructSparseSimilarityGraph(CloudPoint.extractPoints(points), new JMERadialBasisSimilarity(),
                kdTree, 5);*/
        
        
        this.nnSelectSearch.setDoUpdate(true);
        Thread t = new Thread(this.nnSelectSearch);
        //with JME, threads must be daemon or else they will not close when application is closed
        t.setDaemon(true);
        t.start();
    }
    
    
    public int getNearestScreenNeighborId(Vector2f point) {
        return nnSelectSearch.getNearestNeighborId(new Vector3f(point.getX(), point.getY(), cam.getFrustumNear()), 10);
    }
    
    private void updateBFSCamAndTransform() {
        nnSelectSearch.setTransform(pointCloud.getCloudNode().getWorldTransform().toTransformMatrix());
    }
    
    
    @Override
    protected void updatePointBuffer() {
        super.updatePointBuffer();
        //probably a smarter way is to only edit the connections to/from points that were edited, change if needed
        //update the active sim graph here.
    }
    
    @Override        
    public void update(float timePerFrame) {
        super.update(timePerFrame);
        updateBFSCamAndTransform();
        toolInput.update(timePerFrame);
        selectAndUnselectPoints();
        //centroidCloud.update(timePerFrame);
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
