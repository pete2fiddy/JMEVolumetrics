package mygame.control.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

public class LabeledSliderPanel extends JPanel {
    public JSlider slider;
    public JLabel label =  new JLabel();
    
    public LabeledSliderPanel() {
        this.slider = new JSlider();
        add(label); 
        add(slider);
    }
    
    public LabeledSliderPanel(int minSlider, int maxSlider) {
        this.slider = new JSlider(minSlider, maxSlider); 
       add(label);
        add(slider);
    }
}
