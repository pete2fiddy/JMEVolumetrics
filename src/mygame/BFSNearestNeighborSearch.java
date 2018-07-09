/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import mygame.util.PointUtils;

/**
 *
 * @author Owner
 */
public class BFSNearestNeighborSearch implements Runnable {
    private int[][] idBuffer;
    private Vector3f[] X;
    private Camera cam;
    private boolean doUpdate = true;
    
    public BFSNearestNeighborSearch(Vector3f[] X, Camera cam, int bufferWidth, int bufferHeight) {
        this.X = X;
        this.idBuffer = new int[bufferWidth][bufferHeight];
        this.cam = cam;
    }
    

    @Override
    public void run() {
        while(doUpdate) {
            updateBuffer();
        }
    }
    
    public void setDoUpdate(boolean b) {doUpdate = b;}
    
    private void updateBuffer() {
        this.idBuffer = new int[idBuffer.length][idBuffer[0].length];
        for(int i = 0; i < X.length; i++) {
            Vector2f xScreen = PointUtils.getScreenPos(X[i], cam);
            idBuffer[(int)xScreen.getX()][(int)xScreen.getY()] = i;
        }
    }
}
