package mygame.model.volumetrics.surfaceextraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mygame.model.volumetrics.Facet;
import mygame.model.volumetrics.ScalarField;
import mygame.model.volumetrics.Volume;
import org.jblas.DoubleMatrix;

public class NaiveSurfaceNet {
    private static final EdgeLink[] CONNECTED_EDGES = {new EdgeLink(0,1),
    new EdgeLink(0,3),
    new EdgeLink(0,4),
    new EdgeLink(2,1),
    new EdgeLink(2,3),
    new EdgeLink(2,6),
    new EdgeLink(7,3),
    new EdgeLink(7,6),
    new EdgeLink(7,4),
    new EdgeLink(1,5),
    new EdgeLink(5,6),
    new EdgeLink(4,5)};
    
    
    private static final EdgeLink[] EDGES_TO_ITERATE = {new EdgeLink(0,1),
        new EdgeLink(0,3),
        new EdgeLink(0,4)
    };
    /*
    returning Object[] array is for troubleshooting -- knowing stuff like the intersecting cubes is helpful.
    Won't be in final implementation
    
    
    */
    
    public static Volume getVolume(ScalarField<DoubleMatrix> scalarFunc, double isoValue, double[][] iterBounds, double cubeWidth, double nCubesMargin) {
        return getVolume(scalarFunc, isoValue, bufferBounds(iterBounds, cubeWidth*nCubesMargin), cubeWidth);
    }
    
    private static double[][] bufferBounds(double[][] bounds, double bufferAmount) {
        double[][] out = new double[bounds.length][bounds[0].length];
        for(int i = 0; i < out.length; i++) {
            out[i][0] = bounds[i][0] - bufferAmount;
            out[i][1] = bounds[i][1] + bufferAmount;
        }
        return out;
    }
    
    public static Volume getVolume(ScalarField<DoubleMatrix> scalarFunc, double isoValue, double[][] iterBounds, double cubeWidth) {
        Map<NetCoord, SurfaceNetCube> coordCubeMap = getIntersectingCubes(scalarFunc, isoValue, iterBounds, cubeWidth);
        Volume out = new Volume();
        for(NetCoord coord : coordCubeMap.keySet()) {
            //only iterates over a subset of possible edges, as prevents duplicate quads being added. 
            //for example, if a cube has 3 neighbors that share its 0-1 edge,
            //then each neighbor has a different edge (for example, 7-6 for a cube down and back from the original)
            //that also connect to the original cube's 0-1 edge.
            for(EdgeLink edgeLink : EDGES_TO_ITERATE) {
                Facet[] quad = createQuadUsingSharedEdge(coordCubeMap, coord, edgeLink);
                if(quad != null) {
                    for(Facet quadFacet : quad) {
                        out.addFacet(0,quadFacet);
                    }
                }
            }
        }
        return out;
    }
    
    public static Map<NetCoord, SurfaceNetCube> getIntersectingCubes(ScalarField<DoubleMatrix> scalarFunc, double isoValue, double[][] iterBounds, double cubeWidth) {
        Map<NetCoord, SurfaceNetCube> out = new HashMap<NetCoord, SurfaceNetCube>();
        double[] nSteps = {(iterBounds[0][1]-iterBounds[0][0])/cubeWidth, 
            (iterBounds[1][1]-iterBounds[1][0])/cubeWidth,
            (iterBounds[2][1]-iterBounds[2][0])/cubeWidth};
        for(int x = 0; x < nSteps[0]; x++) {
            for(int y = 0; y < nSteps[1]; y++) {
                for(int z = 0; z < nSteps[2]; z++) {
                    NetCoord xyzCoord = new NetCoord(x, y, z);
                    SurfaceNetCube xyzCube = new SurfaceNetCube(scalarFunc, new DoubleMatrix(new double[][]{{iterBounds[0][0]+(x)*cubeWidth,
                    iterBounds[1][0]+(y)*cubeWidth, iterBounds[2][0]+(z)*cubeWidth}}), cubeWidth, isoValue);
                    if(xyzCube.intersectsSurface()) out.put(xyzCoord, xyzCube);
                }
            }
        }
        return out;
    }
    
    private static Facet[] createQuadUsingSharedEdge(Map<NetCoord, SurfaceNetCube> coordCubeMap, NetCoord baseCoord, EdgeLink baseEdge) {
        //check if the edge is actually intersected on baseCoord. If not, return null.
        SurfaceNetCube baseCube = coordCubeMap.get(baseCoord);
        if(!baseCube.intersectingEdges.contains(baseEdge)) return null;
        //logic will fill this such that connectedCubes[0] will be baseCube, and the remainder will be in an order such that adjacent pairs have a line between them
        SurfaceNetCube[] connectedCubes = new SurfaceNetCube[4];
        connectedCubes[0] = baseCube;
        
        
        //if logic below very easily error prone. Check against diagram to make sure correct
        if(baseEdge.equals(CONNECTED_EDGES[0])) {
            connectedCubes[1] = coordCubeMap.get(baseCoord.add(0,-1,0));
            connectedCubes[2] = coordCubeMap.get(baseCoord.add(0,-1,-1));
            connectedCubes[3] = coordCubeMap.get(baseCoord.add(0,0,-1));
        }
        else if(baseEdge.equals(CONNECTED_EDGES[1])) {
            connectedCubes[1] = coordCubeMap.get(baseCoord.add(-1,0,0));
            connectedCubes[2] = coordCubeMap.get(baseCoord.add(-1,0,-1));
            connectedCubes[3] = coordCubeMap.get(baseCoord.add(0,0,-1));
        }
        else if(baseEdge.equals(CONNECTED_EDGES[2])) {
            connectedCubes[1] = coordCubeMap.get(baseCoord.add(0,-1,0));
            connectedCubes[2] = coordCubeMap.get(baseCoord.add(-1,-1,0));
            connectedCubes[3] = coordCubeMap.get(baseCoord.add(-1,0,0));
        }
        else if(baseEdge.equals(CONNECTED_EDGES[3])) {
            connectedCubes[1] = coordCubeMap.get(baseCoord.add(1,0,0));
            connectedCubes[2] = coordCubeMap.get(baseCoord.add(1,0,-1));
            connectedCubes[3] = coordCubeMap.get(baseCoord.add(0,0,-1));
        }
        else if(baseEdge.equals(CONNECTED_EDGES[4])) {
            connectedCubes[1] = coordCubeMap.get(baseCoord.add(0,1,0));
            connectedCubes[2] = coordCubeMap.get(baseCoord.add(0,1,-1));
            connectedCubes[3] = coordCubeMap.get(baseCoord.add(0,0,-1));
        }
        else if(baseEdge.equals(CONNECTED_EDGES[5])) {
            connectedCubes[1] = coordCubeMap.get(baseCoord.add(0,1,0));
            connectedCubes[2] = coordCubeMap.get(baseCoord.add(1,1,0));
            connectedCubes[3] = coordCubeMap.get(baseCoord.add(1,0,0));
        }
        else if(baseEdge.equals(CONNECTED_EDGES[6])) {
            connectedCubes[1] = coordCubeMap.get(baseCoord.add(-1,0,0));
            connectedCubes[2] = coordCubeMap.get(baseCoord.add(-1,1,0));
            connectedCubes[3] = coordCubeMap.get(baseCoord.add(0,1,0));
        }
        else if(baseEdge.equals(CONNECTED_EDGES[7])) {
            connectedCubes[1] = coordCubeMap.get(baseCoord.add(0,1,0));
            connectedCubes[2] = coordCubeMap.get(baseCoord.add(0,1,1));
            connectedCubes[3] = coordCubeMap.get(baseCoord.add(0,0,1));
        }
        else if(baseEdge.equals(CONNECTED_EDGES[8])) {
            connectedCubes[1] = coordCubeMap.get(baseCoord.add(-1,0,0));
            connectedCubes[2] = coordCubeMap.get(baseCoord.add(-1,0,1));
            connectedCubes[3] = coordCubeMap.get(baseCoord.add(0,0,1));
        }
        else if(baseEdge.equals(CONNECTED_EDGES[9])) {
            connectedCubes[1] = coordCubeMap.get(baseCoord.add(1,0,0));
            connectedCubes[2] = coordCubeMap.get(baseCoord.add(1,-1,0));
            connectedCubes[3] = coordCubeMap.get(baseCoord.add(0,-1,0));
        }
        else if(baseEdge.equals(CONNECTED_EDGES[10])) {
            connectedCubes[1] = coordCubeMap.get(baseCoord.add(1,0,0));
            connectedCubes[2] = coordCubeMap.get(baseCoord.add(1,0,1));
            connectedCubes[3] = coordCubeMap.get(baseCoord.add(0,0,1));
        }
        else if(baseEdge.equals(CONNECTED_EDGES[11])) {
            connectedCubes[1] = coordCubeMap.get(baseCoord.add(0,-1,0));
            connectedCubes[2] = coordCubeMap.get(baseCoord.add(0,-1,1));
            connectedCubes[3] = coordCubeMap.get(baseCoord.add(0,0,1));
        }
        DoubleMatrix quadPoints = DoubleMatrix.zeros(4, 3);
        for(int i = 0; i < connectedCubes.length; i++) {
            if(connectedCubes[i] == null) return null;
            quadPoints.putRow(i, connectedCubes[i].cubeInterpPoint);
        }
        return quadTriangleSplit(quadPoints);////new Facet[] {new Facet(quadPoints)};// 
    }
    
    //requires quadPoints be ordered in any way that allows you to connect the dots between adjacent points to form the quad
    private static Facet[] quadTriangleSplit(DoubleMatrix quadPoints) {
        DoubleMatrix points1 = DoubleMatrix.zeros(3,3);
        DoubleMatrix points2 = DoubleMatrix.zeros(3,3);
        points1.putRow(0, quadPoints.getRow(0));
        points1.putRow(1, quadPoints.getRow(1));
        points1.putRow(2, quadPoints.getRow(3));
        
        points2.putRow(0, quadPoints.getRow(1));
        points2.putRow(1, quadPoints.getRow(2));
        points2.putRow(2, quadPoints.getRow(3));
        return new Facet[] {new Facet(points1), new Facet(points2)};
    }
    
}
