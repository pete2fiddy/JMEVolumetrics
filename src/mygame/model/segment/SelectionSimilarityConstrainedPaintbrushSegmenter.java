package mygame.model.segment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.graph.Graph;
import mygame.model.graph.GraphEdge;
import mygame.model.segment.PaintbrushSegmenter.PaintbrushSegmenterArgs;
import mygame.model.segment.SelectionSimilarityConstrainedPaintbrushSegmenter.SelectionSimilarityConstrainedPaintbrushSegmenterArgs;
import mygame.util.ArgumentContainer;

public class SelectionSimilarityConstrainedPaintbrushSegmenter implements Segmenter<SelectionSimilarityConstrainedPaintbrushSegmenterArgs> {

    @Override
    public Set<Integer> segment(SelectionSimilarityConstrainedPaintbrushSegmenterArgs args) {
        Graph simGraph = (Graph)args.get("simGraph");
        NearestNeighborSearcher neighborSearcher = (NearestNeighborSearcher)args.get("neighborSearcher");
        double[] seedPoint = (double[])args.get("seedPoint");
        double radius = (double)args.get("radius");
        double tolerance = (double)args.get("tolerance");
        
        int seedInd = neighborSearcher.getNearestNeighborIds(seedPoint, 1)[0];
        Map<Integer, Double> outSeedWeights = new HashMap<Integer, Double>();
        for(GraphEdge seedOutEdge : simGraph.getOutEdges(seedInd)) {
            outSeedWeights.put(seedOutEdge.CHILD_ID, seedOutEdge.WEIGHT);
        }
        
        Set<Integer> idsInRadius = neighborSearcher.getIdsWithinRadius(seedPoint, radius);
        Set<Integer> out = new HashSet<Integer>();
        for(int id : idsInRadius) {
            Double idWeight = outSeedWeights.get(id);
            System.out.println("id weight: " + idWeight + ", tolerance: " + tolerance);
            if(idWeight != null && idWeight > tolerance) {
                out.add(id);
            }
        }
        return out;
    }

    @Override
    public <D> D accept(SegmenterVisitor<D> visitor) {
        return visitor.visit(this);
    }

    
    public static class SelectionSimilarityConstrainedPaintbrushSegmenterArgs extends ArgumentContainer {

        public SelectionSimilarityConstrainedPaintbrushSegmenterArgs(Graph simGraph, NearestNeighborSearcher neighborSearcher, double[] seedPoint, double radius, double tolerance) {
            super(simGraph, neighborSearcher, seedPoint, radius, tolerance);
        }

        @Override
        protected String[] argNames() {
            return new String[] {"simGraph", "neighborSearcher", "seedPoint", "radius", "tolerance"};
        }
        
    }
}
