package mygame.control.ui;

import javax.swing.JFrame;

public class ToolbarFrame extends JFrame {
    
    public ToolbarFrame(String name) {
        super(name);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.repaint();
    }
}
