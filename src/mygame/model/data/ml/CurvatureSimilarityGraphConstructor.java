package mygame.model.data.ml;

import mygame.model.data.ml.similarity.SimilarityMetric;
import com.jme3.math.Vector3f;
import java.util.Map;
import java.util.Set;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.graph.FullGraph;
import mygame.model.graph.Graph;
import mygame.model.graph.SparseGraph;
import mygame.model.graph.SymmetricGraph;
import mygame.model.data.ml.similarity.jblas.JblasRiemannianCost;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import mygame.util.SegmenterUtils;
import mygame.model.volumetrics.CloudNormal;
import org.jblas.DoubleMatrix;


public class CurvatureSimilarityGraphConstructor {
    //no reason why graphs should only be constructed with 3d points, so all point inputs are DoubleMatrix
    
    //should these all be passed in the normals rather than calculating them here? Can think of cases where the normals would have to be redundantly calculated
    //twice otherwise
    
    
    public static SymmetricGraph constructFullPCASimilarityGraph(DoubleMatrix X, DoubleMatrix normals, NearestNeighborSearcher kdTree, 
            SimilarityMetric<DoubleMatrix> normalSimMetric) {
        
        SymmetricGraph out = new SymmetricGraph(new FullGraph(X.rows));
        for(int i = 0; i < normals.rows; i++) {
            for(int j = 0; j < i; j++) {
                double normalSim = normalSimMetric.similarityBetween(normals.getRow(i), normals.getRow(j));
                out.link(i, j, normalSim);
            }
        }
        return out;
    }
    
    //constructs the similarity graph in a similar style to a Riemannian graph (http://hhoppe.com/recon.pdf), but the normal similarity metric is
    //up to the discretion of the caller
    public static SymmetricGraph constructSparsePCASimilarityGraph(DoubleMatrix X, DoubleMatrix normals, NearestNeighborSearcher kdTree,
            SimilarityMetric<DoubleMatrix> normalSimMetric, int nNodeChildren) {
        SymmetricGraph out = new SymmetricGraph(new SparseGraph(X.rows));
        for(int id1 = 0; id1 < normals.rows; id1++) {
            int[] id1NearestNeighborIds = kdTree.getNearestNeighborIds(X.getRow(id1).toArray(), nNodeChildren);
            for(int id2 : id1NearestNeighborIds) {
                double normalSim = normalSimMetric.similarityBetween(normals.getRow(id1), normals.getRow(id2));
                out.link(id1, id2, normalSim);
            }
        }
        return out;
    }
    
    public static SymmetricGraph constructRiemannianPCASimilarityGraph(DoubleMatrix X, DoubleMatrix normals, NearestNeighborSearcher kdTree,
            int nNodeChildren) {
        SimilarityMetric<DoubleMatrix> simMetric = new JblasRiemannianCost();
        SymmetricGraph out = new SymmetricGraph(new SparseGraph(X.rows));
        for(int id = 0; id < X.rows; id++) {
            int[] idNearestNeighbors = kdTree.getNearestNeighborIds(X.getRow(id).toArray(), nNodeChildren);
            for(int id2 : idNearestNeighbors) {
                double sim = simMetric.similarityBetween(normals.getRow(id), normals.getRow(id2));
                out.link(id, id2, sim);
            }
        }
        return out;
    }
}
