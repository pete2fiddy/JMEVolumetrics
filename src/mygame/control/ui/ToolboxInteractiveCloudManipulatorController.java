package mygame.control.ui;

import com.jme3.input.InputManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mygame.model.pointcloud.InteractivePointCloudManipulator;
import mygame.model.segment.PaintbrushSegmenter;
import mygame.model.segment.Segmenter;

//might want to move the scene mover into here too
public class ToolboxInteractiveCloudManipulatorController implements Updatable, ActionListener, ChangeListener {
    private SegmentPeripheralInputControllerVisitor segmentController;
    private InteractivePointCloudManipulator cloudManipulator;
    //add a JFRame extension that contains segmentation types, graph types, and slider/s that this class can talk with
    //create a wrapper for a jcomponent that can be added to the jframe extension
    private JFrame toolbox = new JFrame("Toolbox");
    
    public ToolboxInteractiveCloudManipulatorController(InputManager inputManager, Camera cam, InteractivePointCloudManipulator cloudManipulator) {
        Vector3f[] points = cloudManipulator.getPointClones();
        this.segmentController = new SegmentPeripheralInputControllerVisitor(inputManager, cam, points);
        this.cloudManipulator = cloudManipulator;
        initToolbox();
    }

    private void initToolbox() {
        toolbox.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        toolbox.setSize(300, 700);
        //init the rest of the toolbox, add components to it and set their listener to this class, then handle the actions
    }
    
    @Override
    public void update(float timePerFrame) {
        segmentController.updateProjectionTransform(cloudManipulator.getCloud().getCloudNode().getWorldTransform().toTransformMatrix());
        segmentController.setGraph(null);
        cloudManipulator.selectPoints(segmentController.segment(new PaintbrushSegmenter()));
        cloudManipulator.update(timePerFrame);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
