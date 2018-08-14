package mygame.model.graph.algo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.model.graph.Graph;
import mygame.model.graph.GraphEdge;

public class Floodfill {
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
