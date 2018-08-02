package mygame.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.data.search.KDTree;
import mygame.graph.Graph;
import mygame.graph.GraphEdge;
import mygame.volumetrics.Facet;
import mygame.volumetrics.IndexedVolume;
import mygame.volumetrics.Volume;
import org.jblas.DoubleMatrix;

public class VolumeUtil {
    
    public static IndexedVolume convertToIndexedVolume(Volume v, double equalityRadius) {
        
        DoubleMatrix allPoints = getPoints(v);
        KDTree allPointsTree = new KDTree(allPoints.toArray2());
        
        int[] uniquePointSubset = getUniquePoints(allPoints, allPointsTree, equalityRadius);
        DoubleMatrix uniquePoints = allPoints.getRows(uniquePointSubset);
        KDTree uniquePointsTree = new KDTree(uniquePoints.toArray2());
        IndexedVolume out = new IndexedVolume(uniquePoints);
        
        for(int facetNum = 0; facetNum < v.numFacets(); facetNum++) {
            Facet f = v.getFacet(facetNum);
            int[] uniqueFacetPoints = new int[f.numPoints()];
            for(int i = 0; i < uniqueFacetPoints.length; i++) {
                uniqueFacetPoints[i] = uniquePointsTree.getNearestNeighborIds(f.getPointClones(i).toArray(), 1)[0];
            }
            out.addFacet(uniqueFacetPoints);
        }
        return out;
    }
    
    public static int[] getUniquePoints(DoubleMatrix allPoints, KDTree kdTree, double equalityRadius) {
        //for a given point, add to unique points if no points wihtin equalityRadius around it are contained
        //in uniquePoints
        Set<Integer> uniquePoints = new HashSet<Integer>();
        for(int i = 0; i < allPoints.rows; i++) {
            Set<Integer> withinRadius = kdTree.getIdsWithinRadius(allPoints.getRow(i).toArray(), equalityRadius);
            boolean pointUnique = true;
            for(int radiusPoint : withinRadius) {
                if(uniquePoints.contains(radiusPoint)) {
                    pointUnique = false;
                    break;
                }
            }
            if(pointUnique) uniquePoints.add(i);
        }
        int[] out = new int[uniquePoints.size()];
        int i = 0;
        for(int uniquePoint : uniquePoints) {
            out[i++] = uniquePoint;
        }
        return out;
    }
    
    
    
    public static DoubleMatrix getPoints(Volume v) {
        DoubleMatrix out = new DoubleMatrix(numPoints(v), 3);
        int pointNum = 0;
        for(int i = 0; i < v.numFacets(); i++) {
            Facet f = v.getFacet(i);
            for(int facetPointNum = 0; facetPointNum < f.numPoints(); facetPointNum++) {
                out.putRow(pointNum++, f.getPointClones(facetPointNum));
            }
        }
        return out;
    }
    
    public static int numPoints(Volume v) {
        int points = 0;
        for(int i = 0; i < v.numFacets(); i++) {
            points += v.getFacet(i).numPoints();
        }
        return points;
    }
    
    public static void orientFaces(IndexedVolume v) {
        //construct a graph of all neighboring facets
        //start from a random facet and deem it to be oriented correctly
        //DFS outward, flipping orientations when necessary (for a base face, a neighbor face must be flipped
        //if an edge they share are in the same index order)
        int[][] facetInds = new int[v.numFacets()][];
        for(int i = 0; i < v.numFacets(); i++) {
            facetInds[i] = v.getFacetInds(i);
        }
        Graph faceConnections = FacetUtil.getIndexedFacetConnectionGraph(facetInds);
        Set<Integer> visited = new HashSet<Integer>();
        while(visited.size() < v.numFacets()) {
           //since the graph traversal can only traverse a single connected component
            orientFacesOfComponent(v, faceConnections, visited.iterator().next(), visited);
        }
    }
    
    private static void orientFacesOfComponent(IndexedVolume v, Graph faceConnectionGraph, int currFace, Set<Integer> visited) {
        if(visited.size() >= v.numFacets()) return;
        
        int[] currFacetInds = v.getFacetInds(currFace);
        List<GraphEdge> currFaceOutEdges = faceConnectionGraph.getOutEdges(currFace);
        for(GraphEdge outEdge : currFaceOutEdges) {
            visited.add(outEdge.CHILD_ID);//visiting now so that later traversals don't visit what is to be visited
        }
        for(GraphEdge outEdge : currFaceOutEdges) {
            if(!visited.contains(outEdge.CHILD_ID)) {
                //orient outEdge.CHILD_ID if necessary
                if(FacetUtil.neighboringFacetRequiresOrientationFlip(currFacetInds, 
                        v.getFacetInds(outEdge.CHILD_ID))){
                    v.flipOrientation(outEdge.CHILD_ID);
                }
                //traverse on outEdge.CHILD_ID
                orientFacesOfComponent(v, faceConnectionGraph, outEdge.CHILD_ID, visited);
            }
        }
    }
}
