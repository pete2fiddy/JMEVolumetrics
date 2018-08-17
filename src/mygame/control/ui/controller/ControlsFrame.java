package mygame.control.ui.controller;

import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ControlsFrame extends JFrame{
    private JLabel controlsLabel;
    
    public ControlsFrame(Map<String, Trigger> bindings) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new GridLayout(0,1));
        
        controlsLabel = new JLabel(parseBindings(bindings));
        getContentPane().add(controlsLabel);
        pack();
    }
    
    private static String parseBindings(Map<String, Trigger> bindings) {
        String out = "<html>";
        for(String action : bindings.keySet()) {
            Trigger actionTrigger = bindings.get(action);
            out += action + ": " + actionTrigger.getName() + "<BR>";
        }
        out += "</html>";
        return out;
    }
}
