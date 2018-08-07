/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.model.volumetrics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class Volume {
    private List<Facet> facets = new ArrayList<Facet>();

    public Volume() {}

    public Volume(Iterable<Facet> facets) {
        for(Facet f : facets) {
            this.facets.add(f);
        }
    }
    
    public void addFacet(int i, Facet f) {facets.add(i, f);}
    
    public Facet getFacet(int i) {return facets.get(i);}
    
    public int numFacets() {return facets.size();}
    
    public Facet removeFacet(int i) {return facets.remove(i);}
    
    public void flipOrientation(int facetNum) {
        Facet toFlip = facets.remove(facetNum);
        DoubleMatrix flippedPoints = new DoubleMatrix(toFlip.numPoints(), 3);
        for(int i = 0; i < toFlip.numPoints(); i++) {
            flippedPoints.putRow(flippedPoints.rows-1-i, toFlip.getPointClones(i));
        }
        facets.add(facetNum, new Facet(flippedPoints));
    }
}
