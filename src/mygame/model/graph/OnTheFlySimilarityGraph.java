package mygame.model.graph;

import java.util.ArrayList;
import java.util.List;
import mygame.model.data.ml.similarity.SimilarityMetric;

public class OnTheFlySimilarityGraph <DataType> implements Graph {
    private DataType[] X;
    private SimilarityMetric<DataType> simMetric;
    
    public OnTheFlySimilarityGraph(DataType[] X, SimilarityMetric<DataType> simMetric) {
        this.X = X;
        this.simMetric = simMetric;
    }
    
    public void setData(DataType[] X) {
        this.X = X;
    }

    @Override
    public void link(int id1, int id2, double weight) {
        throw new UnsupportedOperationException("OnTheFlySimilarityGraph cannot link"
                + " anything, as connections are calculated when requested by calling getOutEdges");
    }

    @Override
    public List<GraphEdge> getOutEdges(int id) {
        ArrayList<GraphEdge> out = new ArrayList<GraphEdge>();
        for(int id2 = 0; id2 < X.length; id2++) {
            out.add(new GraphEdge(id2, simMetric.similarityBetween(X[id], X[id2])));
        }
        return out;
    }

    @Override
    public int numNodes() {
        return X.length;
    }

    @Override
    public void unlink(int id1, int id2) {
        throw new UnsupportedOperationException("OnTheFlySimilarityGraph cannot unlink"
                + " anything, as connections are calculated when requested by calling getOutEdges");
    }
    
}
