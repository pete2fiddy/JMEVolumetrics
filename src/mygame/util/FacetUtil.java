package mygame.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.graph.Graph;
import mygame.graph.SparseGraph;


public class FacetUtil {
    
    public static boolean indexedFacetsConnected(int[] f1, int[] f2) {
        return getCommonIndices(f1, f2).size() >= 2;
    }
    
    public static Graph getIndexedFacetConnectionGraph(int[][] facetInds) {
        Graph out = new SparseGraph(facetInds.length);
        for(int facet1 = 0; facet1 < facetInds.length; facet1++) {
            for(int facet2 = 0; facet2 < facetInds.length; facet2++) {
                if(indexedFacetsConnected(facetInds[facet1], facetInds[facet2])) {
                    out.link(facet1, facet2, 1);
                }
            }
        }
        return out;
    }
    
    /*
    returns a list of common indices between the indices of two facets, in the order
    they appear in f1Inds
    */
    public static List<Integer> getCommonIndices(int[] f1Inds, int[] f2Inds) {
        Set<Integer> f2Set = new HashSet<Integer>();
        for(int i : f2Inds) {
            f2Set.add(i);
        }
        List<Integer> out = new ArrayList<Integer>();
        for(int f1Ind : f1Inds) {
            if(f2Set.contains(f1Ind)) {
                out.add(f1Ind);
            }
        }
        return out;
    }
    
    public static boolean neighboringFacetRequiresOrientationFlip(int[] baseFace, int[] neighborFace) {
        assert(baseFace.length == 3 && neighborFace.length == 3);//may actually work in practice with nontriangular facets, but am not sure, so assertion for now
        List<Integer> commonInds = getCommonIndices(baseFace, neighborFace);
        assert(commonInds.size() >= 2) : "Neighboring facets not actually neighboring";
        //check if the commonInds in same direction in neighborFace. If so, neighborFace requires an orientation flip
        int neighborFaceFirstCommonIndLoc = 0;
        for(int i = 0; i < neighborFace.length; i++) {
            if(neighborFace[i] == commonInds.get(0)) {
                neighborFaceFirstCommonIndLoc = i;
                break;
            }
        }
        int nextNeighborFaceCommonIndLoc = (neighborFaceFirstCommonIndLoc+1)%neighborFace.length;
        assert(commonInds.contains(neighborFace[nextNeighborFaceCommonIndLoc]));
        
        if(neighborFace[neighborFaceFirstCommonIndLoc] == commonInds.get(1)) {
            //SHOULD mean that the shared corners are on the same direction, and neighborFace requires an orientation flip
            return true;
        }
        return false;
        
    }
}
