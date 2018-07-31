/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.volumetrics;

import java.util.Arrays;
import java.util.List;
import mygame.data.search.KDTree;
import mygame.graph.Graph;
import mygame.graph.GraphEdge;
import mygame.graph.SparseGraph;
import mygame.graph.SymmetricGraph;
import mygame.ml.CurvatureSimilarityGraphConstructor;
import mygame.ml.JblasPCA;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class CloudNormal {
    /*
    unoriented cloud normals calculated as follows:
    
    dataset of DoubleMatrix, X, fed in.
    for each point, the nearest 2 points are found (put X into a KDTree, don't worry about transforming it, since is not necessary).
    normal axis is found by cross product is found between the two found points subtracted by the original point
    
    or:
    
    dataset of DoubleMatrix, X, fed in
    for each point, the nearest N points (or all points within a radius) are found (put X into KDTree, don't worry about transforming it, since is not necessary).
    axis of miniminum variance is found by PCA on the near points
    
    
    use graph traversal + cosine angle between to flip neighboring normals (flip in if cosine angle is negative -- if magnitude of cosine
    angle is small, means it is unknown if is good flip)
    
    possibly perform process multiple times and choose the result that is most consistent across all attempts
    */
    
    /*
    
    try to change to weighted PCA
    */
    public static DoubleMatrix getUnorientedPCANormals(DoubleMatrix X, KDTree kdTree, int nNeighbors) {
        
        DoubleMatrix normals = DoubleMatrix.zeros(X.rows, X.columns);
        for(int i = 0; i < X.rows; i++) {
            int[] nearestNeighborIds = kdTree.getNearestNeighborIds(X.getRow(i).toArray(), nNeighbors);
            DoubleMatrix neighborVecs = X.getRows(nearestNeighborIds);
            normals.putRow(i, JblasPCA.getPrincipalComponents(neighborVecs)[0].getColumn(0));
        }
        return normals;
    }
    
    /*
    orients normals using this process: http://hhoppe.com/recon.pdf.
    
    If the minimum spanning tree process fails due to having more than 1 connected component, try making kNeibhorsRiemannian larger
    */
    public static void hoppeOrientNormals(DoubleMatrix X, DoubleMatrix normals,  KDTree kdTree, int kNeighborsRiemannian) {
        SymmetricGraph riemannian = CurvatureSimilarityGraphConstructor.constructRiemannianPCASimilarityGraph(X, normals, kdTree, kNeighborsRiemannian);
        int headId = 0;
        //paper talks about how to choose a good head id, but using an arbitrary value for now
        Graph riemannianMinSpanTree = GraphUtil.primsMinimumSpanningTree(riemannian, headId);
        hoppeOrientNormalsWithRiemannianMinSpanTree(normals, riemannianMinSpanTree, headId);
    }
    
    private static void hoppeOrientNormalsWithRiemannianMinSpanTree(DoubleMatrix normals, Graph riemannianMinSpanTree, int currentNodeId) {
        
        //then traverse its children in any order (doesn't matter because orientation of children ONLY depends on this node's
        //normal and the child normal. This node's normal does not change while traversing other children)
        DoubleMatrix currentNodeNormal = normals.getRow(currentNodeId);
        List<GraphEdge> currentNodeOutEdges = riemannianMinSpanTree.getOutEdges(currentNodeId);
        for(GraphEdge outEdge : currentNodeOutEdges) {
            double childDot = currentNodeNormal.dot(normals.getRow(outEdge.CHILD_ID));
            if(childDot < 0) {
                //flip normal of child
                normals.putRow(outEdge.CHILD_ID, normals.getRow(outEdge.CHILD_ID).mul(-1));
            }
            hoppeOrientNormalsWithRiemannianMinSpanTree(normals, riemannianMinSpanTree, outEdge.CHILD_ID);
        }
    }
    
}
