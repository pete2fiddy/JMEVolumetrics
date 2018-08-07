/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package defunct;

import com.jme3.math.Vector2f;
import mygame.control.ui.Updatable;

/**
 *
 * @author Owner
 */
public interface SegmenterController extends Updatable {
    public boolean selectActive();
    public boolean clearActive();
    public boolean eraseActive();
    public Vector2f getSelectPos();
    public Vector2f getCursorPos();
    public float getSelectionRadius();
    public int getNearestScreenNeighborId(Vector2f point);
    public double getTolerance();
}
