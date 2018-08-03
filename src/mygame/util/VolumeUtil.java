package mygame.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.data.search.KDTree;
import mygame.graph.Graph;
import mygame.graph.GraphEdge;
import mygame.graph.SparseGraph;
import mygame.graph.SymmetricGraph;
import mygame.ml.similarity.SimilarityMetric;
import mygame.ml.similarity.jblas.JblasCosAngleSquaredSimilarity;
import mygame.ml.similarity.jblas.JblasRiemannianCost;
import mygame.volumetrics.CloudNormal;
import mygame.volumetrics.Facet;
import mygame.volumetrics.IndexedVolume;
import mygame.volumetrics.Volume;
import org.jblas.DoubleMatrix;

public class VolumeUtil {
    
    /*
    Process apperas to be pretty resilient and outputs a volume that closely resembles the input, EVEN if equality radius is too large.
    Might be a good way to scale a model down for speed? (at least the process for removing redundant points might be)
    */
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
    
    /*
    not as practical and correct as using the cloud normals to orient the faces of the volume, but can work without the requirements
    of anything other than an indexed volume. Applying hoppe point cloud normal orientation to this is a bit of a hack. It's likely 
    doable in a correct setting, but I'm not sure how the Riemannian would need to be constructed using facets rather than points.
    */
    public static SymmetricGraph constructVolumeNormalRiemannian(IndexedVolume v) {
        SimilarityMetric<DoubleMatrix> simMetric = new JblasRiemannianCost();
        
        SymmetricGraph out = new SymmetricGraph(new SparseGraph(v.numFacets()));
        for(int id1 = 0; id1 < v.numFacets(); id1++) {
            for(int id2 = 0; id2 < v.numFacets(); id2++) {
                if(FacetUtil.indexedFacetsConnected(v.getFacetInds(id1), v.getFacetInds(id2))) {
                    double sim = simMetric.similarityBetween(v.getFacet(id1).getNormalClone(), v.getFacet(id2).getNormalClone());
                    out.link(id1, id2, sim);
                }
            }
        }
        return out;
    }
    
    
    public static void useCloudNormalsToOrientFaces(KDTree pointsKdTree, DoubleMatrix pointNormals, Volume v) {
        for(int i = 0; i < v.numFacets(); i++) {
            DoubleMatrix meanNormal = DoubleMatrix.zeros(3);
            Facet f = v.getFacet(i);
            for(int j = 0; j < f.numPoints(); j++) {
                meanNormal = meanNormal.add(pointNormals.getRow(pointsKdTree.getNearestNeighborId(f.getPointClones(j).toArray())));
            }
            meanNormal = meanNormal.div((double)f.numPoints());
            
            
            
            if(MathUtil.cosAngleBetween(meanNormal, v.getFacet(i).getNormalClone()) < 0) {
                //flip required
                v.flipOrientation(i);
            }
        }
    }
    
    public static void hoppeOrientFaces(IndexedVolume v) {
        SymmetricGraph faceRiemannian = constructVolumeNormalRiemannian(v);
        Graph minSpanRiemannian = GraphUtil.primsMinimumSpanningTree(faceRiemannian, 0);
        DoubleMatrix normals = DoubleMatrix.zeros(v.numFacets(), 3);
        for(int i = 0; i < normals.rows; i++) {
            normals.putRow(i, v.getFacet(i).getNormalClone());
        }
        Set<Integer> toFlipFacetInds = new HashSet<Integer>();
        CloudNormal.setIndsToHoppeOrientNormalsWithRiemannianMinSpanTree(normals, minSpanRiemannian, 0, toFlipFacetInds);
        System.out.println("to flip facet inds: " + toFlipFacetInds);
        for(int flipFacet : toFlipFacetInds) {
            v.flipOrientation(flipFacet);
        }
        for(int i = 0; i < v.numFacets(); i++) {
            v.flipOrientation(i);
        }
    }
    
    /*
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
        Set<Integer> notVisited = new HashSet<Integer>();
        for(int i = 0; i < v.numFacets(); i++) {
            notVisited.add(i);
        }
        while(notVisited.size() > 0) {
           //since the graph traversal can only traverse a single connected component
           int startFace = notVisited.iterator().next();
           notVisited.remove(startFace);
            orientFacesOfComponent(v, faceConnections, startFace, notVisited);
        }
    }
    
    private static void orientFacesOfComponent(IndexedVolume v, Graph faceConnectionGraph, int currFace, Set<Integer> notVisited) {
        if(notVisited.size() <= 0) return;
        
        int[] currFacetInds = v.getFacetInds(currFace);
        List<GraphEdge> currFaceOutEdges = faceConnectionGraph.getOutEdges(currFace);
        for(GraphEdge outEdge : currFaceOutEdges) {
            notVisited.remove(outEdge.CHILD_ID);//visiting now so that later traversals don't visit what is to be visited
        }
        for(GraphEdge outEdge : currFaceOutEdges) {
            if(notVisited.contains(outEdge.CHILD_ID)) {
                //orient outEdge.CHILD_ID if necessary
                if(FacetUtil.neighboringFacetRequiresOrientationFlip(currFacetInds, 
                        v.getFacetInds(outEdge.CHILD_ID))){
                    v.flipOrientation(outEdge.CHILD_ID);
                    System.out.println("face flipped");
                }
                //traverse on outEdge.CHILD_ID
                orientFacesOfComponent(v, faceConnectionGraph, outEdge.CHILD_ID, notVisited);
            }
        }
    }
    */
}
