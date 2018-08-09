package mygame.control.ui.controller;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mygame.control.ui.ColorChangingButton;
import mygame.control.ui.LabeledSliderPanel;

public class ControllerToolboxFrame <GraphType extends Enum, SegmenterType extends Enum> extends JFrame {
    private static final Color SELECTED_COLOR = Color.RED, UNSELECTED_COLOR = Color.WHITE;
    private Map<Component, GraphType> compToGraph = new HashMap<Component, GraphType>();
    private Map<Component, SegmenterType> compToSegmenter = new HashMap<Component, SegmenterType>();
    private JPanel graphPanel = new JPanel();
    private JPanel segmenterPanel = new JPanel();
    private ColorChangingButton activeGraphComponent;
    private ColorChangingButton activeSegmenterComponent;
    private LabeledSliderPanel radiusSliderPanel = new LabeledSliderPanel(0,100);
    private LabeledSliderPanel toleranceSliderPanel = new LabeledSliderPanel(0,100);
    
    public ControllerToolboxFrame(String name, Class<GraphType> graphTypeClass, Class<SegmenterType> segmenterTypeClass) {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        graphPanel.setLayout(new GridLayout(0,1));
        segmenterPanel.setLayout(new GridLayout(0,1));
        graphPanel.add(new JLabel("Graphs:"));
        segmenterPanel.add(new JLabel("Segmenters:"));
        addButtons(graphPanel, graphTypeClass, graphListener, compToGraph);
        addButtons(segmenterPanel, segmenterTypeClass, segmenterListener, compToSegmenter);
        
        this.add(graphPanel);
        this.add(segmenterPanel);
        
        radiusSliderPanel.setLayout(new BoxLayout(radiusSliderPanel, BoxLayout.X_AXIS));
        LabeledSliderPanel.setToDefaultLayout(radiusSliderPanel);
        radiusSliderPanel.slider.addChangeListener(radiusSliderListener);
        
        toleranceSliderPanel.setLayout(new BoxLayout(toleranceSliderPanel, BoxLayout.X_AXIS));
        LabeledSliderPanel.setToDefaultLayout(toleranceSliderPanel);
        toleranceSliderPanel.slider.addChangeListener(toleranceSliderListener);
        
        radiusSliderPanel.label.setText("Radius: ");
        toleranceSliderPanel.label.setText("Tolerance: ");
        
        this.add(radiusSliderPanel);
        this.add(toleranceSliderPanel);
        pack();
    }
    
    private <D extends Enum> void addButtons(Container addTo, Class<D> names, ActionListener addListener, Map<Component, D> compMap) {
        for(Object nameValue : names.getEnumConstants()) {
            D name = (D)nameValue;
            ColorChangingButton btn = new ColorChangingButton(name.toString());
            btn.setBackground(UNSELECTED_COLOR);
            btn.setActionCommand(name.toString());
            btn.addActionListener(addListener);
            btn.setFocusPainted(false);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            addTo.add(btn);
            compMap.put(btn, name);
        }
    }
    
    
    protected SegmenterType getActiveSegmenter() {return compToSegmenter.get(activeSegmenterComponent);}
    
    protected GraphType getActiveGraph() {return compToGraph.get(activeGraphComponent);}
    
    protected double getSegmentRadius() {
        return ((double)(radiusSliderPanel.slider.getValue() - radiusSliderPanel.slider.getMinimum()))/
                ((double)(radiusSliderPanel.slider.getMaximum()-radiusSliderPanel.slider.getMinimum()));
    }
    
    protected double getSegmentTolerance() {
    return ((double)(toleranceSliderPanel.slider.getValue() - toleranceSliderPanel.slider.getMinimum()))/
            ((double)(toleranceSliderPanel.slider.getMaximum()-toleranceSliderPanel.slider.getMinimum()));
    }
    
    private final ChangeListener radiusSliderListener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent ce) {
            radiusSliderPanel.label.setText("Radius: " + Double.toString(getSegmentRadius()));
        }
    };
    
    private final ChangeListener toleranceSliderListener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent ce) {
            toleranceSliderPanel.label.setText("Tolerance: " + Double.toString(getSegmentTolerance()));
        }  
    };
    
    private final ActionListener graphListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
            if(activeGraphComponent != null) activeGraphComponent.setBackground(UNSELECTED_COLOR);
            activeGraphComponent = (ColorChangingButton)ae.getSource();
            activeGraphComponent.setBackground(SELECTED_COLOR);
        }
    };
            
    private final ActionListener segmenterListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
            //TODO: implement slider visibility logic
            if(ae.getSource() == activeSegmenterComponent) {
                activeSegmenterComponent.setBackground(UNSELECTED_COLOR);
                activeSegmenterComponent = null;
            } else {
                if(activeSegmenterComponent != null) activeSegmenterComponent.setBackground(UNSELECTED_COLOR);
                activeSegmenterComponent = (ColorChangingButton)ae.getSource();
                activeSegmenterComponent.setBackground(SELECTED_COLOR);
            }
            pack();
        }
    };
    
}
