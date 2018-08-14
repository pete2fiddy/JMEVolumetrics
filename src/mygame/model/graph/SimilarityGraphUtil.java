package mygame.model.graph;

import com.jme3.math.Vector3f;
import java.util.Set;
import mygame.model.data.ml.similarity.SimilarityMetric;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.util.JblasJMEConverter;
import org.jblas.DoubleMatrix;

public class SimilarityGraphUtil {
    
    public static <DataType> SymmetricGraph constructFullSimilarityGraph(DataType[] X, SimilarityMetric<DataType> metric) {
        SymmetricGraph out = new SymmetricGraph(new FullGraph(X.length));
        for(int i = 0; i < X.length; i++) {
            for(int j = 0; j < i; j++) {
                double similarity = metric.similarityBetween(X[i], X[j]);
                if(similarity > 0) {
                    out.link(i, j, similarity);
                }
            }
        }
        return out;
    }
    
    /*
    constructs a graph where two nodes are connected only if they have a thresholded similarity (using thresholdedSimilarityMetric) 
    > threshold.
    */
    public static <DataType> SymmetricGraph constructSparseSimilarityGraph(DataType[] X, SimilarityMetric<DataType> graphSimilarityMetric, 
            SimilarityMetric<DataType> thresholdedSimilarityMetric, double threshold) {
        SymmetricGraph out = new SymmetricGraph(new SparseGraph(X.length));
        for(int i = 0; i < X.length; i++) {
            for(int j = 0; j < i; j++) {
                double threshSim = thresholdedSimilarityMetric.similarityBetween(X[i], X[j]);
                if(threshSim > threshold) {
                    double edgeWeight = graphSimilarityMetric.similarityBetween(X[i], X[j]);
                    out.link(i, j, edgeWeight);
                }
            }
        }
        return out;
    }
    
    public static SymmetricGraph constructSparseSimilarityGraph(Vector3f[] X, SimilarityMetric<Vector3f> graphSimilarityMetric,
            NearestNeighborSearcher kdTree, int sparseNNeighbors) {
        DoubleMatrix XMat = JblasJMEConverter.toDoubleMatrix(X);
        SymmetricGraph out = new SymmetricGraph(new SparseGraph(X.length));
        for(int i = 0; i < X.length; i++) {
            int[] nearestNeighborsOfI = kdTree.getNearestNeighborIds(XMat.getRow(i).toArray(), sparseNNeighbors);
            for(int id : nearestNeighborsOfI) {
                out.link(i, id, graphSimilarityMetric.similarityBetween(X[i], X[id]));
            }
        }
        return out;
    }
    
    
    public static SymmetricGraph constructSparseSimilarityGraph(Vector3f[] X, SimilarityMetric<Vector3f> graphSimilarityMetric, 
            NearestNeighborSearcher kdTree, double maxRadius) {
        DoubleMatrix XMat = JblasJMEConverter.toDoubleMatrix(X);
        SymmetricGraph out = new SymmetricGraph(new SparseGraph(X.length));
        for(int i = 0; i < X.length; i++) {
            Set<Integer> withinRadiusOfI = kdTree.getIdsWithinRadius(XMat.getRow(i).toArray(), maxRadius);
            for(int withinRadiusId : withinRadiusOfI) {
                out.link(i, withinRadiusId, graphSimilarityMetric.similarityBetween(X[i], X[withinRadiusId]));
            }
        }
        return out;
    }
}
