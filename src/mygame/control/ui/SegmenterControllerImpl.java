package mygame.control.ui;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import mygame.data.search.KDTree;
import mygame.data.search.NearestNeighborSearcher;
import mygame.data.search.PointSelectBFSNearestNeighborSearch;
import mygame.graph.Graph;
import mygame.graph.OnTheFlySimilarityGraph;
import mygame.ml.Segmenter;
import mygame.ml.similarity.jme.JMECosAngleSquaredSimilarity;
import mygame.ml.similarity.jme.JMERadialBasisSimilarity;
import mygame.pointcloud.InteractivePointCloudController;
import mygame.control.ui.SegmenterController;
import mygame.control.ui.SelectionDisabledSegmenter;
import mygame.control.ui.SimilarityThresholdedFloodfillCloudSegmenter;
import mygame.control.ui.SimilarityToSelectionPointPaintBrushCloudSegmenter;
import mygame.control.ui.SinglePointCloudSegmenter;
import mygame.control.ui.SphericalPaintBrushPointCloudSegmenter;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;



public class SegmenterControllerImpl implements SegmenterController, java.awt.event.ActionListener {
    
    private PointSelectBFSNearestNeighborSearch nnSelectSearch;
    private SegmenterGraphPair activeSegmenterGraphPair;
    private SegmenterSelectFrame segmenterSelectFrame = new SegmenterSelectFrame(300, 700);
    private static final String[] DISCRETE_ACTION_NAMES = {"SELECT_TOGGLE", "CLEAR_TOGGLE", "ERASE_TOGGLE"};
    private static final Trigger[] DEFAULT_DISCRETE_ACTION_TRIGGERS = {new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
        new KeyTrigger(KeyInput.KEY_C),
        new KeyTrigger(KeyInput.KEY_E)};
    
    private InputManager inputManager;
    private HashMap<String, Boolean> discreteActionStates = new HashMap<String, Boolean>();
    private Vector2f lastSelectPos = new Vector2f();
    
    private Camera cam;
    private InteractivePointCloudController cloudController;
    private final Map<SegmenterSelectFrame.SegmenterType, SegmenterGraphPair> SEGMENTER_TYPE_PAIR_MAP;
    
    /*
    Should all rotating be down about the center of the screen as opposed to what I have now?
    */
    public SegmenterControllerImpl(InputManager inputManager, Camera cam, InteractivePointCloudController cloudController) {
        this.inputManager = inputManager;
        this.cloudController = cloudController;
        this.cam = cam;
        addMappings();
        
        
        Vector3f[] points = cloudController.getPointClones();
        NearestNeighborSearcher kdTree = new KDTree(JblasJMEConverter.toArr(points));
        this.nnSelectSearch = new PointSelectBFSNearestNeighborSearch(cam, points);
        this.nnSelectSearch.setDoUpdate(true);
        Thread t = new Thread(this.nnSelectSearch);
        //with JME, threads must be daemon or else they will not close when application is closed
        t.setDaemon(true);
        t.start();
        
        
        segmenterSelectFrame.addActionListener(this);
        SEGMENTER_TYPE_PAIR_MAP = createSegmenterTypePairMap(kdTree);
        
    }
    
    
    private void addMappings() {
        for(int i = 0; i < DISCRETE_ACTION_NAMES.length; i++) {
            addMapping(DISCRETE_ACTION_NAMES[i], DEFAULT_DISCRETE_ACTION_TRIGGERS[i]);
        }
    }
    
    private void addMapping(String actionName, Trigger trigger) {
        inputManager.addMapping(actionName, trigger);
        discreteActionStates.put(actionName, false);
        inputManager.addListener(discreteActionListener, actionName);
    }
    
    private final ActionListener discreteActionListener = new ActionListener() {
        @Override
        public void onAction(String actionName, boolean isPressed, float tpf) {
            if(actionName.equals("ERASE_TOGGLE")) {
                if(isPressed) discreteActionStates.put(actionName, !discreteActionStates.get(actionName));
            } else {
                discreteActionStates.put(actionName, isPressed);
                if(actionName.equals("SELECT_TOGGLE") && isPressed) {
                    lastSelectPos = inputManager.getCursorPosition().clone();
                }
            }
        } 
    };
    
    @Override
    public Vector2f getSelectPos() {return lastSelectPos;}
    @Override
    public Vector2f getCursorPos() {return inputManager.getCursorPosition();}
    @Override
    public boolean selectActive() {return discreteActionStates.get("SELECT_TOGGLE");}
    @Override
    public boolean clearActive() {return discreteActionStates.get("CLEAR_TOGGLE");}
    @Override
    public boolean eraseActive() {return discreteActionStates.get("ERASE_TOGGLE");}

    @Override
    public int getNearestScreenNeighborId(Vector2f point) {
        return nnSelectSearch.getNearestNeighborId(new Vector3f(point.getX(), point.getY(), cam.getFrustumNear()), 10);
    }
    
    private void updateBFSCamAndTransform() {
        nnSelectSearch.setTransform(cloudController.getCloud().getCloudNode().getWorldTransform().toTransformMatrix());
    }
    
    
    @Override
    public float getSelectionRadius() {
        return 0.5f;//temp return for now
    }
    
    @Override
    public double getTolerance() {
        return 0.005;//temp for now
    }

    
    @Override
    public void actionPerformed(ActionEvent ae) {
        activeSegmenterGraphPair = SEGMENTER_TYPE_PAIR_MAP.get(segmenterSelectFrame.getActiveSegmenterType());
    }
    
    @Override
    public void update(float timePerFrame) {
        updateBFSCamAndTransform();
        cloudController.selectPoints(activeSegmenterGraphPair.SEGMENTER.getSegmentedIds(activeSegmenterGraphPair.GRAPH));
        cloudController.update(timePerFrame);
    }
    
    
    //get rid of KDTree and useless fields after they're used
    private Map<SegmenterSelectFrame.SegmenterType, SegmenterGraphPair> createSegmenterTypePairMap (NearestNeighborSearcher kdTree) {
        Map<SegmenterSelectFrame.SegmenterType, SegmenterGraphPair> out = new HashMap<SegmenterSelectFrame.SegmenterType, SegmenterGraphPair>();
        Vector3f[] points = cloudController.getPointClones();
        
        Graph onTheFlyAngle = new OnTheFlySimilarityGraph(points, new JMECosAngleSquaredSimilarity());
        Graph nearestNeighborSparseAngle = GraphUtil.constructSparseSimilarityGraph(points, new JMERadialBasisSimilarity(), kdTree, 6);
        Graph nearestNeighborSparseDistance = GraphUtil.constructSparseSimilarityGraph(points, new JMECosAngleSquaredSimilarity(), kdTree, 6);
        
        out.put(SegmenterSelectFrame.SegmenterType.PAINT, new SegmenterGraphPair(new SphericalPaintBrushPointCloudSegmenter(points, kdTree, this),
                null));
        out.put(SegmenterSelectFrame.SegmenterType.CONSTRAINED_ANGLE_PAINT, new SegmenterGraphPair(new SimilarityToSelectionPointPaintBrushCloudSegmenter(points, kdTree, this),
                onTheFlyAngle));
        out.put(SegmenterSelectFrame.SegmenterType.DISTANCE_FLOODFILL, new SegmenterGraphPair(new SimilarityThresholdedFloodfillCloudSegmenter(this),
                nearestNeighborSparseDistance));
        out.put(SegmenterSelectFrame.SegmenterType.POINT, new SegmenterGraphPair(new SinglePointCloudSegmenter(this), null));
        
        SegmenterGraphPair disabledSegmenter = new SegmenterGraphPair(new SelectionDisabledSegmenter(), null);
        out.put(SegmenterSelectFrame.SegmenterType.DISABLED, disabledSegmenter);
        
        activeSegmenterGraphPair = disabledSegmenter;
        for (SegmenterSelectFrame.SegmenterType segmenterType : SegmenterSelectFrame.SegmenterType.values()) {
            assert (out.containsKey(segmenterType)) : "createSegmenterTypePairMap implementation is missing a segmenter type!";
        }
        return out;
    }

    private class SegmenterGraphPair {
        public final Segmenter SEGMENTER;
        public final Graph GRAPH;
        
        public SegmenterGraphPair(Segmenter s, Graph g) {
            SEGMENTER = s;
            GRAPH = g;
        }
    }
}
