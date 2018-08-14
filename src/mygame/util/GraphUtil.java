package mygame.util;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.graph.FullGraph;
import mygame.model.graph.Graph;
import mygame.model.graph.GraphEdge;
import mygame.model.graph.SparseGraph;
import mygame.model.graph.GraphNode;
import mygame.model.graph.SymmetricGraph;
import mygame.model.data.ml.similarity.SimilarityMetric;
import mygame.model.data.search.priorityqueue.ArrayBinaryMinHeap;
import mygame.model.graph.ParentChildGraphEdge;
import org.jblas.DoubleMatrix;


public class GraphUtil {
    
    
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
