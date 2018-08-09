package mygame.control.ui;

import javax.swing.BoxLayout;
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
    
    public static void setToDefaultLayout(LabeledSliderPanel p) {
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.slider.setFocusable(false);
        p.slider.setPaintTicks(true);
        p.slider.setMajorTickSpacing((p.slider.getMaximum() - p.slider.getMinimum())/10);
    }
}
