package mygame.pointcloud;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import mygame.PointSelectBFSNearestNeighborSearch;
import mygame.data.search.JblasKDTree;
import mygame.graph.Graph;
import mygame.input.VolumetricToolInput;
import mygame.ml.CurvatureSimilarityGraphConstructor;
import mygame.ml.JMEInvEuclidianSimilarity;
import mygame.ml.JMEKMeansClusterer;
import mygame.ml.JMERadialBasisSimilarity;
import mygame.ml.JblasCosineAngleSquaredSimilarityMetric;
import mygame.ml.Segmenter;
import mygame.ui.SimilarityToSelectionPointCloudSegmenter;
import mygame.ui.SimilarityThresholdedFloodfillCloudSegmenter;
import mygame.ui.SimilarityToSelectionPointPaintBrushCloudSegmenter;
import mygame.ui.SphericalPaintBrushPointCloudSegmenter;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import mygame.util.SegmenterUtils;
import org.jblas.DoubleMatrix;


public class InteractivePointCloud extends PointCloud {
    private float POINT_SELECT_SIZE_MULTIPLIER = 3f;
    
    
    
    private Map<Integer, Integer> idToClusterMap;
    private Graph activeSimGraph;
    private DoubleMatrix curvatureSimGraph;
    private PointSelectBFSNearestNeighborSearch nnSearch;
    private VolumetricToolInput toolInput;
    private Segmenter pointSegmenter;
    private HashMap<Integer, CloudPoint> selectedPoints = new HashMap<Integer, CloudPoint>();
    private JblasKDTree kdTree;
    
    
    
    
    private PointCloud centroidCloud;
    
    
    public InteractivePointCloud(AssetManager assetManager, Camera cam, CloudPoint[] points, InputManager inputManager) {
        super(assetManager, cam, points);
        initInteractiveParams(inputManager);
        
        //centroidCloud = new PointCloud(assetManager, cam, centroids, new ColorRGBA(0f,1f,0f,1f), 10f);
        //getCloudNode().attachChild(centroidCloud.getCloudNode());
    }
    
    public InteractivePointCloud(AssetManager assetManager, Camera cam, Vector3f[] points, ColorRGBA color, float size,
            InputManager inputManager) {
        super(assetManager, cam, points, color, size);
        initInteractiveParams(inputManager);
        
        //centroidCloud = new PointCloud(assetManager, cam, centroids, new ColorRGBA(0f,1f,0f,1f), 10f);
        //getCloudNode().attachChild(centroidCloud.getCloudNode());
    }
    
    
    private void initInteractiveParams(InputManager inputManager) {
        this.nnSearch = new PointSelectBFSNearestNeighborSearch(cam, CloudPoint.extractPoints(points));
        this.kdTree = new JblasKDTree(JblasJMEConverter.toDoubleMatrix(CloudPoint.extractPoints(points)));
        this.toolInput = new VolumetricToolInput(inputManager);
        //this.pointSegmenter = new SphericalPaintBrushPointCloudSegmenter(this, CloudPoint.extractPoints(points), kdTree, toolInput);
        this.pointSegmenter = new SimilarityThresholdedFloodfillCloudSegmenter(this, toolInput);
        //this.pointSegmenter = new SimilarityToSelectionPointCloudSegmenter(this, toolInput);
        //this.pointSegmenter = new SinglePointCloudSegmenter(this, toolInput);
        //this.pointSegmenter = new SimilarityToSelectionPointPaintBrushCloudSegmenter(this, CloudPoint.extractPoints(points), kdTree, toolInput);
        
        
        long startTime = System.nanoTime();
        /*activeSimGraph = CurvatureSimilarityGraphConstructor.constructPCASimilarityGraph(
        JblasJMEConverter.toDoubleMatrix(CloudPoint.extractPoints(points)),
                kdTree, new JblasCosineAngleSquaredSimilarityMetric(), 10);*/
        
        /*activeSimGraph = CurvatureSimilarityGraphConstructor.constructSparsePCASimilarityGraph(
        JblasJMEConverter.toDoubleMatrix(CloudPoint.extractPoints(points)), 
                new JblasCosineAngleSquaredSimilarityMetric(),
                10, kdTree, .5);*/
        
        activeSimGraph = CurvatureSimilarityGraphConstructor.constructSparsePCASimilarityGraph(JblasJMEConverter.toDoubleMatrix(CloudPoint.extractPoints(points)),
                new JblasCosineAngleSquaredSimilarityMetric(),
                5, kdTree, 15);
        System.out.println("sim graph construction time: " + Double.toString((double)((System.nanoTime()-startTime)/1000000.0)));
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
        return nnSearch.getNearestNeighborId(new Vector3f(point.getX(), point.getY(), cam.getFrustumNear()), 10);
    }
    
    private void updateBFSCamAndTransform() {
        nnSearch.setTransform(getCloudNode().getWorldTransform().toTransformMatrix());
    }
    
    
    public void initSimGraphs() {
        //probably doesn't make sense to completely recluster every time points are updated?
        JMEKMeansClusterer centroidClusterer = new JMEKMeansClusterer(1000, 10);
        Vector3f[] vecs = CloudPoint.extractPoints(points);
        /*proximitySimGraph = GraphUtil.constructSimilarityGraph(centroids, new JMERadialBasisSimilarity());
        
        
        distWeightedCurvatureSimGraph = CurvatureSimilarityGraphConstructor.constructDistanceWeightedPCASimilarityGraph(CloudPoint.extractPoints(points),
                centroids, SegmenterUtils.convertIntoClusterSets(idToClusterMap), new JblasCosineAngleSquaredSimilarityMetric(), new JMERadialBasisSimilarity());
        //distWeightedCurvatureSimGraph = distWeightedCurvatureSimGraph.mul(proximitySimGraph);
       
        curvatureSimGraph = CurvatureSimilarityGraphConstructor.constructPCASimilarityGraph(CloudPoint.extractPoints(points), 
                SegmenterUtils.convertIntoClusterSets(idToClusterMap), new JblasCosineAngleSquaredSimilarityMetric());*/
    }
    
    @Override
    protected void updatePointBuffer() {
        super.updatePointBuffer();
        //probably a smarter way is to only edit the connections to/from points that were edited, change if needed
        initSimGraphs();
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
            selectedPoints.put(id, points[id].copy());
            setPoint(id, new CloudPoint(points[id].point, new ColorRGBA(1f,1f,1f,1f), points[id].size*POINT_SELECT_SIZE_MULTIPLIER));
        }
    }
    
    private void unselectPoint(int id) {
        CloudPoint unselectPoint = selectedPoints.remove(id);
        setPoint(id, unselectPoint);
    }
}
