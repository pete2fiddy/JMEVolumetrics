package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import mygame.ml.KMeans;
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
    private PointCloud pointCloud;
    //see: https://wiki.jmonkeyengine.org/jme3/beginner/hello_material.html
    //for more info about transparent/non-opaque textures
    
    /*
    Notes: if wanted a first person camera, create a camera position node, then attach a rotation node as its parent. Then rotate the rotation node to rotate
    about camera pos, and translate the camera position mode to translate everything. Attach all things to be transformed to the camera position node
    */
    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        volCam = new VolumetricsCamera(inputManager);
        volCam.attachCamera(rootNode);
        
        /*
        DoubleMatrix doubleMatPoints = generateDoubleMatrixPoints(100);
        DoubleMatrix centroids = KMeans.calcKMeansCentroids(doubleMatPoints, 1000, 50);
        System.out.println("centroids: " + centroids);
        */
        
        /*
        Box cube1Mesh = new Box(1f,1f,1f);
        Geometry cube1Geo = new Geometry("Textured cube 1", cube1Mesh);
        cube1Geo.setLocalTranslation(0f,0f,0f);
        Material cube1Mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture cube1Tex = assetManager.loadTexture("Textures/BrickTexture.jpg");
        cube1Mat.setTexture("ColorMap", cube1Tex);
        cube1Geo.setMaterial(cube1Mat);
        
        volCam.attachChildren(cube1Geo);
        */
        
        
        Vector3f[] points = generateSpheresVec3f(500000, new Vector3f[] {Vector3f.ZERO, new Vector3f(3f, -3f, 5f)}, 
                new float[] {2f, 3f});
        pointCloud = PointCloud.initWithFixedColorAndSize(assetManager, cam, points, new ColorRGBA(1f,0f,0f,1f), 10f);
        pointCloud.enableNNSearchThread(true);
        volCam.attachChildren(pointCloud.getCloudNode());
        pointCloud.getCloudNode().setLocalTranslation(0f,0f,0f);
        
    }
    

    /* Use the main event loop to trigger repeating actions. */
    @Override
    public void simpleUpdate(float tpf) {
        // make the player rotate:
        //player.rotate(0, 2*tpf, 0);
        volCam.update(tpf);
        /*for(int i = 0; i < pointCloud.numPoints(); i++) {
            pointCloud.setColor(i, new ColorRGBA((float)Math.random(),
            (float)Math.random(),
            (float)Math.random(),
            (float)Math.random()));
            pointCloud.setSize(i, (float)(Math.random() * 30));
        }*/
        long startTime = System.nanoTime();
        int closestPointId = pointCloud.getNearestScreenNeighborId(inputManager.getCursorPosition());
        if(closestPointId != -1) {
            System.out.println("Point search time ms: " + Double.toString((double)(System.nanoTime() - startTime)/(1000000.0)));
            pointCloud.setColor(closestPointId, ColorRGBA.White);
            pointCloud.setSize(closestPointId, 50f);
            pointCloud.update(tpf);
        }
        
    }
    
    private Vector3f[] generateCubeVec3f(int nPoints) {
        Vector3f[] out = new Vector3f[nPoints];
        for(int i = 0; i < nPoints; i++){
            out[i] = new Vector3f();
            for(int j = 0; j < 3; j++){
                out[i].set(j, (float)(10*(Math.random()-.5)));
            }
        }
        return out;
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
