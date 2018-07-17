package mygame.ml;

import com.jme3.math.Vector3f;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import mygame.data.search.KDTree;
import mygame.graph.FullGraph;
import mygame.graph.Graph;
import mygame.graph.SparseGraph;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import mygame.util.SegmenterUtils;
import mygame.volumetrics.CloudNormal;
import org.jblas.DoubleMatrix;


public class CurvatureSimilarityGraphConstructor {
    
    
    public static SparseGraph constructNoCentroidSparsePCASimilarityGraph(Vector3f[] X, KDTree kdTree, 
            SimilarityMetric<Vector3f> normalSimMetric, int nPCAPoints, int nNodeChildren) {
        Vector3f[] normals = JblasJMEConverter.toVector3f(CloudNormal.getUnorientedPCANormals(JblasJMEConverter.toDoubleMatrix(X), kdTree, nPCAPoints));
        SparseGraph out = new SparseGraph(X.length);
        for(int i = 0; i < X.length; i++) {
            int[] iNearestNeighbors = kdTree.getNearestNeighborIds(JblasJMEConverter.toArr(X[i])[0], nNodeChildren);
            for(int connectId : iNearestNeighbors) {
                double normalSim = normalSimMetric.similarityBetween(normals[i], normals[connectId]);
                out.link(i, connectId, normalSim);
                out.link(connectId, i, normalSim);
            }
        }
        return out;
    }
    
    
    public static SparseGraph constructSparsePCASimilarityGraph (Vector3f[] X, KDTree kdTree, SimilarityMetric<Vector3f> normalSimMetric,
            Map<Integer, Integer> idToClusterMap, Vector3f[] centroids) {
        //all points in a cluster are sparsely connected with similarity 1 (max similarity). (each point in a cluster has one child)
        //one point (closest to centroid of the cluster) is attached to the closest to centroid of all other clusters, 
        //with their normal similarity as edge weights
        
        
        SparseGraph out = new SparseGraph(X.length);
        
        Set<Integer>[] clusterSetIds = SegmenterUtils.convertIntoClusterSets(idToClusterMap);
        
        //sets all within-cluster connections to be sparse (one child per node) and 1
        for(Set<Integer> idCluster : clusterSetIds) {
            sparseConnectCluster(out, idCluster, 1.0);
        }
        
        
        Vector3f[] clusterNormals = getClusterNormals(X, idToClusterMap, centroids.length);
        int[] nearestIdToCentroids = new int[centroids.length];
        for(int i = 0; i < nearestIdToCentroids.length; i++) {
            nearestIdToCentroids[i] = kdTree.getNearestNeighborId(JblasJMEConverter.toArr(centroids[i])[0]);
        }
        for(int i = 0; i < nearestIdToCentroids.length; i++) {
            for(int j = 0; j < i; j++) {
                double normalSim = normalSimMetric.similarityBetween(clusterNormals[i], clusterNormals[j]);
                out.link(nearestIdToCentroids[i], nearestIdToCentroids[j], normalSim);
                out.link(nearestIdToCentroids[j], nearestIdToCentroids[i], normalSim);
            }
        }
        
        return out;
    }
    
    public static SparseGraph constructSuperSparsePCASimilarityGraph(Vector3f[] X, KDTree kdTree, SimilarityMetric<Vector3f> normalSimMetric,
            Map<Integer, Integer> idToClusterMap, Vector3f[] centroids, int nCentroidNeighbors) {
        
        //uses centroids and cluster subsets to map all points in a subset to that cluster's normal. 
        //sparsely connects all points in a subset
        //connects nearest neighbor to centroids together, iff they are suitably close to each other
        
        KDTree centroidKDTree = new KDTree(JblasJMEConverter.toArr(centroids));
        SparseGraph out = new SparseGraph(X.length);
        Set<Integer>[] clusterSetIds = SegmenterUtils.convertIntoClusterSets(idToClusterMap);
        
        //sets all within-cluster connections to be sparse (one child per node) and 1
        for(Set<Integer> idCluster : clusterSetIds) {
            sparseConnectCluster(out, idCluster, 1.0);
        }
        
       
        Vector3f[] clusterNormals = getClusterNormals(X, idToClusterMap, centroids.length);//cluster normals basically constant no matter the number of clusters, apparently
        
        int[] nearestIdToCentroids = new int[centroids.length];
        
        for(int i = 0; i < nearestIdToCentroids.length; i++) {
            nearestIdToCentroids[i] = kdTree.getNearestNeighborId(JblasJMEConverter.toArr(centroids[i])[0]);
        }
        
        for(int i = 0; i < nearestIdToCentroids.length; i++) {
            int[] nearestCentroidNeighborIds = centroidKDTree.getNearestNeighborIds(
                    JblasJMEConverter.toArr(centroids[i])[0], nCentroidNeighbors);
            for(int clusterId : nearestCentroidNeighborIds) {
                //note, clusterId will == i when iterating
                double normalSim = normalSimMetric.similarityBetween(clusterNormals[i], clusterNormals[clusterId]);
                out.link(nearestIdToCentroids[i], nearestIdToCentroids[clusterId], normalSim);
                out.link(nearestIdToCentroids[clusterId], nearestIdToCentroids[i], normalSim);
            }
        }
        return out;
        
    }
    
    private static void sparseConnectCluster(Graph graph, Set<Integer> idCluster, double value) {
        Integer[] idClusterArr = idCluster.toArray(new Integer[idCluster.size()]);
        for(int i = 1; i < idClusterArr.length; i++) {
            graph.link(idClusterArr[i-1], idClusterArr[i], value);
            graph.link(idClusterArr[i], idClusterArr[i-1], value);
        }
        graph.link(idClusterArr[idClusterArr.length-1], idClusterArr[0], value);
        graph.link(idClusterArr[0], idClusterArr[idClusterArr.length-1], value);
    }
    
    public static FullGraph constructPCASimilarityGraph(DoubleMatrix X, KDTree kdTree, SimilarityMetric normalSimMetric, int nNeighbors) {
        return GraphUtil.constructFullSimilarityGraph(JblasJMEConverter.toVector3f(CloudNormal.getUnorientedPCANormals(X, kdTree, nNeighbors)), normalSimMetric);
    }
    
    private static Vector3f[] getClusterNormals(Vector3f[] X, Map<Integer, Integer> idToClusterMap, int nCentroids) {
        Set<Vector3f>[] clusterVecSets = SegmenterUtils.convertIntoClusterVectorSets(X, idToClusterMap, nCentroids);
        Vector3f[] clusterNormals = new Vector3f[nCentroids];
        for(int i = 0; i < clusterNormals.length; i++) {
            DoubleMatrix jblasClusterINormal = JblasPCA.getPrincipalComponents(
            JblasJMEConverter.toDoubleMatrix(clusterVecSets[i].toArray(new Vector3f[clusterVecSets[i].size()])))[0].getColumn(0);
            clusterNormals[i] = JblasJMEConverter.toVector3f(jblasClusterINormal)[0];
        }
        return clusterNormals;
    }
}
