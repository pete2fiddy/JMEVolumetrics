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
        
        
        Vector3f[] points = generatePointsVec3f(100000);
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
    
    private FloatBuffer generatePoints(int nPoints){
        FloatBuffer out = BufferUtils.createFloatBuffer(nPoints*3);
        for(int i = 0; i < nPoints; i++){
            for(int j = 0; j < 3; j++){
                out.put((float)(10*(Math.random()-.5)));
            }
        }
        return out;
    }
    
    private Vector3f[] generatePointsVec3f(int nPoints) {
        Vector3f[] out = new Vector3f[nPoints];
        for(int i = 0; i < nPoints; i++){
            out[i] = new Vector3f();
            for(int j = 0; j < 3; j++){
                out[i].set(j, (float)(10*(Math.random()-.5)));
            }
        }
        return out;
    }
    
    private DoubleMatrix generateDoubleMatrixPoints(int nPoints){
        DoubleMatrix points = DoubleMatrix.zeros(nPoints, 3);
        for(int i = 0; i < points.rows; i++){
            points.putRow(i, new DoubleMatrix(new double[][] {{(10*(Math.random()-.5)),
            (10*(Math.random()-.5)),
            (10*(Math.random()-.5))}}).transpose());
        }
        return points;
    }
}
