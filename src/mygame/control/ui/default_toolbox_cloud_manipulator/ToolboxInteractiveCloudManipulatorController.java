package mygame.control.ui.default_toolbox_cloud_manipulator;

import com.jme3.input.InputManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mygame.control.ui.LabeledSliderPanel;
import mygame.control.ui.SegmentPeripheralInputControllerVisitor;
import mygame.control.ui.SimpleToolbarFrame;
import mygame.control.ui.ToolbarFrame;
import mygame.control.ui.Updatable;
import mygame.model.data.ml.similarity.jme.JMECosAngleSquaredSimilarity;
import mygame.model.data.ml.similarity.jme.JMERadialBasisSimilarity;
import mygame.model.data.search.KDTree;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.graph.Graph;
import mygame.model.graph.OnTheFlySimilarityGraph;
import mygame.model.pointcloud.InteractivePointCloudManipulator;
import mygame.model.segment.FloodfillSegmenter;
import mygame.model.segment.PaintbrushSegmenter;
import mygame.model.segment.Segmenter;
import mygame.model.segment.SelectionSimilarityConstrainedPaintbrushSegmenter;
import mygame.model.volumetrics.CloudNormal;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import org.jblas.DoubleMatrix;

//might want to move the scene mover into here too
public class ToolboxInteractiveCloudManipulatorController implements Updatable, ActionListener, ChangeListener {

    private static final int[] SEGMENT_SETTING_SLIDER_TICK_RANGE = {0, 100};
    private static final String RADIUS_SLIDER_LABEL_TEXT = "Radius: ";
    private static final String TOLERANCE_SLIDER_LABEL_TEXT = "Tolerance: ";
    private static final Set<SegmenterType> RADIUS_ADJUSTABLE_SEGMENTERS;
    private static final Set<SegmenterType> TOLERANCE_ADJUSTABLE_SEGMENTERS;

    static {
        RADIUS_ADJUSTABLE_SEGMENTERS = new HashSet<SegmenterType>();
        RADIUS_ADJUSTABLE_SEGMENTERS.add(SegmenterType.PAINTBRUSH);
        RADIUS_ADJUSTABLE_SEGMENTERS.add(SegmenterType.CONSTRAINED_PAINTBRUSH);
        
        TOLERANCE_ADJUSTABLE_SEGMENTERS = new HashSet<SegmenterType>();
        TOLERANCE_ADJUSTABLE_SEGMENTERS.add(SegmenterType.FLOODFILL);
        TOLERANCE_ADJUSTABLE_SEGMENTERS.add(SegmenterType.CONSTRAINED_PAINTBRUSH);
    }

    private SegmentPeripheralInputControllerVisitor segmentController;
    private InteractivePointCloudManipulator cloudManipulator;
    private ToolbarFrame<SegmenterType, GraphType> toolbarFrame;

    private JPanel segmentationSettingSliderPanel = new JPanel();
    
    private LabeledSliderPanel radiusSliderPanel = new LabeledSliderPanel(SEGMENT_SETTING_SLIDER_TICK_RANGE[0], SEGMENT_SETTING_SLIDER_TICK_RANGE[1]);
    private LabeledSliderPanel toleranceSliderPanel = new LabeledSliderPanel(SEGMENT_SETTING_SLIDER_TICK_RANGE[0], SEGMENT_SETTING_SLIDER_TICK_RANGE[1]);

    private Map<GraphType, Graph> tokenToGraph = new HashMap<GraphType, Graph>();
    private Map<SegmenterType, Segmenter> tokenToSegmenter = new HashMap<SegmenterType, Segmenter>();

    //add a JFRame extension that contains segmentation types, graph types, and slider/s that this class can talk with
    //create a wrapper for a jcomponent that can be added to the jframe extension
    public ToolboxInteractiveCloudManipulatorController(InputManager inputManager, Camera cam, InteractivePointCloudManipulator cloudManipulator) {
        Vector3f[] points = cloudManipulator.getPointClones();
        NearestNeighborSearcher neighborSearcher = new KDTree(JblasJMEConverter.toArr(points));
        //purposefully do not orient normals. Would be a better idea to do that after selection, using the subset of the normals of the selection
        DoubleMatrix normals = CloudNormal.getUnorientedPCANormals(JblasJMEConverter.toDoubleMatrix(points), neighborSearcher, 30);
        
        
        
        this.segmentController = new SegmentPeripheralInputControllerVisitor(inputManager, cam, points, neighborSearcher);
        this.cloudManipulator = cloudManipulator;

        tokenToSegmenter.put(SegmenterType.FLOODFILL, new FloodfillSegmenter());
        tokenToSegmenter.put(SegmenterType.PAINTBRUSH, new PaintbrushSegmenter());
        tokenToSegmenter.put(SegmenterType.CONSTRAINED_PAINTBRUSH, new SelectionSimilarityConstrainedPaintbrushSegmenter());

        tokenToGraph.put(GraphType.SPARSE_DISTANCE, GraphUtil.constructSparseSimilarityGraph(points, new JMERadialBasisSimilarity(), neighborSearcher, 6));
        tokenToGraph.put(GraphType.SPARSE_ANGLE, GraphUtil.constructSparseSimilarityGraph(JblasJMEConverter.toVector3f(normals), new JMECosAngleSquaredSimilarity(), neighborSearcher, 6));
        tokenToGraph.put(GraphType.ON_THE_FLY_ANGLE, new OnTheFlySimilarityGraph(JblasJMEConverter.toVector3f(normals), new JMECosAngleSquaredSimilarity()));

        this.toolbarFrame = new SimpleToolbarFrame<SegmenterType, GraphType>("Toolbox", SegmenterType.class, GraphType.class);
        this.toolbarFrame.setResizable(false);
        this.toolbarFrame.addListener(this);

        addToolbarAddons();
    }

    private void addToolbarAddons() {
        segmentationSettingSliderPanel.setLayout(new GridLayout(0, 1));
        toolbarFrame.add(segmentationSettingSliderPanel);
        
        configureSliderPanel(radiusSliderPanel);
        configureSliderPanel(toleranceSliderPanel);
        
        segmentationSettingSliderPanel.add(radiusSliderPanel);
        segmentationSettingSliderPanel.add(toleranceSliderPanel);

        toolbarFrame.pack();
    }

    private void configureSliderPanel(LabeledSliderPanel p) {
        p.setLayout(new GridLayout(0,1));
        p.label.setHorizontalAlignment(JLabel.CENTER);
        p.slider.setPaintTicks(true);
        p.slider.setMajorTickSpacing((SEGMENT_SETTING_SLIDER_TICK_RANGE[1] - SEGMENT_SETTING_SLIDER_TICK_RANGE[0])/10);
        p.slider.addChangeListener(this);
    }
    
    @Override
    public void update(float timePerFrame) {
        segmentController.updateProjectionTransform(cloudManipulator.getCloud().getCloudNode().getWorldTransform().toTransformMatrix());
        Segmenter activeSegmenter = tokenToSegmenter.get(toolbarFrame.getActiveSegmenter());//segmenters.get(toolbarFrame.getActiveSegmenter());
        if (activeSegmenter != null) {
            cloudManipulator.selectPoints(segmentController.segment(activeSegmenter));
            if(RADIUS_ADJUSTABLE_SEGMENTERS.contains(toolbarFrame.getActiveSegmenter())) {
                radiusSliderPanel.setVisible(true);
            } else {
                radiusSliderPanel.setVisible(false);
            }
            if(TOLERANCE_ADJUSTABLE_SEGMENTERS.contains(toolbarFrame.getActiveSegmenter())) {
                toleranceSliderPanel.setVisible(true);
            } else {
                toleranceSliderPanel.setVisible(false);
            }
        } else {
            radiusSliderPanel.setVisible(false);
            toleranceSliderPanel.setVisible(false);
        }
        toolbarFrame.setSize(toolbarFrame.getPreferredSize());
        cloudManipulator.update(timePerFrame);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        segmentController.setGraph(tokenToGraph.get(toolbarFrame.getActiveGraph()));

        
        if (TOLERANCE_ADJUSTABLE_SEGMENTERS.contains(toolbarFrame.getActiveSegmenter())) {
            double sliderInterpVal = interpSliderVal(toleranceSliderPanel.slider.getValue());
            toleranceSliderPanel.label.setText(TOLERANCE_SLIDER_LABEL_TEXT + Double.toString(sliderInterpVal));
            segmentController.setSegmentTolerance(sliderInterpVal);
        } else if (RADIUS_ADJUSTABLE_SEGMENTERS.contains(toolbarFrame.getActiveSegmenter())) {
            double sliderInterpVal = interpSliderVal(radiusSliderPanel.slider.getValue());
            radiusSliderPanel.label.setText(RADIUS_SLIDER_LABEL_TEXT + Double.toString(sliderInterpVal));
            segmentController.setSegmentRadius(sliderInterpVal);
        }
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        //slider logic here
       
        if (RADIUS_ADJUSTABLE_SEGMENTERS.contains(toolbarFrame.getActiveSegmenter())) {
            double sliderInterpVal = interpSliderVal(radiusSliderPanel.slider.getValue());
            segmentController.setSegmentRadius(sliderInterpVal);
            radiusSliderPanel.label.setText(RADIUS_SLIDER_LABEL_TEXT + Double.toString(sliderInterpVal));
        }
        if (TOLERANCE_ADJUSTABLE_SEGMENTERS.contains(toolbarFrame.getActiveSegmenter())) {
            double sliderInterpVal = interpSliderVal(toleranceSliderPanel.slider.getValue());
            segmentController.setSegmentTolerance(sliderInterpVal);
            toleranceSliderPanel.label.setText(TOLERANCE_SLIDER_LABEL_TEXT + Double.toString(sliderInterpVal));
        }
        segmentationSettingSliderPanel.repaint();
    }

    private double interpSliderVal(int s) {
        return ((double)s) / ((double) (SEGMENT_SETTING_SLIDER_TICK_RANGE[1] - SEGMENT_SETTING_SLIDER_TICK_RANGE[0]));
    }


    private enum GraphType {
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

    private enum SegmenterType {
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
