package mygame.input;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import mygame.pointcloud.InteractivePointCloudController;
import mygame.ui.SelectionDisabledSegmenter;
import mygame.ui.Updatable;
import mygame.ui.action.UIControlAction;
import mygame.ui.action.UISegmenterAction;

public class InteractivePointCloudToolController implements ActionListener {
    private static final Color BUTTON_UNSELECTED_COLOR = Color.BLACK, BUTTON_SELECTED_COLOR = Color.RED;
    
    private InteractivePointCloudController controlCloud;
    private final JFrame FRAME = new JFrame();
    private HashMap<String, UIControlAction> commandActionMap = new HashMap<String, UIControlAction>();
    private HashMap<String, JButton> commandButtonMap = new HashMap<String, JButton>();
    private String activeCommand;
    
    public InteractivePointCloudToolController(InteractivePointCloudController controlCloud, int width, int height) {
        
        FRAME.setSize(width, height);
        FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FRAME.setVisible(true);
        
        this.controlCloud = controlCloud;
        //when adding buttons, add this as an action listener too to enable it and do other logic. Use a hashmap for command string to button
        initLayout();
        initActionsAndButtons();
        FRAME.pack();
        FRAME.repaint();
    }
    
    private void initLayout() {
        FRAME.setLayout(new FlowLayout());
    }
    
    private void initActionsAndButtons() {
        addButton(FRAME, new UISegmenterAction(controlCloud, controlCloud.PAINTBRUSH, null), "Paint", "PAINT_SELECT");
        addButton(FRAME, new UISegmenterAction(controlCloud, controlCloud.SIMILARITY_TO_SELECTION_CONSTRAINED_PAINTBRUSH, controlCloud.ON_THE_FLY_ANGLE), 
                "Constrained Paint", "CONSTRAINED_ANGLE_PAINT_SELECT");
        addButton(FRAME, new UISegmenterAction(controlCloud, controlCloud.SIMILARITY_THRESHOLDED_FLOODFILL, controlCloud.NEAREST_NEIGHBOR_SPARSE_DISTANCE),
                "Distance Floodfill", "DISTANCE_FLOODFILL_SELECT");
        addButton(FRAME, new UISegmenterAction(controlCloud, controlCloud.SINGLE_POINT, null), "Single Point", "POINT_SELECT");
        
    }
    
    private void addButton(Container c, UIControlAction controlAction, String buttonText, String actionCommand) {
        JButton out = new JButton(buttonText);
        out.setActionCommand(actionCommand);
        out.addActionListener(this);
        out.setOpaque(true);
        out.setBorderPainted(false);
        out.setBackground(BUTTON_UNSELECTED_COLOR);
        out.setForeground(BUTTON_UNSELECTED_COLOR);
        c.add(out);
        out.setVisible(true);
        commandButtonMap.put(actionCommand, out);
        commandActionMap.put(actionCommand, controlAction);
    }
    

    @Override
    public void actionPerformed(ActionEvent ae) {
        if(ae.getActionCommand().equals(activeCommand)) {
            unselectButton(activeCommand);
            controlCloud.setSegmenter(new SelectionDisabledSegmenter());
        } else {
            unselectButton(activeCommand);
            selectButton(ae.getActionCommand());
        }
        FRAME.repaint();
    }
    
    private void selectButton(String actionCommand) {
        if(actionCommand == null) return;
        activeCommand = actionCommand;
        commandActionMap.get(actionCommand).performAction();
        commandButtonMap.get(actionCommand).setBackground(BUTTON_SELECTED_COLOR);
        commandButtonMap.get(actionCommand).setForeground(BUTTON_SELECTED_COLOR);
    }
    
    private void unselectButton(String actionCommand) {
        if(actionCommand == null) return;
        commandButtonMap.get(actionCommand).setBackground(BUTTON_UNSELECTED_COLOR);
        commandButtonMap.get(actionCommand).setForeground(BUTTON_UNSELECTED_COLOR);
        activeCommand = null;
    }
    
}
