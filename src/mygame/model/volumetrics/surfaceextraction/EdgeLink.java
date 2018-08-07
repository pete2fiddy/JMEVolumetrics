
package mygame.model.volumetrics.surfaceextraction;

import mygame.util.HashUtil;
import org.jblas.DoubleMatrix;

public class EdgeLink {
        protected int corner1, corner2;
        protected DoubleMatrix interpPoint;
        
        public EdgeLink(int corner1, int corner2) {
            this.corner1 = corner1;
            this.corner2 = corner2;
        }
        
        public void setInterpPoint(DoubleMatrix corners, double[] CORNER_ISOS, double isoValue) {
            DoubleMatrix p1 = corners.getRow(corner1);
            DoubleMatrix p2 = corners.getRow(corner2);
            double v1 = CORNER_ISOS[corner1];
            double v2 = CORNER_ISOS[corner2];
            this.interpPoint = p1.add((p2.sub(p1)).mul((isoValue - v1)/(v2-v1)));
        }
        
        @Override
        public boolean equals(Object l2) {
            if(!(l2 instanceof EdgeLink)) return false;
            EdgeLink link2 = (EdgeLink)l2;
            return (corner1 == link2.corner1 && corner2 == link2.corner2) || (corner1 == link2.corner2 && corner2 == link2.corner1);
        }

        @Override
        public int hashCode() {
            //flipped hashes multiplied together so if equivalent objects with flipped corner1 and corner2 
            //are hashed, they have the same hash code
            return HashUtil.simpleIntArrHashCode(corner1, corner2) * HashUtil.simpleIntArrHashCode(corner2, corner1);
        }
        
        @Override
        public String toString() {
            return "EdgeLink: " + Integer.toString(corner1) + ", " + Integer.toString(corner2);
        }
    }