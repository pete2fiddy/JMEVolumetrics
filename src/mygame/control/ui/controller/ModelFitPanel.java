package mygame.control.ui.controller;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ModelFitPanel extends JPanel {
    protected JButton fitButton = new JButton("Fit to mesh");
    protected JButton calcVolumeButton = new JButton("Calculate volume");
    
    public ModelFitPanel(ActionListener fitListener, ActionListener calcVolumeListener) {
        setLayout(new GridLayout(0,1));
        fitButton.addActionListener(fitListener);
        calcVolumeButton.addActionListener(calcVolumeListener);
        add(fitButton);
        add(calcVolumeButton);
        calcVolumeButton.setVisible(false);
    }
    
}
