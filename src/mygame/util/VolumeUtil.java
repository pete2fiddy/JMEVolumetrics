package mygame.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.model.data.search.KDTree;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.graph.Graph;
import mygame.model.graph.GraphEdge;
import mygame.model.graph.SparseGraph;
import mygame.model.graph.SymmetricGraph;
import mygame.model.data.ml.similarity.SimilarityMetric;
import mygame.model.data.ml.similarity.jblas.JblasCosAngleSquaredSimilarity;
import mygame.model.data.ml.similarity.jblas.JblasRiemannianCost;
import mygame.model.graph.algo.PrimsMinSpan;
import mygame.model.volumetrics.CloudNormal;
import mygame.model.volumetrics.Facet;
import mygame.model.volumetrics.IndexedVolume;
import mygame.model.volumetrics.Volume;
import org.jblas.DoubleMatrix;

public class VolumeUtil {
    
    /*
    Process apperas to be pretty resilient and outputs a volume that closely resembles the input, EVEN if equality radius is too large.
    Might be a good way to scale a model down for speed? (at least the process for removing redundant points might be)
    */
    public static IndexedVolume convertToIndexedVolume(Volume v, double equalityRadius) {
        
        DoubleMatrix allPoints = getPoints(v);
        NearestNeighborSearcher allPointsTree = new KDTree(allPoints.toArray2());
        int[] uniquePointSubset = getUniquePoints(allPoints, allPointsTree, equalityRadius);
        DoubleMatrix uniquePoints = allPoints.getRows(uniquePointSubset);
        NearestNeighborSearcher uniquePointsTree = new KDTree(uniquePoints.toArray2());
        
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
    
    public static int[] getUniquePoints(DoubleMatrix allPoints, NearestNeighborSearcher kdTree, double equalityRadius) {
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
    
    
    public static void useCloudNormalsToOrientFaces(NearestNeighborSearcher pointsKdTree, DoubleMatrix pointNormals, Volume v) {
        for(int i = 0; i < v.numFacets(); i++) {
            DoubleMatrix meanNormal = DoubleMatrix.zeros(3);
            Facet f = v.getFacet(i);
            for(int j = 0; j < f.numPoints(); j++) {
                meanNormal = meanNormal.add(pointNormals.getRow(pointsKdTree.getNearestNeighborIds(f.getPointClones(j).toArray(), 1)[0]));
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
        Graph minSpanRiemannian = PrimsMinSpan.buildMST(faceRiemannian, 0);
        DoubleMatrix normals = DoubleMatrix.zeros(v.numFacets(), 3);
        for(int i = 0; i < normals.rows; i++) {
            normals.putRow(i, v.getFacet(i).getNormalClone());
        }
        Set<Integer> toFlipFacetInds = new HashSet<Integer>();
        CloudNormal.setIndsToHoppeOrientNormalsWithRiemannianMinSpanTree(normals, minSpanRiemannian, 0, toFlipFacetInds);
        for(int flipFacet : toFlipFacetInds) {
            v.flipOrientation(flipFacet);
        }
        for(int i = 0; i < v.numFacets(); i++) {
            v.flipOrientation(i);
        }
    }
}
