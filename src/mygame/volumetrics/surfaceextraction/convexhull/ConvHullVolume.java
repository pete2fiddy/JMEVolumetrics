/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.volumetrics.surfaceextraction.convexhull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.volumetrics.Facet;
import mygame.volumetrics.Volume;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Owner
 */
public class ConvHullVolume extends Volume {
    private List<Integer[]> facetInds = new ArrayList<Integer[]>();
    
    /*
    returns a set of edges (oriented such that edge[0] -> edge[1] is CCW) of the horizon line given an eyePoint
    */
    public Set<IndexedFacetEdge> getHorizonRidge(DoubleMatrix eyePoint, List<Integer>[] visibleInvisibleFaces) {
        //horizon ridge is just the intersection between the edges of facets from which the point is visible and the edges of facets from which the point is not visible
        //the order of edges of the non-visible facet on the horizon ridge is the correct orientation for the edges to be CCW
        Set<IndexedFacetEdge>[] visInvisEdges = getVisibleInvisibleEdges(eyePoint, visibleInvisibleFaces);
        Set<IndexedFacetEdge> edgeUnion = new HashSet<IndexedFacetEdge>();
        //edges added to edge union SHOULD be CCW
        for(IndexedFacetEdge e : visInvisEdges[0]) {
            if(visInvisEdges[1].contains(e)) edgeUnion.add(e);
        }
        for(IndexedFacetEdge e : visInvisEdges[1]) {
            if(visInvisEdges[0].contains(e)) edgeUnion.add(e);
        }
        return edgeUnion;
    }
    
    public Integer[] getFacetInds(int facetNum) {return facetInds.get(facetNum);}
    
    public Set<Integer>[] getOutsideSets(DoubleMatrix X, Set<Integer> remainingPoints) {
        Set<Integer>[] out = new Set[numFacets()];
        for(int i = 0; i < numFacets(); i++) {
            out[i] = new HashSet<Integer>();
        }
        for(int remainingId : remainingPoints) {
            for(int facetNum = 0; facetNum < numFacets(); facetNum++) {
                if(getFacet(facetNum).signedDistanceToPoint(X.getRow(remainingId)) > 0) {
                    out[facetNum].add(remainingId);
                    break;
                }
            }
        }
        return out;
    }

    public void addFacet(DoubleMatrix X, int... inds) {
        Integer[] addInds = new Integer[inds.length];
        for(int i = 0; i < inds.length; i++) {
            addInds[i] = inds[i];
        }
        facetInds.add(numFacets(), addInds);
        super.addFacet(numFacets(), new Facet(X.getRows(inds)));
    }
    
    @Override
    public Facet removeFacet(int i) {
        facetInds.remove(i);
        return super.removeFacet(i);
    }
    
    public boolean containsPoint(DoubleMatrix p) {
        for(int i = 0; i < numFacets(); i++) {
            if(getFacet(i).signedDistanceToPoint(p) > 0) return false;
        }
        return true;
    }

    /*
    returns [visible edges, invisible edges]. Flips visible edges so that they are oriented the same way invisible edges does.
    */
    protected Set<IndexedFacetEdge>[] getVisibleInvisibleEdges(DoubleMatrix eyePoint, List<Integer>[] visibleInvisibleFaces) {
        Set<IndexedFacetEdge>[] visibleInvisibleEdges = new HashSet[2];
        for(int i = 0; i < visibleInvisibleEdges.length; i++) {
            visibleInvisibleEdges[i] = new HashSet<IndexedFacetEdge>();
            for(int facetNum : visibleInvisibleFaces[i]) {
                Facet facet = getFacet(facetNum);
                Integer[] inds = facetInds.get(facetNum);
                for(int facetPoint = 0; facetPoint < inds.length; facetPoint++) {
                    int[] addEdge = (i == 1)? new int[] {inds[(facetPoint+1)%inds.length], 
                        inds[facetPoint]} : new int[] {inds[facetPoint], 
                        inds[(facetPoint+1)%inds.length]};//flip so that they have comparable ordering
                    visibleInvisibleEdges[i].add(new IndexedFacetEdge(addEdge));
                }
            }
        }
        return visibleInvisibleEdges;
    }

    /*
    returns [visible points, invisible points]
    */
    public List<Integer>[] getVisibleInvisibleFaces(DoubleMatrix eyePoint) {
        List<Integer>[] out = new ArrayList[2];
        for(int i = 0; i < out.length; i++) {
            out[i] = new ArrayList<Integer>();
        }
        for(int facetNum = numFacets()-1; facetNum >= 0; facetNum--) {
            if(getFacet(facetNum).signedDistanceToPoint(eyePoint) > 0) {
                out[0].add(facetNum);
            } else {
                out[1].add(facetNum);
            }
        }
        return out;
    }

    
    /*
    returns in order of [conflict face id, X id]
    */
    public int[] getPointFarthestFromFacets(DoubleMatrix X, Set<Integer>[] outsideSets) {
        int maxDistFace = 0;
        int maxDistInd = 0;
        double maxDist = -1;
        for(int setNum = 0; setNum < outsideSets.length; setNum++) {
            for(int xInd : outsideSets[setNum]) {
                double indDist = getFacet(setNum).signedDistanceToPoint(X.getRow(xInd));
                if(indDist > maxDist) {
                    maxDistFace = setNum;
                    maxDist = indDist;
                    maxDistInd = xInd;
                }
            }
        }
        return new int[] {maxDistFace, maxDistInd};
    }
    
    public Volume toVolume(List<Integer[]> inds) {
        Volume out = new Volume();
        for(int i = 0; i < numFacets(); i++) {
            out.addFacet(0, getFacet(i));
            inds.add(facetInds.get(i));
        }
        return out;
    }
}
