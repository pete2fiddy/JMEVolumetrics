package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import mygame.input.VolumetricToolInput;
import mygame.ml.CentroidClusterer;
import mygame.ml.Segmenter;
import mygame.ml.SimilarityMetric;
import mygame.ui.SimilarityThresholdedFloodfillCloudSegmenter;
import mygame.ui.SinglePointCloudSegmenter;
import mygame.ui.SphericalPaintBrushPointCloudSegmenter;
import mygame.util.GraphUtil;
import org.jblas.DoubleMatrix;


public class InteractivePointCloud extends PointCloud {

    private CentroidClusterer<Vector3f> centroidClusterer;
    private SimilarityMetric<Vector3f> simMetric;
    private Vector3f[] centroids;
    private Map<Integer, Integer> idToClusterMap;
    private DoubleMatrix simGraph;
    private final PointSelectBFSNearestNeighborSearch nnSearch;
    private VolumetricToolInput toolInput;
    private Segmenter pointSegmenter;
    private HashMap<Integer, SelectedPoint> selectedPoints = new HashMap<Integer, SelectedPoint>();
    
    
    /*
    BUG: random point (likely the first or something) gets selected during SinglePointCloudSegmenter???
    */
    public InteractivePointCloud(AssetManager assetManager, Camera cam, Vector3f[] points, ColorRGBA[] colors, float[] sizes,
            InputManager inputManager, CentroidClusterer<Vector3f> centroidClustererIn, SimilarityMetric<Vector3f> similarityMetricIn) {
        super(assetManager, cam, points, colors, sizes);
        this.nnSearch = new PointSelectBFSNearestNeighborSearch(cam, points);
        this.toolInput = new VolumetricToolInput(inputManager);
        this.setClusteringMethod(centroidClustererIn, similarityMetricIn);
        this.pointSegmenter = new SphericalPaintBrushPointCloudSegmenter(this, getPoints(), centroids, idToClusterMap, toolInput);//new SimilarityThresholdedFloodfillCloudSegmenter(this, toolInput, idToClusterMap);//new SinglePointCloudSegmenter(this, toolInput);
    }
    
    public InteractivePointCloud(AssetManager assetManager, Camera cam, Vector3f[] points, ColorRGBA color, float size,
            InputManager inputManager, CentroidClusterer<Vector3f> centroidClustererIn, SimilarityMetric<Vector3f> similarityMetricIn) {
        super(assetManager, cam, points, color, size);
        this.nnSearch = new PointSelectBFSNearestNeighborSearch(cam, points);
        this.toolInput = new VolumetricToolInput(inputManager);
        this.setClusteringMethod(centroidClustererIn, similarityMetricIn);
        this.pointSegmenter = new SphericalPaintBrushPointCloudSegmenter(this, getPoints(), centroids, idToClusterMap, toolInput);//new SimilarityThresholdedFloodfillCloudSegmenter(this, toolInput, idToClusterMap);//new SinglePointCloudSegmenter(this, toolInput);
    }
    
    public void enableNNSearchThread(boolean b) {
        this.nnSearch.setDoUpdate(b);
        if(b) {
            Thread t = new Thread(this.nnSearch);
            //with JME, threads must be daemon or else they will not close when application is closed
            t.setDaemon(true);
            t.start();
        }
    }
    
    
    public int getNearestScreenNeighborId(Vector2f point) {
        return nnSearch.getNearestNeighborId(new Vector3f(point.getX(), point.getY(), getCam().getFrustumNear()), 10);
    }
    
    
    private void updateBFSCamAndTransform() {
        nnSearch.setTransform(getCloudNode().getWorldTransform().toTransformMatrix());
    }
    
    
    public void setClusteringMethod(CentroidClusterer<Vector3f> centroidClusterer, SimilarityMetric<Vector3f> similarityMetric) {
        this.centroidClusterer = centroidClusterer;
        this.simMetric = similarityMetric;
        centroids = centroidClusterer.getClusterCentroids(getPoints());
        idToClusterMap = centroidClusterer.clusterIds(centroids, getPoints());
        simGraph = GraphUtil.constructSimilarityGraph(centroids, simMetric);
    }
    
    
    
    @Override        
    public void update(float timePerFrame) {
        super.update(timePerFrame);
        updateBFSCamAndTransform();
        toolInput.update(timePerFrame);
        selectAndUnselectPoints();
    }
    
    private void selectAndUnselectPoints() {
        Set<Integer> segmentIds = pointSegmenter.getSegmentedIds(simGraph);
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
            selectedPoints.put(id, new SelectedPoint(getPoint(id), getColor(id), getSize(id)));
            setColor(id, new ColorRGBA(1f, 1f, 1f, 1f));
            setSize(id, getSize(id)*10f);
            
        }
    }
    
    private void unselectPoint(int id) {
        SelectedPoint selectPoint = selectedPoints.get(id);
        setColor(id, selectPoint.COLOR);
        setSize(id, selectPoint.SIZE);
        selectedPoints.remove(id);
    }
    
    
    
    private class SelectedPoint {
        public final Vector3f POINT;
        public final ColorRGBA COLOR;
        public final float SIZE;
        public SelectedPoint(Vector3f point, ColorRGBA color, float size) {
            POINT = point;
            COLOR = color;
            SIZE = size;
        }
    }
}
