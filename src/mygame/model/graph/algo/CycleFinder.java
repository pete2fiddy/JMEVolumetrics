package mygame.model.graph.algo;

import java.util.ArrayList;
import java.util.List;
import mygame.model.graph.Graph;
import mygame.model.graph.GraphEdge;

public class CycleFinder {
    
    
    
    
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
            List<GraphEdge[]> addCycles = PathFinder.getAllNEdgePathsFromNodeToNode(disposableGraph, n, id, id);
            for(GraphEdge[] addCycle : addCycles) {
                cycles.add(addCycle);
                for(int cycleId = 0; cycleId < addCycle.length; cycleId++) {
                    disposableGraph.unlink(cycleId, (cycleId+1)%addCycle.length);
                }
            }
        }
        return cycles;
    }
}
