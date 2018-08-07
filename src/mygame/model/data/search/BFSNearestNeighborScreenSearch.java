package mygame.model.data.search;

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


public class BFSNearestNeighborScreenSearch implements Runnable {
    protected Camera cam;
    protected Vector3f[] X;
    private volatile int[][] idBuffer;
    protected Matrix4f pointTransform = Matrix4f.IDENTITY;
    private volatile boolean doUpdate = true;
    
    
    public BFSNearestNeighborScreenSearch(Camera cam, Vector3f[] X) {
        this.cam = cam;
        this.X = X;
        updateIdBuffer();
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
            int idAtSearchPoint = idBuffer[searchPoint[0]][searchPoint[1]];
            if(idAtSearchPoint != -1) {
                foundPoints.add(idAtSearchPoint);
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
    
    
    private void updateIdBuffer() {
        int[][] tempIdBuffer = new int[cam.getWidth()][cam.getHeight()];
        ArrayUtil.fill2d(tempIdBuffer, -1);
        for(int i = 0; i < X.length; i++) {
            Vector3f xWorldTransformed = pointTransform.mult(X[i], null);
            Vector3f xScreenPos = cam.getScreenCoordinates(xWorldTransformed);
            int pixelX = (int)xScreenPos.getX();
            int pixelY = (int)xScreenPos.getY();
            if(ArrayUtil.inBounds(tempIdBuffer, pixelX, pixelY)) {
                if(tempIdBuffer[pixelX][pixelY] == -1) {
                    tempIdBuffer[pixelX][pixelY] = i;
                } else {
                    Vector3f compareScreenPos = cam.getScreenCoordinates(pointTransform.mult(X[tempIdBuffer[pixelX][pixelY]], null));
                    float samePixelPointDepth = compareScreenPos.getZ();
                    if(xScreenPos.getZ() < samePixelPointDepth) {
                        tempIdBuffer[pixelX][pixelY] = i;
                    }
                }
            }
        }
        idBuffer = tempIdBuffer;
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
