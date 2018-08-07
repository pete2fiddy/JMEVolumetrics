package mygame.model.volumetrics;

import org.jblas.Decompose;
import org.jblas.DoubleMatrix;


public class VolumeSolver {
    
    /*
    assumes all facets of V are triangles
    See http://chenlab.ece.cornell.edu/Publication/Cha/icip01_Cha.pdf for details
    
    Possible improvments: Using pointcloud normals calculated through my own method more noise-resiliant than cross product normals --
    either allow a Volume where the normals can be changed and are not final, or allow for a matrix of point cloud normals for each point to be passed.
    
    Code modification would be very light -- replace facet.NORMAL with the average normal of the 3 points of each facet using the calculated normals.
    */
    public static double calcVolume(Volume V) {
        double vol = 0;
        for(int i = 0; i < V.numFacets(); i++) {
            vol += getSignedSimplexVolumeFromOrigin(V.getFacet(i));
        }
        return Math.abs(vol);
    }
    
    private static double getSignedSimplexVolumeFromOrigin(Facet f) {
        assert(f.numPoints() == 3);
        double signMetric = f.getNormalClone().dot(f.getPointClones(0));
        double signDirection = (signMetric > 0)? 1 : -1;
        return signDirection * getUnsignedSimplexVolumeFromOrigin(f);
    }
    
    private static double getUnsignedSimplexVolumeFromOrigin(Facet f) {
        //honestly not sure why all volumes were off by exactly a factor of 3 (corrected for by dividing by 3.0)
        //-- paper implementation multiplies volumes by 1/6, and my math multiplied by 1/2 instead, so I assume 
        //the number isn't coming from nowhere.
        return (.5/3.0) * Math.abs(det3d(f.getPointClones(0,1,2).transpose()));
    }
    
    private static double det3d(DoubleMatrix A) {
        assert(A.rows == A.columns && A.rows == 3);
        return A.get(0,0)*(A.get(1,1)*A.get(2,2) - A.get(1,2)*A.get(2,1)) -
               A.get(0,1)*(A.get(1,0)*A.get(2,2) - A.get(1,2)*A.get(2,0)) +
               A.get(0,2)*(A.get(1,0)*A.get(2,1) - A.get(1,1)*A.get(2,0));
    }
}
