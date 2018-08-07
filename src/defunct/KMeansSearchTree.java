/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package defunct;

import com.jme3.math.Matrix4f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import mygame.model.data.ml.JMEKMeans;
import mygame.model.data.ml.KMeans;
import mygame.util.PointUtil;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
//not fully implemented because found that splitting between DoubleMatrix and node is kind of odd
public class KMeansSearchTree {
    //getting ids through this method has possible issues: if a vector is edited in-place, it's hash 
    //code will change and an incorrect id could be returned
    private HashMap<Vector3f, Integer> vecToIdMap = new HashMap<Vector3f, Integer>();
    private Vector3f[] X;
    private KMeansTreeNode head;
    
    public KMeansSearchTree(Vector3f[] X) {
        this.X = X;
        for(int i = 0; i < X.length; i++) {
            vecToIdMap.put(X[i], i);
        }
    }
    
    
    /*
    the greater the number of centroids per kmeans, the faster the runtime, but possibly less precise
    the greader the depth, the faster the runtime, but the less precise (more border conditions where
    results are not ideal)
    */
    public void fit(int depth, int centroidsPerKMeans, int itersPerKMeans) {
        head = new KMeansTreeNode();
        head.fit(X, depth, centroidsPerKMeans, itersPerKMeans);
    }
    
    public Vector3f getNearestNeighbor(Vector3f x) {return getNearestNeighbor(x, Matrix4f.IDENTITY);}
    public Vector3f getNearestNeighbor(Vector3f x, Matrix4f treeTransformation) {return head.getNearestNeighbor(treeTransformation.invert().mult(x, null));}
    public int getNearestNeighborId(Vector3f x){return getNearestNeighborId(x, Matrix4f.IDENTITY);}
    public int getNearestNeighborId(Vector3f x, Matrix4f treeTransformation) {return vecToIdMap.get(getNearestNeighbor(x, treeTransformation));}
    
    
    public Vector3f getNearestScreenNeighbor(Vector2f x, Matrix4f treeTransformation, Camera cam){
        return head.getNearestScreenNeighbor(x, treeTransformation, cam);
    }
    public int getNearestScreenNeighborId(Vector2f x, Matrix4f treeTransformation, Camera cam) {
        return vecToIdMap.get(getNearestScreenNeighbor(x, treeTransformation, cam));
    }
    
    private class KMeansTreeNode {
        private Vector3f[] childCentroids;
        private KMeansTreeNode[] children;
        private Vector3f[] leafPoints;
        
        public KMeansTreeNode(){}
        
        
        /* assumes pretransformedPoint has the inverse transformation as is applied to 
        the points in the tree applied already. Just multiply by identity if no transformation has occurred to
        the points in the tree
        */
        public Vector3f getNearestNeighbor(Vector3f pretransformedPoint) {
            if(isLeaf()) {
                int nearestLeafPointId = PointUtil.getNearestNeighborId(leafPoints, pretransformedPoint);
                return leafPoints[nearestLeafPointId];
            }
            int nearestCentroidId = PointUtil.getNearestNeighborId(childCentroids, pretransformedPoint);
            return children[nearestCentroidId].getNearestNeighbor(pretransformedPoint);
        }
        
        public Vector3f getNearestScreenNeighbor(Vector2f point, Matrix4f treePretransformMat, Camera cam) {
            if(isLeaf()) {
                int nearestLeafPointId = PointUtil.getNearestScreenNeighborId(leafPoints, point, 
                        treePretransformMat, cam);
                return leafPoints[nearestLeafPointId];
            }
            int nearestCentroidId = PointUtil.getNearestScreenNeighborId(childCentroids, point,
                    treePretransformMat, cam);
            return children[nearestCentroidId].getNearestScreenNeighbor(point, treePretransformMat, cam);
        }
        
        protected boolean isLeaf() {return children == null;}
        
        public void fit(Vector3f[] X, int depthRemaining, int centroidsPerKMeans, int itersPerKMeans) {
            if(depthRemaining <= 0 || X.length < centroidsPerKMeans) {
                //is leaf
                leafPoints = X;
                return;
            }
            Vector3f[] tempChildCentroids = JMEKMeans.calcKMeansCentroids(X, centroidsPerKMeans, itersPerKMeans);
            System.out.println("child centroids: " + Arrays.toString(tempChildCentroids));
            LinkedList<Vector3f>[] childClusters = JMEKMeans.assignToClusters(tempChildCentroids, X);
            
            ArrayList<Integer> nonEmptyClusterIds = new ArrayList<Integer>();
            for(int i = 0; i < childClusters.length; i++){
                if(childClusters[i].size()>0) nonEmptyClusterIds.add(i);
            }
            childCentroids = new Vector3f[nonEmptyClusterIds.size()];
            children = new KMeansTreeNode[nonEmptyClusterIds.size()];
            for(int i = 0; i < children.length; i++) {
                childCentroids[i] = tempChildCentroids[nonEmptyClusterIds.get(i)];
                children[i] = new KMeansTreeNode();
                children[i].fit(childClusters[nonEmptyClusterIds.get(i)].toArray(new Vector3f[childClusters[nonEmptyClusterIds.get(i)].size()]),
                        depthRemaining - 1, centroidsPerKMeans, itersPerKMeans);
            }
            
        }
    }
    
}
