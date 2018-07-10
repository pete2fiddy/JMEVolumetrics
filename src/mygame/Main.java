package mygame;

import mygame.input.VolumetricsCamera;
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
import mygame.ml.JMEInvEuclidianSimilarity;
import mygame.ml.JMEKMeansClusterer;
import mygame.ml.JMERadialBasisSimilarity;
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
    private InteractivePointCloud pointCloud;
    //see: https://wiki.jmonkeyengine.org/jme3/beginner/hello_material.html
    //for more info about transparent/non-opaque textures
    /*
    TODO: 
    
    Add a segmenter that segments based on relatively constant curvature
    
    Add a segmenter that grows a radius when user drags
    
    Add a paint brush sugmenter
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
        
        
        Vector3f[] points = generateSpheresVec3f(100000, new Vector3f[] {Vector3f.ZERO, new Vector3f(3f, -3f, 5f)}, 
                new float[] {2f, 3f});
        pointCloud = new InteractivePointCloud(assetManager, cam, points, new ColorRGBA(1f,0f,0f,1f), 10f, 
                inputManager,
                new JMEKMeansClusterer(1000, 10), 
                new JMERadialBasisSimilarity());
        pointCloud.enableNNSearchThread(true);
        volCam.attachChildren(pointCloud.getCloudNode());
        pointCloud.getCloudNode().setLocalTranslation(0f,0f,0f);
        
    }
    

    /* Use the main event loop to trigger repeating actions. */
    @Override
    public void simpleUpdate(float tpf) {
        volCam.update(tpf);
        pointCloud.update(tpf);
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
