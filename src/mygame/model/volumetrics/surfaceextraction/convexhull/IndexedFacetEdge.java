/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.model.volumetrics.surfaceextraction.convexhull;


public class IndexedFacetEdge {
    public final int[] X_INDS;

    public IndexedFacetEdge(int... xInds) {
        assert(xInds.length == 2);
        X_INDS = xInds;
    }

    @Override
    public boolean equals(Object f1) {
        if(!(f1 instanceof IndexedFacetEdge)) return false;
        IndexedFacetEdge f = (IndexedFacetEdge)f1;
        if(f.X_INDS.length != X_INDS.length) return false;
        for(int i = 0; i < X_INDS.length; i++) {
            if(f.X_INDS[i] != X_INDS[i]) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        //hashcode implementation from here: https://stackoverflow.com/questions/9858376/hashcode-for-3d-integer-coordinates-with-high-spatial-coherence
        int hash = 23;
        for(int i : X_INDS) {
            hash = hash*31 + i;
        }
        return hash;
    }
}
