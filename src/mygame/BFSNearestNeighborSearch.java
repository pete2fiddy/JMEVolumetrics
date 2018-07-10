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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import mygame.util.ArrayUtil;
import mygame.util.PointUtil;


public class BFSNearestNeighborSearch implements Runnable {
    protected Camera cam;
    protected Vector3f[] X;
    private int[][] idBuffer;
    protected Matrix4f pointTransform = Matrix4f.IDENTITY;
    private volatile boolean doUpdate = true;
    
    
    public BFSNearestNeighborSearch(Camera cam, Vector3f[] X) {
        this.cam = cam;
        this.X = X;
        resetIdBuffer();
    }
    
    public Set<Integer> getNearestNeighborIds(int[] searchPoint, int depthToSearch) {
        if(!ArrayUtil.inBounds(idBuffer, searchPoint)) new HashSet<Integer>();
        LinkedList<int[]> searchPoints = new LinkedList<int[]>();
        searchPoints.add(searchPoint);
        HashSet<Integer> foundPoints = new HashSet<Integer>();
        boolean[][] visitedPixels = new boolean[idBuffer.length][idBuffer[0].length];
        getNearestNeighborIds(searchPoints, foundPoints, depthToSearch, visitedPixels);
        return foundPoints;
    }
    
    private void getNearestNeighborIds(List<int[]> searchPoints, Set<Integer> foundPoints, int depthRemaining, boolean[][] visitedPixels) {
        if(depthRemaining < 0 || searchPoints.size() <= 0) {// || !ArrayUtil.inBounds(idBuffer, searchPoint)) {
            return;
        }
        LinkedList<int[]> searchPointsNeighbors = new LinkedList<int[]>();
        for(int[] searchPoint : searchPoints) {
            visitedPixels[searchPoint[0]][searchPoint[1]] = true;
            if(idBuffer[searchPoint[0]][searchPoint[1]] != -1) {
                foundPoints.add(idBuffer[searchPoint[0]][searchPoint[1]]);
            }

            //add all 8 points to use euclidian instead of manhattan distance nearest neighbor
            int[] neighbor1 = {searchPoint[0]-1, searchPoint[1]};
            int[] neighbor2 = {searchPoint[0]+1, searchPoint[1]};
            int[] neighbor3 = {searchPoint[0], searchPoint[1]-1};
            int[] neighbor4 = {searchPoint[0], searchPoint[1]+1};
            if(ArrayUtil.inBounds(idBuffer, neighbor1) && !visitedPixels[neighbor1[0]][neighbor1[1]]) {
                searchPointsNeighbors.add(neighbor1);
            }
            if(ArrayUtil.inBounds(idBuffer, neighbor2) && !visitedPixels[neighbor2[0]][neighbor2[1]]) {
                searchPointsNeighbors.add(neighbor2);
            }
            if(ArrayUtil.inBounds(idBuffer, neighbor3) && !visitedPixels[neighbor3[0]][neighbor3[1]]) {
                searchPointsNeighbors.add(neighbor3);
            }
            if(ArrayUtil.inBounds(idBuffer, neighbor4) && !visitedPixels[neighbor4[0]][neighbor4[1]]) {
                searchPointsNeighbors.add(neighbor4);
            }
        }
        getNearestNeighborIds(searchPointsNeighbors, foundPoints, depthRemaining-1, visitedPixels);
    }
    
    private void resetIdBuffer() {
        idBuffer = new int[cam.getWidth()][cam.getHeight()];
        ArrayUtil.fill2d(idBuffer, -1);
        
    }
    
    private void updateIdBuffer() {
        resetIdBuffer();
        for(int i = 0; i < X.length; i++) {
            Vector3f xWorldTransformed = pointTransform.mult(X[i], null);
            Vector3f xScreenPos = cam.getScreenCoordinates(xWorldTransformed);
            assert(xScreenPos.getZ() == cam.distanceToNearPlane(xWorldTransformed));
            int pixelX = (int)xScreenPos.getX();
            int pixelY = (int)xScreenPos.getY();
            if(ArrayUtil.inBounds(idBuffer, pixelX, pixelY)) {
                if(idBuffer[pixelX][pixelY] == -1) {
                    idBuffer[pixelX][pixelY] = i;
                } else {
                    float samePixelPointDepth = cam.distanceToNearPlane(pointTransform.mult(X[idBuffer[pixelX][pixelY]], null));
                    if(xScreenPos.getZ() < samePixelPointDepth) {
                        idBuffer[pixelX][pixelY] = i;
                    }
                }
            }
        }
    }
    
    public void setDoUpdate(boolean b) {doUpdate = b;}
    public void setTransform(Matrix4f pointTransform) {this.pointTransform = pointTransform;}
    
    @Override
    public void run() {
        while(doUpdate) {
            updateIdBuffer();
        }
    }
}
