package mygame.control.ui;

import defunct.ToolbarFrame;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import mygame.model.graph.Graph;

/*
A toolbar frame that automatically constructs itself using the names for segmenters and graphs supplied in the maps
passed to the constructor.
*/
public class SimpleToolbarFrame <SegmenterType extends Enum, GraphType extends Enum> extends ToolbarFrame <SegmenterType, GraphType> {
    private static final Color SELECTED_COLOR = Color.RED, UNSELECTED_COLOR = Color.WHITE;
    private Component activeGraphComponent = null;
    private Component activeSegmenterComponent = null;
    
    
    
    private Map<Component, GraphType> componentToGraph = new HashMap<Component, GraphType>();
    private Map<Component, SegmenterType> componentToSegmenter = new HashMap<Component, SegmenterType>();
    
    /*
    Segmenter names and graph names must be unique since it is a map
    */
    public SimpleToolbarFrame(String name,  Class<SegmenterType> segmenterNames, Class<GraphType> graphNames) {
        super(name);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        JPanel segmentPanel = new JPanel();
        segmentPanel.setLayout(new GridLayout(0,1));
        segmentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        
        JLabel segmentsField = new JLabel("Segmenters:");
        segmentsField.setHorizontalAlignment(JLabel.CENTER);
        segmentPanel.add(segmentsField);
        
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new GridLayout(0,1));
        graphPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel graphsField = new JLabel("Graphs:");
        graphsField.setHorizontalAlignment(JLabel.CENTER);
        graphPanel.add(graphsField);
        
        addButtonsTo(segmentPanel, segmenterNames, createInternalListener(new SegmenterActionListener()), componentToSegmenter);
        addButtonsTo(graphPanel, graphNames, createInternalListener(new GraphActionListener()), componentToGraph);
        
        add(segmentPanel);
        add(graphPanel);
        pack();
        repaint();
    }
    
    private <D extends Enum> void addButtonsTo(Container c, Class<D> names, ActionListener addListener, Map<Component, D> componentMap) {
        for(Object nameValue : names.getEnumConstants()) {
            D name = (D)nameValue;
            ColorChangingButton btn = new ColorChangingButton(name.toString());
            btn.setBackground(UNSELECTED_COLOR);
            btn.addActionListener(addListener);
            btn.setActionCommand(name.toString());
            btn.setFocusPainted(false);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            c.add(btn);
            componentMap.put(btn, name);
        }
    }

    @Override
    public SegmenterType getActiveSegmenter() {
        return componentToSegmenter.get(activeSegmenterComponent);
    }

    @Override
    public GraphType getActiveGraph() {
        return componentToGraph.get(activeGraphComponent);
    }
    
    private class SegmenterActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ae) {
            if(((Component)ae.getSource()).equals(activeSegmenterComponent)) {
                activeSegmenterComponent = null;
                unselect(((Component)ae.getSource()));
            } else {
                unselect(activeSegmenterComponent);
                activeSegmenterComponent = (Component)ae.getSource();
                select(activeSegmenterComponent);
            }
        }
    }
    
    private class GraphActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ae) {
            unselect(activeGraphComponent);
            activeGraphComponent = (Component)ae.getSource();
            select(activeGraphComponent);
        }
    }
    
    private void select(Component c) {
        if(c == null) return;
        c.setBackground(SELECTED_COLOR);
    }
    
    private void unselect(Component c) {
        if(c == null) return;
        c.setBackground(UNSELECTED_COLOR);
    }
    
    
}
