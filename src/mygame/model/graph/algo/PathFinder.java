package mygame.model.graph.algo;

import java.util.ArrayList;
import java.util.List;
import mygame.model.graph.Graph;
import mygame.model.graph.GraphEdge;

public class PathFinder {
    public static List<GraphEdge[]> getAllNEdgePathsFromNodeToNode(Graph g, int n, int startNode, int endNode) {
        List<GraphEdge[]> out = new ArrayList<GraphEdge[]>();
        if(n == 1) {
            for(GraphEdge startNodeOutEdge : g.getOutEdges(startNode)) {
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
}
