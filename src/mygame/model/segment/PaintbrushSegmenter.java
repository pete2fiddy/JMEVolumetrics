package mygame.model.segment;

import java.util.Set;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.graph.Graph;
import mygame.model.segment.PaintbrushSegmenter.PaintbrushSegmenterArgs;
import mygame.util.ArgumentContainer;

public class PaintbrushSegmenter implements Segmenter<PaintbrushSegmenterArgs> {

    @Override
    public Set segment(PaintbrushSegmenterArgs args) {
        NearestNeighborSearcher neighborSearcher = (NearestNeighborSearcher)args.get("neighborSearcher");
        double[] seedPoint = (double[])args.get("seedPoint");
        double radius = (double)args.get("radius");
        return neighborSearcher.getIdsWithinRadius(seedPoint, radius);
    }

    @Override
    public <D> D accept(SegmenterVisitor<D> visitor) {
        return visitor.visit(this);
    }
    
    public static class PaintbrushSegmenterArgs extends ArgumentContainer {

        public PaintbrushSegmenterArgs(NearestNeighborSearcher neighborSearcher, double[] seedPoint, double radius) {
            super(neighborSearcher, seedPoint, radius);
        }

        @Override
        protected String[] argNames() {
            return new String[] {"neighborSearcher", "seedPoint", "radius"};
        }
        
    }
}
