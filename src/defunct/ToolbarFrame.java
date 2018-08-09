package defunct;

import defunct.Segmenter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import mygame.model.graph.Graph;

public abstract class ToolbarFrame <SegmenterType extends Enum, GraphType extends Enum> extends JFrame {
    /*
    if more than just graphs and segmenting done in a toolbar, then just change implementation to add more abstract methods for getting active versions of the
    new things
    */
    private List<ActionListener> listeners = new LinkedList<ActionListener>();
    public ToolbarFrame(String name) {
        super(name);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();
        this.repaint();
    }
    
    
    public abstract SegmenterType getActiveSegmenter();
    public abstract GraphType getActiveGraph();
    
    public void addListener(ActionListener listener) {
        listeners.add(listener);
    }
    
    private void updateListeners(ActionEvent ae) {
        for(ActionListener l : listeners) {
            l.actionPerformed(ae);
        }
    }
    
    /*
    Returns an actionListener that wraps al and ensures that all listeners added to the ToolbarFrame have their actionPerformed call after
    al's actionPerformed is called
    */
    protected ActionListener createInternalListener(final ActionListener al) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                al.actionPerformed(ae);
                updateListeners(ae);
            }
        };
    }
    
}
