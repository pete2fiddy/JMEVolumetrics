


package mygame.model.volumetrics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.graph.Graph;
import mygame.model.graph.GraphEdge;
import mygame.model.graph.SparseGraph;
import mygame.model.graph.SymmetricGraph;
import mygame.model.data.ml.CurvatureSimilarityGraphConstructor;
import mygame.model.data.ml.JblasPCA;
import mygame.model.graph.algo.PrimsMinSpan;
import mygame.util.BoxUtil;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import mygame.util.PointUtil;
import org.jblas.DoubleMatrix;

public class CloudNormal {
    
    public static DoubleMatrix getUnorientedPCANormals(DoubleMatrix X, NearestNeighborSearcher kdTree, int nNeighbors) {
        
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
    public static void hoppeOrientNormals(DoubleMatrix X, DoubleMatrix normals,  NearestNeighborSearcher kdTree, int kNeighborsRiemannian) {
        SymmetricGraph riemannian = CurvatureSimilarityGraphConstructor.constructRiemannianPCASimilarityGraph(X, normals, kdTree, kNeighborsRiemannian);
        
        
        int headId = flipHeadIndToFaceOutward(X, normals);
        //paper talks about how to choose a good head id, but using an arbitrary value for now
        Graph riemannianMinSpanTree = PrimsMinSpan.buildMST(riemannian, headId);
        setIndsToHoppeOrientNormalsWithRiemannianMinSpanTree(normals, riemannianMinSpanTree, headId, new HashSet<Integer>());
    }
    
    private static int flipHeadIndToFaceOutward(DoubleMatrix X, DoubleMatrix normals) {
        double[][] bbox = PointUtil.getPointBounds3d(X);
        int out = 0;
        for(int i = 0; i < X.rows; i++) {
            if(X.get(i, 0) > X.get(out, 0)) {
                out = i;
            }
        }
        if(BoxUtil.boxContains(bbox, X.getRow(out).add(normals.getRow(out)))) normals.putRow(out, normals.getRow(out).mul(-1));
        return out;
    }
    
    /*
    orients the normals and fills a set of integers of the faces that were oriented
    */
    public static void setIndsToHoppeOrientNormalsWithRiemannianMinSpanTree(DoubleMatrix normals, Graph riemannianMinSpanTree, int currentNodeId, Set<Integer> toOrient) {
        
        //then traverse its children in any order (doesn't matter because orientation of children ONLY depends on this node's
        //normal and the child normal. This node's normal does not change while traversing other children)
        DoubleMatrix currentNodeNormal = normals.getRow(currentNodeId);
        List<GraphEdge> currentNodeOutEdges = riemannianMinSpanTree.getOutEdges(currentNodeId);
        for(GraphEdge outEdge : currentNodeOutEdges) {
            double childDot = currentNodeNormal.dot(normals.getRow(outEdge.CHILD_ID));
            if(childDot < 0) {
                //flip normal of child
                normals.putRow(outEdge.CHILD_ID, normals.getRow(outEdge.CHILD_ID).mul(-1));
                toOrient.add(outEdge.CHILD_ID);
            }
            setIndsToHoppeOrientNormalsWithRiemannianMinSpanTree(normals, riemannianMinSpanTree, outEdge.CHILD_ID, toOrient);
        }
    }
    
}
