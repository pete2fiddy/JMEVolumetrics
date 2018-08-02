package mygame.volumetrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.volumetrics.surfaceextraction.convexhull.IndexedFacetEdge;
import org.jblas.DoubleMatrix;

public class IndexedVolume extends Volume {
    private List<int[]> facetInds = new ArrayList<int[]>();
    private DoubleMatrix points;
    
    public IndexedVolume(DoubleMatrix points) {
        this.points = points;
    }
    
    public int[] getFacetInds(int facetNum) {return facetInds.get(facetNum);}
    
    @Override
    public void flipOrientation(int facetNum) {
        int[] unflipped = facetInds.get(facetNum);
        removeFacet(facetNum);
        int[] flipped = new int[unflipped.length];
        for(int i = 0; i < flipped.length; i++) {
            flipped[i] = unflipped[unflipped.length - 1 - i];
        }
        addFacet(flipped);
    }
    
    public void addFacet(int... inds) {
        facetInds.add(0, inds);
        super.addFacet(0, new Facet(points.getRows(inds)));
    }
    
    @Override
    public Facet removeFacet(int i) {
        facetInds.remove(i);
        return super.removeFacet(i);
    }
    
    @Override
    public void addFacet(int i, Facet f) {
        throw new UnsupportedOperationException("Facets must be added to indexed volume along with corresponding indices denoting which points from the point set the facet uses");
    }
    
    public DoubleMatrix getPointSetClone() {
        DoubleMatrix out = DoubleMatrix.zeros(points.rows, points.columns);
        return out.copy(points);
    }
}
