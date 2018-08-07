package mygame.model.segment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.model.graph.Graph;
import mygame.model.graph.GraphEdge;
import mygame.model.segment.FloodfillSegmenter.FloodfillSegmenterArgs;
import mygame.util.ArgumentContainer;

public class FloodfillSegmenter implements Segmenter<FloodfillSegmenterArgs>{

    @Override
    public Set<Integer> segment(FloodfillSegmenterArgs args) {
        Graph graph = (Graph)args.get("graph");
        int seedNode = (int)args.get("seedNode");
        double minWeight = (double)args.get("minWeight");
        Set<Integer> visited = new HashSet<Integer>();
        Set<Integer> out = new HashSet<Integer>();
        iterFloodfill(graph, seedNode, out, visited, minWeight);
        return out;
    }
    
    private void iterFloodfill(Graph g, int currNode, Set<Integer> filled, Set<Integer> visited, double minWeight) {
        filled.add(currNode);
        visited.add(currNode);
        List<GraphEdge> currNodeOutEdges = g.getOutEdges(currNode);
        for(GraphEdge currNodeOutEdge : currNodeOutEdges) {
            if(!visited.contains(currNodeOutEdge.CHILD_ID) && currNodeOutEdge.WEIGHT > minWeight) {
                iterFloodfill(g, currNodeOutEdge.CHILD_ID, filled, visited, minWeight);
            }
        }
    }

    @Override
    public <D> D accept(SegmenterVisitor<D> visitor) {
        return visitor.visit(this);
    }
    
    public static class FloodfillSegmenterArgs extends ArgumentContainer {

        public FloodfillSegmenterArgs(Graph g, int seedNode, double minWeight) {
            super(g, seedNode, minWeight);
        }

        @Override
        protected String[] argNames() {
            return new String[] {"graph", "seedNode", "minWeight"};
        }
        
    }
    
}
