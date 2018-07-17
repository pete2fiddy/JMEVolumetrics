package mygame.ml;

import com.jme3.math.Vector3f;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import mygame.data.search.JblasKDTree;
import mygame.graph.FullGraph;
import mygame.graph.Graph;
import mygame.graph.SparseGraph;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import mygame.util.SegmenterUtils;
import org.jblas.DoubleMatrix;


public class CurvatureSimilarityGraphConstructor {
    
    
    
    public static SparseGraph constructSparsePCASimilarityGraph (Vector3f[] X, JblasKDTree kdTree, SimilarityMetric<Vector3f> normalSimMetric,
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
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < nearestIdToCentroids.length; i++) {
            nearestIdToCentroids[i] = kdTree.getNearestNeighborId(JblasJMEConverter.toDoubleMatrix(centroids[i]));
        }
        System.out.println("Time profiled: " + Double.toString(System.currentTimeMillis() - startTime));
        for(int i = 0; i < nearestIdToCentroids.length; i++) {
            for(int j = 0; j < i; j++) {
                double normalSim = normalSimMetric.similarityBetween(clusterNormals[i], clusterNormals[j]);
                out.link(nearestIdToCentroids[i], nearestIdToCentroids[j], normalSim);
                out.link(nearestIdToCentroids[j], nearestIdToCentroids[i], normalSim);
            }
        }
        
        return out;
    }
    
    public static SparseGraph constructSuperSparsePCASimilarityGraph(Vector3f[] X, JblasKDTree kdTree, SimilarityMetric<Vector3f> normalSimMetric,
            Map<Integer, Integer> idToClusterMap, Vector3f[] centroids, int nCentroidNeighbors) {
        
        
         long startTime = System.nanoTime();
        
        JblasKDTree centroidKDTree = new JblasKDTree(JblasJMEConverter.toDoubleMatrix(centroids));
        SparseGraph out = new SparseGraph(X.length);
        Set<Integer>[] clusterSetIds = SegmenterUtils.convertIntoClusterSets(idToClusterMap);
        
        //sets all within-cluster connections to be sparse (one child per node) and 1
        for(Set<Integer> idCluster : clusterSetIds) {
            sparseConnectCluster(out, idCluster, 1.0);
        }
        
       
        Vector3f[] clusterNormals = getClusterNormals(X, idToClusterMap, centroids.length);//cluster normals basically constant no matter the number of clusters, apparently
        
        int[] nearestIdToCentroids = new int[centroids.length];
        
        for(int i = 0; i < nearestIdToCentroids.length; i++) {
            nearestIdToCentroids[i] = kdTree.getNearestNeighborId(JblasJMEConverter.toDoubleMatrix(centroids[i]));
        }
        
        for(int i = 0; i < nearestIdToCentroids.length; i++) {
            int[] nearestCentroidNeighborIds = centroidKDTree.getNearestNeighborIds(
                    JblasJMEConverter.toDoubleMatrix(centroids[i]), nCentroidNeighbors);
            for(int clusterId : nearestCentroidNeighborIds) {
                //note, clusterId will == i when iterating
                double normalSim = normalSimMetric.similarityBetween(clusterNormals[i], clusterNormals[clusterId]);
                out.link(nearestIdToCentroids[i], nearestIdToCentroids[clusterId], normalSim);
                out.link(nearestIdToCentroids[clusterId], nearestIdToCentroids[i], normalSim);
            }
        }
        System.out.println("profile time: " + Double.toString(((double)(System.nanoTime() - startTime)/1000000.0)));
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
    
    public static FullGraph constructPCASimilarityGraph(DoubleMatrix X, JblasKDTree kdTree, SimilarityMetric normalSimMetric, int nNeighbors) {
        return GraphUtil.constructFullSimilarityGraph(getNormals(X, kdTree, nNeighbors), normalSimMetric);
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
    
    private static DoubleMatrix[] getNormals(DoubleMatrix X, JblasKDTree kdTree, int nNeighbors) {
        DoubleMatrix[] normals = new DoubleMatrix[X.rows];
        for(int i = 0; i < X.rows; i++) {
            int[] nearestNeighborIds = kdTree.getNearestNeighborIds(X.getRow(i), nNeighbors);
            normals[i] = JblasPCA.getPrincipalComponents(X.getRows(nearestNeighborIds))[0].getColumn(0);
        }
        return normals;
    }
}
