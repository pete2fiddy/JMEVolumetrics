/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.Matrix4f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.LinkedList;
import mygame.util.PointUtils;

/**
 *
 * @author Owner
 */
public class BFSNearestNeighborSearch implements Runnable {
    private final int MAX_NEIGHBORHOOD_SEARCH_DISTANCE = 15;
    private final int[][] idBuffer;
    private final Vector3f[] X;
    private final Camera cam;
    //explanation for why doUpdate must be volatile here: https://meta.stackoverflow.com/questions/269174/questions-about-threadloop-not-working-without-print-statement
    private volatile boolean doUpdate = true;
    private Matrix4f pointTransform = Matrix4f.IDENTITY;
    
    public BFSNearestNeighborSearch(Camera cam, Vector3f[] X, int bufferWidth, int bufferHeight) {
        this.cam = cam;
        this.X = X;
        this.idBuffer = new int[bufferWidth][bufferHeight];
        resetIdBuffer();
    }
    
    public int getNearestNeighborId(Vector2f point) {
        LinkedList<int[]> points = new LinkedList<int[]>();
        points.add(new int[] {(int)point.getX(), (int)point.getY()});
        return getNearestNeighborId(points, new boolean[idBuffer.length][idBuffer[0].length], MAX_NEIGHBORHOOD_SEARCH_DISTANCE);
    }
    
    private int getNearestNeighborId(LinkedList<int[]> points, boolean[][] visited, int depthRemaining) {
        if(points.size() <= 0 || depthRemaining <= 0) {
            return -1;
        }
        
        LinkedList<int[]> childPoints = new LinkedList<int[]>();
        for(int[] point : points) {
            if(idBuffer[point[0]][point[1]] != -1) return idBuffer[point[0]][point[1]];
            visited[point[0]][point[1]] = true;
        }
        for(int[] point : points) {
            childPoints.addAll(getNeighbors(point, visited));
        }
        return getNearestNeighborId(childPoints, visited, depthRemaining - 1);
    }
    
    
    private LinkedList<int[]> getNeighbors(int[] point, boolean[][] visited) {
        LinkedList<int[]> neighbors = new LinkedList<int[]>();
        if(point[0] > 0 && !visited[point[0]-1][point[1]]) {
            neighbors.add(new int[] {point[0]-1, point[1]});
        }
        if(point[0] < idBuffer.length-1 && !visited[point[0]+1][point[1]]) {
            neighbors.add(new int[] {point[0]+1, point[1]});
        }
        if(point[1] > 0 && !visited[point[0]][point[1]-1]) {
            neighbors.add(new int[] {point[0], point[1]-1});
        }
        if(point[1] < idBuffer[0].length-1 && !visited[point[0]][point[1]+1]) {
            neighbors.add(new int[] {point[0], point[1]+1});
        }
        return neighbors;
    }
    
    @Override
    public void run() {
        //explanation for why doUpdate must be volatile here: https://meta.stackoverflow.com/questions/269174/questions-about-threadloop-not-working-without-print-statement
        while(doUpdate) {
            if(cam != null) {
                updateBuffer();
            }
        }
    }
    
    public void setTransform(Matrix4f transform) {this.pointTransform = transform;}
    
    public void setDoUpdate(boolean b) {doUpdate = b;}
    
    private void updateBuffer() {
        resetIdBuffer();
        for(int i = 0; i < X.length; i++) {
            Vector2f xScreen = PointUtils.getScreenPos(pointTransform.mult(X[i], null), cam);
            if(xScreen.getX() >= 0 && xScreen.getX() < idBuffer.length && xScreen.getY() >= 0 && xScreen.getY() < idBuffer[0].length) {
                idBuffer[(int)xScreen.getX()][(int)xScreen.getY()] = i;
            }
        }
    }
    
    private void resetIdBuffer() {
        for(int i = 0; i < idBuffer.length; i++){
            for(int j = 0; j < idBuffer[i].length; j++) {
                idBuffer[i][j] = -1;
            }
        }
    }
}
