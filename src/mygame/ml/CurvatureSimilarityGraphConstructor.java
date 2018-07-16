package mygame.ml;

import com.jme3.math.Vector3f;
import java.util.Set;
import mygame.data.search.JblasKDTree;
import mygame.graph.FullGraph;
import mygame.graph.SparseGraph;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import org.jblas.DoubleMatrix;


public class CurvatureSimilarityGraphConstructor {
    
    /*
    public static DoubleMatrix constructDistanceWeightedPCASimilarityGraph(Vector3f[] X, Vector3f[] centroids, Set<Integer>[] idClusters, SimilarityMetric normalSimilarityMetric, SimilarityMetric distanceMetric) {
        return constructPCASimilarityGraph(X, idClusters, normalSimilarityMetric).mul(GraphUtil.constructSimilarityGraph(centroids, distanceMetric));
    }
    
    
    public static DoubleMatrix constructPCASimilarityGraph(Vector3f[] X, Set<Integer>[] idClusters, SimilarityMetric simMetric) {
        return constructPCASimilarityGraph(JblasJMEConverter.toDoubleMatrix(X), idClusters, simMetric);
    }
    */
    
    public static SparseGraph constructSparsePCASimilarityGraph(DoubleMatrix X, SimilarityMetric normalSimMetric,
            int nNeighbors, JblasKDTree kdTree, double maxRadius) {
        SparseGraph out = new SparseGraph(X.rows);
        DoubleMatrix[] normals = getNormals(X, kdTree, nNeighbors);
        int nConnections = 0;
        for(int i = 0; i < X.rows; i++) {
            Set<Integer> withinRadius = kdTree.getIdsWithinRadius(X.getRow(i), maxRadius);
            for(int radiusId : withinRadius) {
                out.link(i, radiusId, normalSimMetric.similarityBetween(normals[i], normals[radiusId]));
                nConnections++;
            }
        }
        System.out.println("N CONNECTIONS: " + nConnections);
        return out;
    }
    
    public static SparseGraph constructSparsePCASimilarityGraph(DoubleMatrix X, SimilarityMetric normalSimMetric, 
            int pcaNNeighbors, JblasKDTree kdTree, int sparseNNeighbors) {
        SparseGraph out = new SparseGraph(X.rows);
        DoubleMatrix[] normals = getNormals(X, kdTree, pcaNNeighbors);
        for(int i = 0; i < X.rows; i++) {
            int[] nearestNeighbors = kdTree.getNearestNeighborIds(X.getRow(i), sparseNNeighbors);
            for(int id : nearestNeighbors) {
                out.link(i, id, normalSimMetric.similarityBetween(normals[i], normals[id]));
            }
        }
        return out;
    }
    
    public static FullGraph constructPCASimilarityGraph(DoubleMatrix X, JblasKDTree kdTree, SimilarityMetric normalSimMetric, int nNeighbors) {
        return GraphUtil.constructFullSimilarityGraph(getNormals(X, kdTree, nNeighbors), normalSimMetric);
    }
    
    private static DoubleMatrix[] getNormals(DoubleMatrix X, JblasKDTree kdTree, int nNeighbors) {
        DoubleMatrix[] normals = new DoubleMatrix[X.rows];
        for(int i = 0; i < X.rows; i++) {
            int[] nearestNeighborIds = kdTree.getNearestNeighborIds(X.getRow(i), nNeighbors);
            normals[i] = JblasPCA.getPrincipalComponents(X.getRows(nearestNeighborIds))[0].getColumn(0);
        }
        return normals;
    }
}
