package mygame.model.volumetrics.surfaceextraction;

import com.jme3.scene.shape.Box;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import mygame.model.graph.Graph;
import mygame.model.graph.GraphEdge;
import mygame.model.graph.SparseGraph;
import mygame.util.HashUtil;
import mygame.util.JblasJMEConverter;
import mygame.model.volumetrics.ScalarField;
import org.jblas.DoubleMatrix;

public class SurfaceNetCube {
    private static final DoubleMatrix CORNER_PLACEMENTS = new DoubleMatrix(new double[][] {{0,0,0},
        {1,0,0},
        {1,1,0},
        {0,1,0},
        {0,0,1},
        {1,0,1},
        {1,1,1},
        {0,1,1}});
    private static final Graph CONNECTED_CORNERS;
    static {
        CONNECTED_CORNERS = new SparseGraph(CORNER_PLACEMENTS.rows);
        for(int i = 0; i < CORNER_PLACEMENTS.rows; i++) {
            for(int j = 0; j < CORNER_PLACEMENTS.rows; j++) {
                if(i!=j) {
                    if(CORNER_PLACEMENTS.getRow(i).distance1(CORNER_PLACEMENTS.getRow(j)) <= 1) {
                        CONNECTED_CORNERS.link(i,j,1);
                    }
                }
            }
        }
    }
    
    protected DoubleMatrix corners;
    protected double[] CORNER_ISOS = new double[CORNER_PLACEMENTS.rows];
    protected Collection<EdgeLink> intersectingEdges = new HashSet<EdgeLink>();
    protected DoubleMatrix cubeInterpPoint = DoubleMatrix.zeros(3);
    
    public SurfaceNetCube(ScalarField<DoubleMatrix> scalarFunc, DoubleMatrix coord, double cubeWidth, double isoValue) {
        setCorners(coord, cubeWidth);
        setCornerIsoVals(scalarFunc);
        setIntersectingEdges(isoValue);
        setCubeInterpPoint();
    }
    
    private void setCorners(DoubleMatrix coord, double cubeWidth) {
        corners = CORNER_PLACEMENTS.mul(cubeWidth).addRowVector(coord);
    }
    
    private void setCornerIsoVals(ScalarField<DoubleMatrix> scalarFunc) {
        for(int i = 0; i < corners.rows; i++) {
            CORNER_ISOS[i] = scalarFunc.scalarValue(corners.getRow(i));
        }
    }
    
    private void setIntersectingEdges(double isoValue) {
        for(int corner1 = 0; corner1 < CORNER_PLACEMENTS.rows; corner1++) {
            for(GraphEdge edge : CONNECTED_CORNERS.getOutEdges(corner1)) {
                int corner2 = edge.CHILD_ID;
                if((CORNER_ISOS[corner1] < isoValue && CORNER_ISOS[corner2] >= isoValue) || 
                   (CORNER_ISOS[corner2] < isoValue && CORNER_ISOS[corner1] >= isoValue)) {
                    EdgeLink addLink = new EdgeLink(corner1, corner2);
                    addLink.setInterpPoint(corners, CORNER_ISOS, isoValue);
                    intersectingEdges.add(addLink);
                }
            }
        }
    }
    
    private void setCubeInterpPoint() {
        for(EdgeLink link : intersectingEdges) {
            cubeInterpPoint.addi(link.interpPoint);
        }
        cubeInterpPoint.divi((double)intersectingEdges.size());
    }
    
    public boolean intersectsSurface() {
        return !intersectingEdges.isEmpty();
    }
    
    
    public Box getGeom() {
        return new Box(JblasJMEConverter.toVector3f(corners.getRow(0))[0],
        JblasJMEConverter.toVector3f(corners.getRow(6))[0]);
    }
}
