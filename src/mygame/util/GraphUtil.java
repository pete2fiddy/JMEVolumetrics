/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.util;

import com.jme3.math.Vector3f;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.data.search.KDTree;
import mygame.graph.FullGraph;
import mygame.graph.Graph;
import mygame.graph.GraphEdge;
import mygame.graph.SparseGraph;
import mygame.graph.GraphNode;
import mygame.ml.SimilarityMetric;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class GraphUtil {
    
    public static <DataType> FullGraph constructFullSimilarityGraph(DataType[] X, SimilarityMetric<DataType> metric) {
        FullGraph out = new FullGraph(X.length);
        for(int i = 0; i < X.length; i++) {
            for(int j = 0; j < i; j++) {
                double similarity = metric.similarityBetween(X[i], X[j]);
                if(similarity > 0) {
                    out.link(i, j, similarity);
                    out.link(j, i, similarity);
                }
            }
        }
        return out;
    }
    
    /*
    constructs a graph where two nodes are connected only if they have a thresholded similarity (using thresholdedSimilarityMetric) 
    > threshold.
    */
    public static <DataType> SparseGraph constructSparseSimilarityGraph(DataType[] X, SimilarityMetric<DataType> graphSimilarityMetric, 
            SimilarityMetric<DataType> thresholdedSimilarityMetric, double threshold) {
        SparseGraph out = new SparseGraph(X.length);
        for(int i = 0; i < X.length; i++) {
            for(int j = 0; j < i; j++) {
                double threshSim = thresholdedSimilarityMetric.similarityBetween(X[i], X[j]);
                if(threshSim > threshold) {
                    double edgeWeight = graphSimilarityMetric.similarityBetween(X[i], X[j]);
                    out.link(i, j, edgeWeight);
                    out.link(j, i, edgeWeight);
                }
            }
        }
        return out;
    }
    
    public static SparseGraph constructSparseSimilarityGraph(Vector3f[] X, SimilarityMetric<Vector3f> graphSimilarityMetric,
            KDTree kdTree, int sparseNNeighbors) {
        DoubleMatrix XMat = JblasJMEConverter.toDoubleMatrix(X);
        SparseGraph out = new SparseGraph(X.length);
        for(int i = 0; i < X.length; i++) {
            int[] nearestNeighborsOfI = kdTree.getNearestNeighborIds(XMat.getRow(i).toArray(), sparseNNeighbors);
            for(int id : nearestNeighborsOfI) {
                out.link(i, id, graphSimilarityMetric.similarityBetween(X[i], X[id]));
            }
        }
        return out;
    }
    
    
    public static SparseGraph constructSparseSimilarityGraph(Vector3f[] X, SimilarityMetric<Vector3f> graphSimilarityMetric, 
            KDTree kdTree, double maxRadius) {
        DoubleMatrix XMat = JblasJMEConverter.toDoubleMatrix(X);
        SparseGraph out = new SparseGraph(X.length);
        int nConnections = 0;
        for(int i = 0; i < X.length; i++) {
            Set<Integer> withinRadiusOfI = kdTree.getIdsWithinRadius(XMat.getRow(i).toArray(), maxRadius);
            for(int withinRadiusId : withinRadiusOfI) {
                out.link(i, withinRadiusId, graphSimilarityMetric.similarityBetween(X[i], X[withinRadiusId]));
                nConnections ++;
            }
        }
        System.out.println("n connections: " + nConnections);
        return out;
    }
    
    
    public static Set<Integer> thresholdedFloodfill(Graph graph, int startId, double threshold) {
        HashSet<Integer> out = new HashSet<Integer>();
        thresholdFloodfill(graph, startId, threshold, new HashSet<Integer>(), out);
        return out;
    }
    
    private static void thresholdFloodfill(Graph graph, int startId, double threshold, Set<Integer> visited, Set<Integer> out) {
        visited.add(startId);
        out.add(startId);
        List<GraphEdge> outEdges = graph.getOutEdges(startId);
        
        for(GraphEdge outEdge : outEdges) {
            if(outEdge.WEIGHT > threshold && !visited.contains(outEdge.CHILD_ID)) {
                thresholdFloodfill(graph, outEdge.CHILD_ID, threshold, visited, out);
            }
        }
    }
}
