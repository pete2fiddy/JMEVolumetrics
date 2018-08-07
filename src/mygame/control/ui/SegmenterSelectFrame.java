package mygame.control.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import mygame.pointcloud.InteractivePointCloudController;
import mygame.control.ui.SelectionDisabledSegmenter;
import mygame.control.ui.Updatable;
import defunct.ui.action.UIControlAction;

public class SegmenterSelectFrame implements ActionListener {

    private static final Color BUTTON_UNSELECTED_COLOR = Color.BLACK, BUTTON_SELECTED_COLOR = Color.RED;
    private final JFrame FRAME = new JFrame();
    private SegmenterTypeButton activeSegmenterTypeButton;
    private List<ActionListener> listeners = new LinkedList<ActionListener>();

    public SegmenterSelectFrame(int width, int height) {
        FRAME.setSize(width, height);
        FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FRAME.setVisible(true);

        initLayout();
        initActionsAndButtons();
        FRAME.pack();
        FRAME.repaint();
    }

    private void initLayout() {
        FRAME.setLayout(new FlowLayout());
    }

    public SegmenterType getActiveSegmenterType() {
        if (activeSegmenterTypeButton == null) {
            return SegmenterType.DISABLED;
        }
        return activeSegmenterTypeButton.TYPE;
    }

    private void initActionsAndButtons() {
        addButton(FRAME, SegmenterType.PAINT, "Paint");
        addButton(FRAME, SegmenterType.CONSTRAINED_ANGLE_PAINT, "Constrained Paint");
        addButton(FRAME, SegmenterType.DISTANCE_FLOODFILL, "Distance Floodfill");
        addButton(FRAME, SegmenterType.POINT, "Single Point");
    }

    private void addButton(Container c, SegmenterType type, String buttonText) {
        SegmenterTypeButton out = new SegmenterTypeButton(buttonText, type);
        out.setOpaque(true);
        out.setBorderPainted(false);
        out.setBackground(BUTTON_UNSELECTED_COLOR);
        out.setForeground(BUTTON_UNSELECTED_COLOR);
        c.add(out);
        out.setVisible(true);
        out.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        assert (source instanceof SegmenterTypeButton);
        SegmenterTypeButton sourceButton = (SegmenterTypeButton) source;
        SegmenterType selectSegmenterType = sourceButton.TYPE;
        if(activeSegmenterTypeButton == null) {
            selectButton(sourceButton);
        } else if (selectSegmenterType == activeSegmenterTypeButton.TYPE) {
            unselectButton(sourceButton);
        } else {
            unselectButton(activeSegmenterTypeButton);
            selectButton(sourceButton);
        }
        if(activeSegmenterTypeButton!=null) System.out.println("active segmenter type: " + activeSegmenterTypeButton.TYPE);
        if(activeSegmenterTypeButton == null) System.out.println("active segmenter type: null");
        for(ActionListener al : listeners) {
            al.actionPerformed(ae);
        }
    }

    private void selectButton(SegmenterTypeButton b) {
        activeSegmenterTypeButton = b;
        b.setBackground(BUTTON_SELECTED_COLOR);
        b.setForeground(BUTTON_SELECTED_COLOR);
    }

    private void unselectButton(JButton b) {
        activeSegmenterTypeButton = null;
        b.setBackground(BUTTON_UNSELECTED_COLOR);
        b.setForeground(BUTTON_UNSELECTED_COLOR);
    }

    public void addActionListener(ActionListener al) {
        //can't just add al as a listener to each button, because does not guarantee the correct order of calling actionPerformed between
        //this class's actionPerformed and the added listeners
        listeners.add(al);
    }

    public enum SegmenterType {
        PAINT, CONSTRAINED_ANGLE_PAINT, DISTANCE_FLOODFILL, POINT, DISABLED;

    }

    private class SegmenterTypeButton extends JButton {

        public final SegmenterType TYPE;

        public SegmenterTypeButton(String name, SegmenterType type) {
            super(name);
            this.TYPE = type;
        }
    }
}
