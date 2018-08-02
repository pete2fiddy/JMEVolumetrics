package mygame.volumetrics.surfaceextraction.convexhull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import mygame.volumetrics.Facet;
import mygame.volumetrics.IndexedVolume;
import mygame.volumetrics.Volume;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;

public class ConvexHull {
    
    /*
    returns a volume of points, X's, convex hull, with a list detailing the indices of the corresponding faces in the output volume. Faces are already correctly oriented.
    */
    public static IndexedVolume quickhull3d(DoubleMatrix X) {
        ConvHullVolume out = initConvHull(X);
        Set<Integer> remainingPoints = new HashSet<Integer>();
        for(int i = 0; i < X.rows; i++) {
            remainingPoints.add(i);
        }
        for(int facetNum = 0; facetNum < out.numFacets(); facetNum++) {
            int[] facetInds = out.getFacetInds(facetNum);
            for(int i : facetInds) {
                remainingPoints.remove(i);
            }
        }
        
        remainingPoints.removeAll(getPointsWithinHull(X, remainingPoints, out));
        
        
        while(remainingPoints.size() > 0) {
            int farthestPoint = out.getPointFarthestFromFacets(X, out.getOutsideSets(X, remainingPoints))[1];
            remainingPoints.remove(farthestPoint);
            List<Integer>[] visInvisFacets = out.getVisibleInvisibleFaces(X.getRow(farthestPoint));
            Set<IndexedFacetEdge> horizonRidge = out.getHorizonRidge(X.getRow(farthestPoint), visInvisFacets);
            
            for(int visibleFacetNum : visInvisFacets[0]) {
                int[] visFacetInds = out.getFacetInds(visibleFacetNum);
                remainingPoints.removeAll(getPointsWithinHull(X, remainingPoints, constructSimplexFromBaseAndPoint(X, visFacetInds[0], visFacetInds[1], visFacetInds[2], farthestPoint)));
                out.removeFacet(visibleFacetNum);
            }
            
            for(IndexedFacetEdge horizonEdge : horizonRidge) {
                out.addFacet(horizonEdge.X_INDS[0], horizonEdge.X_INDS[1], farthestPoint);
            }
            
            
            
            //is doing extra computation work by using the entire convex hull instead of only simplices created from farthestPoint and visible faces
            remainingPoints.removeAll(getPointsWithinHull(X, remainingPoints, out));
        }
        return (IndexedVolume)out;
    }
    
    
    private static Set<Integer> getPointsWithinHull(DoubleMatrix X, Set<Integer> remainingPoints, ConvHullVolume hull) {
        Set<Integer> out = new HashSet<Integer>();
        for(int remainingId : remainingPoints) {
            if(hull.containsPoint(X.getRow(remainingId))) out.add(remainingId);
        }
        return out;
    }
    
    private static ConvHullVolume initConvHull(DoubleMatrix X) {
        int p1 = 0, p2 = 0, p3 = 0;
        for(int i = 0; i < X.rows; i++) {
            if(X.get(i,0) < X.get(p1,0)) p1 = i;
            if(X.get(i,0) > X.get(p2,0)) p2 = i;
            if(X.get(i,1) > X.get(p3,1)) p3 = i;
        }
        //baseFacet SHOULD be CCW
        Facet baseFacet = new Facet(X.getRows(new int[]{p1, p2, p3}));
        //technically possible for no points to be exterior to baseFacet
        int farthestPoint = baseFacet.getFarthestPoint(X);
        return constructSimplexFromBaseAndPoint(X, p1,p2,p3,farthestPoint);
    }
    
    private static ConvHullVolume constructSimplexFromBaseAndPoint(DoubleMatrix X, int baseP1, int baseP2, int baseP3, int addPoint) {
        ConvHullVolume out = new ConvHullVolume(X);
        out.addFacet(baseP3, baseP2, baseP1);
        out.addFacet(baseP1, baseP2, addPoint);
        out.addFacet(baseP2, baseP3, addPoint);
        out.addFacet(baseP3, baseP1, addPoint);
        return out;
    }
    
}
