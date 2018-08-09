package mygame.control.ui.controller;

import com.jme3.input.InputManager;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mygame.control.ui.Updatable;
import mygame.model.data.ml.similarity.jme.JMECosAngleSquaredSimilarity;
import mygame.model.data.ml.similarity.jme.JMERadialBasisSimilarity;
import mygame.model.data.search.KDTree;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.data.search.PointSelectBFSNearestNeighborSearch;
import mygame.model.graph.Graph;
import mygame.model.graph.OnTheFlySimilarityGraph;
import mygame.model.pointcloud.InteractivePointCloudManipulator;
import mygame.model.segment.FloodfillSegmenter;
import mygame.model.segment.PaintbrushSegmenter;
import mygame.model.segment.Segmenter;
import mygame.model.segment.SegmenterVisitor;
import mygame.model.segment.SelectionSimilarityConstrainedPaintbrushSegmenter;
import mygame.model.segment.SelectionSimilarityConstrainedPaintbrushSegmenter.SelectionSimilarityConstrainedPaintbrushSegmenterArgs;
import mygame.model.volumetrics.CloudNormal;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import org.jblas.DoubleMatrix;


public class Controller implements Updatable, SegmenterVisitor<Set<Integer>> {
    private UIController<GraphType, SegmenterType> input;
    private InteractivePointCloudManipulator model;
    private NearestNeighborSearcher neighborSearcher;
    private PointSelectBFSNearestNeighborSearch screenNeighborSearcher;
    private Map<GraphType, Graph> graphMap = new HashMap<GraphType, Graph>();
    private Map<SegmenterType, Segmenter> segmenterMap = new HashMap<SegmenterType, Segmenter>();
    private Set<Integer> selectedPoints = new HashSet<Integer>();
    private double[][] points;
    
    public Controller(InputManager inputManager, Camera cam, InteractivePointCloudManipulator model) {
        Vector3f[] pointsVec = model.getPointClones();
        this.points = JblasJMEConverter.toArr(pointsVec);
        this.model = model;
        this.input = new UIController<GraphType, SegmenterType>(inputManager, GraphType.class, SegmenterType.class);
        this.neighborSearcher = new KDTree(JblasJMEConverter.toArr(pointsVec));
        fillMaps(pointsVec, CloudNormal.getUnorientedPCANormals(JblasJMEConverter.toDoubleMatrix(pointsVec), neighborSearcher, 30));
        initScreenNeighborSearcher(cam, pointsVec);
    }
    
    private void initScreenNeighborSearcher(Camera cam, Vector3f[] pointsVec) {
        this.screenNeighborSearcher = new PointSelectBFSNearestNeighborSearch(cam, pointsVec);
        Thread t = new Thread(this.screenNeighborSearcher);
        t.setDaemon(true);//must be set to daemon or else it won't close when the JME window is closed
        t.start();
    }
    
    private void fillMaps(Vector3f[] points, DoubleMatrix normals) {
        segmenterMap.put(SegmenterType.FLOODFILL, new FloodfillSegmenter());
        segmenterMap.put(SegmenterType.PAINTBRUSH, new PaintbrushSegmenter());
        segmenterMap.put(SegmenterType.CONSTRAINED_PAINTBRUSH, new SelectionSimilarityConstrainedPaintbrushSegmenter());

        graphMap.put(GraphType.SPARSE_DISTANCE, GraphUtil.constructSparseSimilarityGraph(points, new JMERadialBasisSimilarity(), neighborSearcher, 6));
        graphMap.put(GraphType.SPARSE_ANGLE, GraphUtil.constructSparseSimilarityGraph(JblasJMEConverter.toVector3f(normals), new JMECosAngleSquaredSimilarity(), neighborSearcher, 6));
        graphMap.put(GraphType.ON_THE_FLY_ANGLE, new OnTheFlySimilarityGraph(JblasJMEConverter.toVector3f(normals), new JMECosAngleSquaredSimilarity()));
    }
    
    
    private Set<Integer> segment(Segmenter s) {
        return (Set<Integer>)s.accept(this);
    }
    
    @Override
    public Set<Integer> visit(PaintbrushSegmenter brushSegmenter) {
        int nearestScreenNeighbor = getNearestScreenNeighbor();
        if(nearestScreenNeighbor < 0) return new HashSet<Integer>();
        PaintbrushSegmenter.PaintbrushSegmenterArgs args = new PaintbrushSegmenter.PaintbrushSegmenterArgs(neighborSearcher, 
                points[nearestScreenNeighbor],
                input.getSegmentRadius());
        
        return brushSegmenter.segment(args);
    }

    @Override
    public Set<Integer> visit(SelectionSimilarityConstrainedPaintbrushSegmenter constrainedPaintSegmenter) {
        int nearestScreenNeighbor = getNearestScreenNeighbor();
        if(nearestScreenNeighbor < 0) return new HashSet<Integer>();
        SelectionSimilarityConstrainedPaintbrushSegmenterArgs args = new SelectionSimilarityConstrainedPaintbrushSegmenterArgs(
                graphMap.get(input.getActiveGraph()), 
                neighborSearcher, 
                points[nearestScreenNeighbor], 
                input.getSegmentRadius(), 
                input.getSegmentTolerance());
        return constrainedPaintSegmenter.segment(args);
    }
    
    @Override
    public Set<Integer> visit(FloodfillSegmenter fillSegmenter) {
        int nearestScreenNeighbor = getNearestScreenNeighbor();
        if(nearestScreenNeighbor < 0) return new HashSet<Integer>();
        FloodfillSegmenter.FloodfillSegmenterArgs args = new FloodfillSegmenter.FloodfillSegmenterArgs(graphMap.get(input.getActiveGraph()), nearestScreenNeighbor, input.getSegmentTolerance());
        return fillSegmenter.segment(args);
    }
    
    private int getNearestScreenNeighbor() {
        if(!input.actionActive(UIController.ActionType.SELECT_ACTION)) return -1;
        Vector2f selectPos = input.getSelectPos();
        if(selectPos == null) return -1;
        return screenNeighborSearcher.getNearestNeighborId(new Vector3f(selectPos.x, selectPos.y, 0), 10);
    }
    
    @Override
    public void update(float timePerFrame) {
        screenNeighborSearcher.setTransform(model.getCloud().getCloudNode().getWorldTransform().toTransformMatrix());
        if(input.getActiveSegmenter() != null) {
            if(input.actionActive(UIController.ActionType.SELECT_ACTION)) {
                if(!input.actionActive(UIController.ActionType.ERASE_ACTION)) {
                    selectedPoints.addAll(segment(segmenterMap.get(input.getActiveSegmenter())));
                } else {
                    selectedPoints.removeAll(segment(segmenterMap.get(input.getActiveSegmenter())));
                }
            }
        }
        if(input.actionActive(UIController.ActionType.CLEAR_ACTION)) {
            selectedPoints = new HashSet<Integer>();
        }
        model.selectPoints(selectedPoints);
        model.update(timePerFrame);
        input.update(timePerFrame);
    }
    
    private static enum GraphType {
        ON_THE_FLY_ANGLE("On the fly angle"), SPARSE_DISTANCE("Sparse distance"), SPARSE_ANGLE("Sparse angle");

        private final String BUTTON_TEXT;

        private GraphType(String buttonText) {
            this.BUTTON_TEXT = buttonText;
        }

        @Override
        public String toString() {
            return BUTTON_TEXT;
        }
    }

    private static enum SegmenterType {
        PAINTBRUSH("Paintbrush"), FLOODFILL("Floodfill"), CONSTRAINED_PAINTBRUSH("Constrained paintbrush");
        private final String BUTTON_TEXT;

        private SegmenterType(String buttonText) {
            this.BUTTON_TEXT = buttonText;
        }

        @Override
        public String toString() {
            return BUTTON_TEXT;
        }
    }
}
