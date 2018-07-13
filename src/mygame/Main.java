package mygame;

import mygame.pointcloud.InteractivePointCloud;
import mygame.input.VolumetricsCamera;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Line;
import java.util.ArrayList;
import java.util.HashSet;
import mygame.data.search.JblasKDTree;
import mygame.ml.JMEKMeansClusterer;
import mygame.ml.JMERadialBasisSimilarity;
import mygame.pointcloud.LineCloud;
import mygame.util.JblasJMEConverter;
import mygame.volumetrics.CloudNormal;
import org.jblas.DoubleMatrix;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication {
    
    
    
    public static void main(String[] args){
        Main app = new Main();
        app.start();
    }
    
    
    private VolumetricsCamera volCam;
    private InteractivePointCloud pointCloud;
    //see: https://wiki.jmonkeyengine.org/jme3/beginner/hello_material.html
    //for more info about transparent/non-opaque textures
    /*
    TODO: 
    
    MODIFY PAINT BRUSH SEARCHING TO FIRST SEARCH FOR INITIAL POINT USING BFS, THEN SEARCH FOR THE REST USING A NORMAL,
    NONTRANSFORMABLE KDTREE!!!
    
    Use idBuffer -- instead of cycling through all points -- for stuff like the spherical paint brush segmenter (reduce all points into a subset of those
    that appear near the center point in the idBuffer, then do the same comparisons as now but on that severely restricted subset)
    
    similarity metric compatiability with certain segmenters is hard to tell without looking at code (for example,
    constructing a distance weighted graph requires a JME metric and a JBlas metric)
    
    Somehow modify pointcloud and BFSNearestNeighborSearch so they can sensibly buffer together, and allows pointcloud to track which points are visible on screen
    during the construction of the idBuffer. Then the set of points visible can be used to remove invisible points from computation during segmentation, etc. (not sure
    how useful this actually ends up being?)
    
    Add a segmenter that segments based on relatively constant curvature (clicking and dragging segments flat walls, etc.) (note this is actually accomplishable
    by passing a graph to SimilarityTHresholdedFloodfillCloudSegmenter with a simGraph where each edge represents the similarity of CURVATURE between two centroids.
    I assume it would work especially well with a clustering algorithm that specifically looks for constant curvature within space)
    
    Add a paintbrush that does not let you "paint outside of the lines" (meaning you can't paint into another area with a major difference in curvature
    without releasing and starting a new stroke in that area)
    
    
    Add a segmenter that grows a radius when user drags
    
    Add a flat paint brush segmenter
    
    (Eventually...) create the model-to-cloud-fit algorithm
    */
    /*
    Notes: if wanted a first person camera, create a camera position node, then attach a rotation node as its parent. Then rotate the rotation node to rotate
    about camera pos, and translate the camera position mode to translate everything. Attach all things to be transformed to the camera position node
    */
    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        volCam = new VolumetricsCamera(inputManager);
        volCam.attachCamera(rootNode);
        
        
        Vector3f[] points = generateSpheresVec3f(10000, new Vector3f[] {Vector3f.ZERO, new Vector3f(3f, -3f, 5f)}, 
                new float[] {2f, 3f});
        
        pointCloud = new InteractivePointCloud(assetManager, cam, points, new ColorRGBA(1f,0f,0f,1f), 10f, 
                inputManager);
        pointCloud.enableNNSearchThread(true);
        volCam.attachChildren(pointCloud.getCloudNode());
        pointCloud.getCloudNode().setLocalTranslation(0f,0f,0f);
        
        
        DoubleMatrix jblasPoints = DoubleMatrix.zeros(points.length, 3);
        for(int i = 0; i < jblasPoints.rows; i++) {
            jblasPoints.putRow(i, new DoubleMatrix(new double[][] {{points[i].x, points[i].y, points[i].z}}));
        }
        
        DoubleMatrix pointNormals = CloudNormal.getUnorientedPCANormals(jblasPoints, 15);
        
        
        
        
        
        LineCloud lineCloud = new LineCloud(assetManager, points, JblasJMEConverter.toVector3f(pointNormals), .25f);
        volCam.attachChildren(lineCloud.getCloudNode());
        /*
        float lineMagnitude = .25f;
        Geometry[] lineGeoms = new Geometry[pointNormals.rows];
        for(int i = 0; i < pointNormals.rows; i++) {
            Vector3f start = points[i];
            Vector3f end = start.add((new Vector3f((float)pointNormals.get(i,0), 
            (float)pointNormals.get(i,1),
            (float)pointNormals.get(i,2))).mult(lineMagnitude));
            Line l = new Line(start, end);
            lineGeoms[i] = new Geometry("Line " + Integer.toString(i), l);
            lineGeoms[i].setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
            volCam.attachChildren(lineGeoms[i]);
        }
        Line[] lineMeshes = new Line[pointNormals.rows];
        */
        
    }
    

    /* Use the main event loop to trigger repeating actions. */
    @Override
    public void simpleUpdate(float tpf) {
        volCam.update(tpf);
        //pointCloud.update(tpf);
    }
    
    private Vector3f[] generateCubesVec3f(int nPointsPerCube, Vector3f[] centers, float[] radiuses) {
        Vector3f[] out = new Vector3f[nPointsPerCube*centers.length];
        for(int cubeNum = 0; cubeNum < centers.length; cubeNum++) {
            Vector3f[] cube = generateCubeVec3f(nPointsPerCube, centers[cubeNum], radiuses[cubeNum]);
            for(int i = 0; i < cube.length; i++) {
                out[cubeNum*nPointsPerCube + i] = cube[i];
            }
        }
        return out;
    }
    
    private Vector3f[] generateCubeVec3f(int nPoints, Vector3f center, float radius) {
        float halfRadius = radius/2f;
        Matrix3f cubeBasises = new Matrix3f(halfRadius, 0f, 0f,
        0f, halfRadius, 0f,
        0f, 0f, halfRadius);
        Vector3f[] points = new Vector3f[nPoints];
        
        for(int i = 0; i < nPoints; i++) {
            
            float[][] mixMatArr = new float[3][3];
            int fixedSide = (int)(Math.random()*3);
            for(int j = 0; j < mixMatArr.length; j++){
                if(j == fixedSide) {
                    mixMatArr[j][j] = (float)((Math.random() < 0.5)? -1:1);
                } else {
                    mixMatArr[j][j] = (float)(2*(Math.random()-.5));
                }
            }
            Matrix3f mixMat = new Matrix3f();
            mixMat.set(mixMatArr);
            mixMat = mixMat.mult(cubeBasises);
            Vector3f point = center;
            for(int j = 0; j < 3; j++) {
                point = point.add(mixMat.getRow(j));
            }
            points[i] = point;
        }
        return points;
    }
    
    private Vector3f[] generateSpheresVec3f(int nPointsPerSphere, Vector3f[] centers, float[] radiuses) {
        Vector3f[] out = new Vector3f[nPointsPerSphere*centers.length];
        for(int sphereNum = 0; sphereNum < centers.length; sphereNum++) {
            Vector3f[] points = generateSphereVec3f(nPointsPerSphere, centers[sphereNum], radiuses[sphereNum]);
            for(int i = 0; i < nPointsPerSphere; i++){
                out[sphereNum*nPointsPerSphere + i] = points[i];
            }
        }
        return out;
    }
    
    private Vector3f[] generateSphereVec3f(int nPoints, Vector3f center, float r) {
        Vector3f[] out = new Vector3f[nPoints];
        for(int i = 0; i < nPoints; i++) {
            out[i] = new Vector3f();
            float z = (float)(2*r*(Math.random()-.5));
            float rHeight = (float)Math.sqrt((r*r - z*z));
            float theta = (float)(2*Math.PI*Math.random());
            out[i].setX((float)(rHeight * Math.sin(theta)));
            out[i].setY((float)(rHeight * Math.cos(theta)));
            out[i].setZ(z);
            out[i] = out[i].add(center);
        }
        return out;
    }
    
}
