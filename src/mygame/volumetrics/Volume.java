/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.volumetrics;

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
}
