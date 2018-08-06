/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ui;

import com.jme3.math.Vector2f;

/**
 *
 * @author Owner
 */
public interface SegmenterToolControllerInterface {
    public boolean selectActive();
    public boolean clearActive();
    public boolean eraseActive();
    public Vector2f getSelectPos();
    public Vector2f getCursorPos();
    public float getSelectRadius();
}
