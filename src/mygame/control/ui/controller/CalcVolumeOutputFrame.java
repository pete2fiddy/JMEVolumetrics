package mygame.control.ui.controller;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class CalcVolumeOutputFrame extends JDialog {
    private final double VOLUME;
    private final JLabel VOLUME_LABEL = new JLabel();
    private final JButton COPY_VOLUME_BUTTON = new JButton("Copy");
    
    public CalcVolumeOutputFrame(double volume) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new GridLayout(0,1));
        COPY_VOLUME_BUTTON.addActionListener(COPY_LISTENER);
        
        this.VOLUME = volume;
        VOLUME_LABEL.setText("Volume: " + Double.toString(VOLUME));
        getContentPane().add(VOLUME_LABEL);
        getContentPane().add(COPY_VOLUME_BUTTON);
        
        
        pack();
    }
    
    private final ActionListener COPY_LISTENER = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
            //pull volume from the controller and handle here
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection toCopy = new StringSelection(Double.toString(VOLUME));
            c.setContents(toCopy, toCopy);
        }
    };
}
