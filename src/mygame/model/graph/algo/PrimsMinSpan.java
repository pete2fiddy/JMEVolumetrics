package mygame.model.graph.algo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import mygame.model.data.search.priorityqueue.ArrayBinaryMinHeap;
import mygame.model.graph.Graph;
import mygame.model.graph.GraphEdge;
import mygame.model.graph.SparseGraph;
import mygame.model.graph.SymmetricGraph;

public class PrimsMinSpan {
    
    
    //assumes graph has 1 connected component 
    public static Graph buildMST(SymmetricGraph graph, int headId) {
        Graph tree = new SparseGraph(graph.numNodes());
        PrimsNode[] primsNodes = new PrimsNode[graph.numNodes()];
        for(GraphEdge connectedToHeadEdge : graph.getOutEdges(headId)) {
            primsNodes[connectedToHeadEdge.CHILD_ID] = new PrimsNode(connectedToHeadEdge.CHILD_ID, connectedToHeadEdge.WEIGHT, headId);
        }
        
        Map<Integer, Double>[] weights = new HashMap[graph.numNodes()];
        for(int i = 0; i < primsNodes.length; i++) {
            if(primsNodes[i] == null) primsNodes[i] = new PrimsNode(i, Double.MAX_VALUE, -1);
            weights[i] = new HashMap<Integer, Double>();
            for(GraphEdge g : graph.getOutEdges(i)) {
                weights[i].put(g.CHILD_ID, g.WEIGHT);
            }
        }
        ArrayBinaryMinHeap<PrimsNode> nodeHeap = new ArrayBinaryMinHeap<PrimsNode>(primsNodes.length);
        for(int i = 0; i < primsNodes.length; i++) {
            if(i != headId)nodeHeap.add(primsNodes[i], primsNodes[i].weight);
        }
        boolean[] added = new boolean[graph.numNodes()];
        added[headId] = true;
        constructPrimsMST(weights, tree, primsNodes, nodeHeap, added);
        return tree;
    }
    
    
    
    private static void constructPrimsMST(Map<Integer, Double>[] weights, Graph tree, PrimsNode[] primsNodes, ArrayBinaryMinHeap<PrimsNode> nodeHeap, boolean[] added) {
        if(nodeHeap.size() <= 0) return;
        PrimsNode addNode = nodeHeap.pop();
        if(addNode.inMstId < 0) throw new UnsupportedOperationException("Graph passed to buildMST has only one connected component -- MST not constructable!");
        tree.link(addNode.inMstId, addNode.MY_ID, addNode.weight);
        added[addNode.MY_ID] = true;
        for(int outNode : weights[addNode.MY_ID].keySet()) {
            PrimsNode updateNode = primsNodes[outNode];
            if(!added[updateNode.MY_ID] && addNode.MY_ID != updateNode.MY_ID && weights[addNode.MY_ID].get(updateNode.MY_ID) < updateNode.weight) {
                updateNode.weight = addNode.weight;
                updateNode.inMstId = addNode.MY_ID;
                
                nodeHeap.updatePriority(updateNode, updateNode.weight);
            }
        }
        constructPrimsMST(weights, tree, primsNodes, nodeHeap, added);
    }
    
    private static class PrimsNode {
        public final int MY_ID;
        public double weight;
        public int inMstId;
        
        public PrimsNode(int myId, double weight, int inMstId) {
            this.MY_ID = myId;
            this.weight = weight;
            this.inMstId = inMstId;
        } 
        
        public static final Comparator<PrimsNode> COMPARATOR = new Comparator<PrimsNode>() {
            @Override
            public int compare(PrimsNode t, PrimsNode t1) {
                if(t.weight < t1.weight) return 1;
                if(t.weight > t1.weight) return -1;
                return 0;
            }
        };
    }
}
