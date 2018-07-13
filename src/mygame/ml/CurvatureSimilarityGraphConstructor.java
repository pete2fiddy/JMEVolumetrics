package mygame.ml;

import com.jme3.math.Vector3f;
import java.util.Set;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import org.jblas.DoubleMatrix;


public class CurvatureSimilarityGraphConstructor {
    
    public static DoubleMatrix constructDistanceWeightedPCASimilarityGraph(Vector3f[] X, Vector3f[] centroids, Set<Integer>[] idClusters, SimilarityMetric normalSimilarityMetric, SimilarityMetric distanceMetric) {
        return constructPCASimilarityGraph(X, idClusters, normalSimilarityMetric).mul(GraphUtil.constructSimilarityGraph(centroids, distanceMetric));
    }
    
    
    public static DoubleMatrix constructPCASimilarityGraph(Vector3f[] X, Set<Integer>[] idClusters, SimilarityMetric simMetric) {
        return constructPCASimilarityGraph(JblasJMEConverter.toDoubleMatrix(X), idClusters, simMetric);
    }
    
    public static DoubleMatrix constructPCASimilarityGraph(DoubleMatrix X, Set<Integer>[] idClusters, SimilarityMetric simMetric) {
        //need to throw error if don'th ave at least 3 points?? (fewer will not have enough info to find the min variance axis)
        DoubleMatrix[] clusters = new DoubleMatrix[idClusters.length];
        for(int i = 0; i < clusters.length; i++) {
            DoubleMatrix clusterI = DoubleMatrix.zeros(idClusters[i].size(), X.columns);
            int j = 0;
            for(int id : idClusters[i]) {
                clusterI.putRow(j++, X.getRow(id));
            }
            clusters[i] = clusterI;
        }
        DoubleMatrix[] clusterNormals = new DoubleMatrix[clusters.length];
        for(int i = 0; i < clusterNormals.length; i++) {
            DoubleMatrix[] clusterPCA = JblasPCA.getPrincipalComponents(clusters[i]);
            clusterNormals[i] = clusterPCA[0].getColumn(0);
        }
        return GraphUtil.constructSimilarityGraph(clusterNormals, simMetric);
    }
}
