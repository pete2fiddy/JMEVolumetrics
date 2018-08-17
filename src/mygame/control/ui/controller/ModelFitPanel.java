package mygame.control.ui.controller;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ModelFitPanel extends JPanel {
    protected JButton fitButton = new JButton("Fit to mesh");
    protected JButton convHullButton = new JButton("Fit to Convex Hull");
    protected JButton calcVolumeButton = new JButton("Calculate volume");
    
    public ModelFitPanel(ActionListener fitListener, ActionListener calcVolumeListener, ActionListener convHullListener) {
        setLayout(new GridLayout(0,1));
        fitButton.addActionListener(fitListener);
        calcVolumeButton.addActionListener(calcVolumeListener);
        convHullButton.addActionListener(convHullListener);
        add(fitButton);
        add(convHullButton);
        add(calcVolumeButton);
        calcVolumeButton.setVisible(false);
    }
    
}
