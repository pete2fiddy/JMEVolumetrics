package defunct.default_toolbox_cloud_manipulator;

import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class ToolboxSegmentationSliderPanel implements ChangeListener {
    private static final Set<ToolboxInteractiveCloudManipulatorController.SegmenterType> RADIUS_ADJUSTABLE_SEGMENTERS;
    private static final Set<ToolboxInteractiveCloudManipulatorController.SegmenterType> TOLERANCE_ADJUSTABLE_SEGMENTERS;
    
    static {
        RADIUS_ADJUSTABLE_SEGMENTERS = new HashSet<ToolboxInteractiveCloudManipulatorController.SegmenterType>();
        RADIUS_ADJUSTABLE_SEGMENTERS.add(ToolboxInteractiveCloudManipulatorController.SegmenterType.PAINTBRUSH);
        RADIUS_ADJUSTABLE_SEGMENTERS.add(ToolboxInteractiveCloudManipulatorController.SegmenterType.CONSTRAINED_PAINTBRUSH);
        
        TOLERANCE_ADJUSTABLE_SEGMENTERS = new HashSet<ToolboxInteractiveCloudManipulatorController.SegmenterType>();
        TOLERANCE_ADJUSTABLE_SEGMENTERS.add(ToolboxInteractiveCloudManipulatorController.SegmenterType.FLOODFILL);
        TOLERANCE_ADJUSTABLE_SEGMENTERS.add(ToolboxInteractiveCloudManipulatorController.SegmenterType.CONSTRAINED_PAINTBRUSH);
    }
    
    private JPanel panel = new JPanel();
    private ToolboxInteractiveCloudManipulatorController toControl;
    
    public ToolboxSegmentationSliderPanel(ToolboxInteractiveCloudManipulatorController toControl) {
        this.toControl = toControl;
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
