package defunct;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.HashSet;
import java.util.Set;
import mygame.control.ui.PeripheralInputTokenizer;
import mygame.control.ui.PeripheralInputTokenizer.ActionActiveState.ActivationType;
import mygame.model.data.search.KDTree;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.data.search.PointSelectBFSNearestNeighborSearch;
import mygame.model.graph.Graph;
import mygame.model.segment.FloodfillSegmenter;
import mygame.model.segment.FloodfillSegmenter.FloodfillSegmenterArgs;
import mygame.model.segment.PaintbrushSegmenter;
import mygame.model.segment.PaintbrushSegmenter.PaintbrushSegmenterArgs;
import mygame.model.segment.Segmenter;
import mygame.model.segment.SegmenterVisitor;
import mygame.model.segment.SelectionSimilarityConstrainedPaintbrushSegmenter;
import mygame.model.segment.SelectionSimilarityConstrainedPaintbrushSegmenter.SelectionSimilarityConstrainedPaintbrushSegmenterArgs;
import mygame.util.JblasJMEConverter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/*
segments a set of points using peripheral (mouse + keyboard) input. Other settings can be adjusted externally. Made most sense to me to only put peripheral input in here.
*/
public class SegmentPeripheralInputControllerVisitor implements SegmenterVisitor<Set<Integer>>, SegmentContainer {
    private static final String SELECT_ACTION = "SELECT", CLEAR_ACTION = "CLEAR", ERASE_ACTION = "ERASE";
    private static final Trigger DEFAULT_SELECT_TRIGGER = new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
            DEFAULT_CLEAR_TRIGGER = new KeyTrigger(KeyInput.KEY_C),
            DEFAULT_ERASE_TRIGGER = new KeyTrigger(KeyInput.KEY_E);
    private static final ActivationType DEFAULT_SELECT_ACTIVE = ActivationType.ON_WHILE_PRESSED, 
            DEFAULT_CLEAR_ACTIVE = ActivationType.ON_WHILE_PRESSED,
            DEFAULT_ERASE_ACTIVE = ActivationType.TOGGLE_ON_PRESS;
    
    
    private static final int SCREEN_NEIGHBOR_SEARCH_RADIUS = 10;
    private NearestNeighborSearcher neighborSearcher;
    private PointSelectBFSNearestNeighborSearch screenNeighborSearcher;
    private InputManager inputManager;
    private double[][] points;
    private PeripheralInputTokenizer inputTokenizer;
    private Graph activeGraph;
    private Set<Integer> currentlySelected = new HashSet<Integer>();
    private double segmentRadius = 0.5, segmentTolerance = 0.5;
    
    //would be nice if I could figure out a clean way to avoid passing points, since it is only used in one line of code in the entire class
    public SegmentPeripheralInputControllerVisitor(InputManager inputManager, Camera cam, Vector3f[] points, NearestNeighborSearcher neighborSearcher) {
        screenNeighborSearcher = new PointSelectBFSNearestNeighborSearch(cam, points);
        screenNeighborSearcher.setDoUpdate(true);
        Thread t= new Thread(screenNeighborSearcher);
        //needs to be set to daemon or else it won't close after app closes
        t.setDaemon(true);
        t.start();
        
        
        this.points = JblasJMEConverter.toArr(points);
        this.inputManager = inputManager;
        this.neighborSearcher = neighborSearcher;
        
        this.inputTokenizer = new PeripheralInputTokenizer(inputManager);
        inputTokenizer.addMapping(SELECT_ACTION, DEFAULT_SELECT_TRIGGER, DEFAULT_SELECT_ACTIVE);
        inputTokenizer.addMapping(CLEAR_ACTION, DEFAULT_CLEAR_TRIGGER, DEFAULT_CLEAR_ACTIVE);
        inputTokenizer.addMapping(ERASE_ACTION, DEFAULT_ERASE_TRIGGER, DEFAULT_ERASE_ACTIVE);
    }
    
    public void setGraph(Graph g) {activeGraph = g;}
    
    public Set<Integer> segment(Segmenter segmenter) {
        //do general-purpose logic for checking if election is allowed, etc. (selection clicked/no eraser down, etc)
        if(inputTokenizer.actionActive(CLEAR_ACTION)) {
            currentlySelected = new HashSet<Integer>();
        } else if(inputTokenizer.actionActive(SELECT_ACTION)) {
            Set<Integer> newSegmentedIds = (Set<Integer>) segmenter.accept(this);
            if(inputTokenizer.actionActive(ERASE_ACTION)) {
                currentlySelected.removeAll(newSegmentedIds);
            } else {
                currentlySelected.addAll(newSegmentedIds);
            }
        }
        return currentlySelected;
    }
    
    @Override
    public Set<Integer> visit(PaintbrushSegmenter brushSegmenter) {
        int nearestScreenNeighbor = getNearestScreenNeighbor();
        if(nearestScreenNeighbor < 0) return new HashSet<Integer>();
        PaintbrushSegmenterArgs args = new PaintbrushSegmenterArgs(neighborSearcher, 
                points[nearestScreenNeighbor],
                segmentRadius);
        
        return brushSegmenter.segment(args);
    }

    
    
    @Override
    public Set<Integer> visit(SelectionSimilarityConstrainedPaintbrushSegmenter constrainedPaintSegmenter) {
        int nearestScreenNeighbor = getNearestScreenNeighbor();
        if(nearestScreenNeighbor < 0) return new HashSet<Integer>();
        SelectionSimilarityConstrainedPaintbrushSegmenterArgs args = new SelectionSimilarityConstrainedPaintbrushSegmenterArgs(activeGraph, 
                neighborSearcher, 
                points[nearestScreenNeighbor], 
                segmentRadius, 
                segmentTolerance);
        return constrainedPaintSegmenter.segment(args);
    }
    
    @Override
    public Set<Integer> visit(FloodfillSegmenter fillSegmenter) {
        int nearestScreenNeighbor = getNearestScreenNeighbor();
        if(nearestScreenNeighbor < 0) return new HashSet<Integer>();
        FloodfillSegmenterArgs args = new FloodfillSegmenterArgs(activeGraph, nearestScreenNeighbor, segmentTolerance);
        return fillSegmenter.segment(args);
    }
    
    
    
    private int getNearestScreenNeighbor() {
        Vector2f cursorPos = getCursorPos();
        return screenNeighborSearcher.getNearestNeighborId(new Vector3f(cursorPos.x, cursorPos.y, 0), SCREEN_NEIGHBOR_SEARCH_RADIUS);
    }

    private Vector2f getCursorPos() {return inputManager.getCursorPosition();}
    
    /*
    this MUST be called in order to update the screen neighbor searcher after any kind of transformation is applied to the set of points
    */
    public void updateProjectionTransform(Matrix4f transform) {this.screenNeighborSearcher.setTransform(transform);}
    
    
    public void setSegmentRadius(double rad) {this.segmentRadius = rad;}
    public void setSegmentTolerance(double tol) {this.segmentTolerance = tol;}

    @Override
    public Set<Integer> getSegmentedIds() {
        return currentlySelected;
    }


    
}
