package mygame.control.ui;

import java.awt.Color;
import javax.swing.JButton;

/*
an extension of JButton made to fix an annoying issue where changing backgrounds of a JButton was unecessarily convoluted (order methods are called matters, for example)
*/
public class ColorChangingButton extends JButton {
    
    public ColorChangingButton(String text) {
        super(text);
        this.setContentAreaFilled(false);
        //opacity is required, but setContentAreaFilled can disable it. It must be called after the setContentAreaFilled call because of this.
        this.setOpaque(true);
        this.setBorderPainted(false);
    }
}
