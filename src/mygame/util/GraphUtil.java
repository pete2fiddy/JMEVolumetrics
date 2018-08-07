package mygame.util;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.graph.FullGraph;
import mygame.model.graph.Graph;
import mygame.model.graph.GraphEdge;
import mygame.model.graph.SparseGraph;
import mygame.model.graph.GraphNode;
import mygame.model.graph.SymmetricGraph;
import mygame.model.data.ml.similarity.SimilarityMetric;
import org.jblas.DoubleMatrix;


public class GraphUtil {
    
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
    
    /*
    if source is not symmetric and clone is, the process will FORCE a symmetric version of source. Which edge direction is basically in order of the largest id of parent node (
    if an edge connects node 10 to node 20, the edge from 20 to 10 will be chosen in the symmetric clone, connecting 10 and 20 to each other)
    */
    public static void cloneInto(Graph source, Graph clone) {
        for(int nodeId = 0; nodeId < source.numNodes(); nodeId++) {
            List<GraphEdge> idEdges = source.getOutEdges(nodeId);
            for(GraphEdge edge : idEdges) {
                clone.link(nodeId, edge.CHILD_ID, edge.WEIGHT);
            }
        }
    }
    
    
    //assumes graph has 1 connected component AND is undirected (if i and j are linked, j and i must be linked)
    public static SparseGraph primsMinimumSpanningTree(SymmetricGraph graph, int headId) {
        HashSet<Integer> cComponent = new HashSet<Integer>();
        SparseGraph tree = new SparseGraph(graph.numNodes());
        cComponent.add(headId);
        SymmetricGraph graphClone = new SymmetricGraph(new SparseGraph(graph.numNodes()));
        cloneInto(graph, graphClone);
        constructPrimsMinSpanningTree(graphClone, tree, cComponent);
        return tree;
    }
    
    //assumes graphClone is one connected component when initially passed
    private static void constructPrimsMinSpanningTree(SymmetricGraph graphClone, Graph tree, Set<Integer> connectedComponent) {
        if(connectedComponent.size() >= graphClone.numNodes()) {
            return;
        }
        int minParent = -1;
        GraphEdge minEdge = null;
        
        for(int nodeId : connectedComponent) {
            List<GraphEdge> idEdges = graphClone.getOutEdges(nodeId);
            for(GraphEdge idEdge : idEdges) {
                //ensures that doesn't form a cycle, and that new edge is of smaller weight than previously found minimum
                if(!connectedComponent.contains(idEdge.CHILD_ID)) {
                    if(minEdge == null || idEdge.WEIGHT < minEdge.WEIGHT) {
                        minParent = nodeId;
                        minEdge = idEdge;
                    }
                } else {
                    //remove the edge from graphClone since it forms a cycle with the connected component
                    graphClone.unlink(nodeId, idEdge.CHILD_ID);
                }
            }
        }
        if(minParent == -1) {
            throw new IllegalArgumentException("Graph passed to primsMinimumSpanningTree likely contains more than only 1 connected component. "
                    + " Minimum spanning tree cannot be constructed!");
        }
        tree.link(minParent, minEdge.CHILD_ID, minEdge.WEIGHT);
        connectedComponent.add(minEdge.CHILD_ID);
        constructPrimsMinSpanningTree(graphClone, tree, connectedComponent);
    }
    
    public static String toString(Graph g) {
        String out = "";
        for(int i = 0; i < g.numNodes(); i++) {
            out += "Node " + Integer.toString(i) + ": [";
            List<GraphEdge> iEdges = g.getOutEdges(i);
            for(GraphEdge edge : iEdges) {
                out += edge.toString() + ", ";
            }
            out += "]\n";
        }
        return out;
    }
    
    /*
    NOTE: ALTERS disposableGraph in the process (removes N-Cycles as it finds them). If you wish to keep the graph intact, use cloneInto().
    This was done to prevent the function having to make an assumption about the ideal type of graph object to use when cloning the graph.
    Also, the user may not care about keeping the graph intact, in which case, wasting resources on cloning the graph is unnecessary.
    
    Cycles are removed from the graph as they are found to 1) speed up the algorithm, and 2) prevent identical cycles from being added to the output,
    but with different starting nodes.
    */
    public static List<GraphEdge[]> getAllUniqueNEdgeCycles(Graph disposableGraph, int n) {
        //not tested yet
        List<GraphEdge[]> cycles = new ArrayList<GraphEdge[]>();
        for(int id = 0; id < disposableGraph.numNodes(); id++) {
            List<GraphEdge[]> addCycles = getAllNEdgePathsFromNodeToNode(disposableGraph, n, id, id);
            for(GraphEdge[] addCycle : addCycles) {
                cycles.add(addCycle);
                for(int cycleId = 0; cycleId < addCycle.length; cycleId++) {
                    disposableGraph.unlink(cycleId, (cycleId+1)%addCycle.length);
                }
            }
        }
        return cycles;
    }
    
    public static List<GraphEdge[]> getAllNEdgePathsFromNodeToNode(Graph g, int n, int startNode, int endNode) {
        List<GraphEdge[]> out = new ArrayList<GraphEdge[]>();
        if(n == 1) {
            for(GraphEdge startNodeOutEdge : g.getOutEdges(startNode)) {
                //System.out.println("startNodeOutEdge.CHILD_ID: " + startNodeOutEdge.CHILD_ID);
                //System.out.println("endNode: " + endNode);
                //System.out.println("----------------------------------");
                if(startNodeOutEdge.CHILD_ID == endNode) out.add(new GraphEdge[]{startNodeOutEdge});
            }
            return out;
        }
        
        for(GraphEdge startOutEdge : g.getOutEdges(startNode)) {
            List<GraphEdge[]> pathsFromStartOutEdgeChildToEndNode = getAllNEdgePathsFromNodeToNode(g, n-1, startOutEdge.CHILD_ID, endNode);
            for(GraphEdge[] pathFromStartOutEdgeChildToEndNode : pathsFromStartOutEdgeChildToEndNode) {
                GraphEdge[] startOutEdgeMergedWithSubpath = new GraphEdge[pathFromStartOutEdgeChildToEndNode.length + 1];
                startOutEdgeMergedWithSubpath[0] = startOutEdge;
                for(int i = 1; i < startOutEdgeMergedWithSubpath.length; i++){ 
                    startOutEdgeMergedWithSubpath[i] = pathFromStartOutEdgeChildToEndNode[i-1];
                }
                out.add(startOutEdgeMergedWithSubpath);
            }
        }
        return out;
    }
    
    public static void unlinkAll(Graph g) {
        for(int i = 0; i < g.numNodes(); i++) {
            List<GraphEdge> children = g.getOutEdges(i);
            for(GraphEdge outEdge : children) {
                g.unlink(i, outEdge.CHILD_ID);
            }
        }
    }
    
    //erases all links in clone and clones source into it
    public static void eraseAndCloneInto(Graph source, Graph clone) {
        unlinkAll(clone);
        cloneInto(source, clone);
    }
}
