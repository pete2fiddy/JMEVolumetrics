package mygame;

import mygame.control.NavigationController;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.model.data.search.KDTree;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.pointcloud.InteractivePointCloudManipulator;
import mygame.view.pointcloud.PointCloud;
import javax.swing.UIManager;
import mygame.control.ui.controller.Controller;
import mygame.model.data.load.SimpleCloudLoader;
import mygame.model.data.save.SimpleCloudSaver;
import mygame.model.data.search.priorityqueue.ArrayBinaryMinHeap;
import mygame.util.JblasJMEConverter;
import mygame.util.RandomUtil;
import mygame.view.pointcloud.CloudPoint;

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
    
    private Controller cloudController;
    
    /*
    
    TODO: Ground plane selection (fill in gaps wth points)
    
    TODO: Change "show controls" frame to be more informative about key input.
    
    TODO: Even fairly small clouds for whatever reason can take a long time to appear on screen...
    
    TODO: Convex hull fairly slow... maybe unavoidable because real models will have points all near the surface
    and few interior points
    
    TODO: Utilize convex hull for meshes that contain interior point because two separate mesh's points are merged
    together?
    
    TODO: Could create a more robust volume calculator given that all my models are fit and then surface-netted
    that just adds the volume of the contained surface net cubes... Would be much more resilient to holes.
    Would prefer not to implement in the same method as surface netthough, but running algorithm twice,
    once for model and once for volume, would also be a waste.
    
    TODO: RBFMeshMaker memory overflows on medium and greater sized point sets, and is slow. Fix. Can sparsify the system using a nearest neighbor searcher
    and restricting the number of neighbors used when summing the RBFs. Use when constructing and evaluating the rbf. If nto sparsified, the approach in general
    has poor runtime order since it solves a linear system with number of equations equal to the number of points.
    
    TODO: Add model resolution slider
    
   
    
    TODO: Add a panel to say which selection type (e.g. erase, clear, select) is active to ToolboxInteractiveCloudManipulatorController, and contain buttons for each time
    
    TODO: add some better compatibility checking (e.g. not selecting a graph type when using floodfill) to SimpleToolbarFrame (basically prevents crashes)
    
    TODO: Fix back-parts of cloud being selected by the bfs nearest enighbor search when cloud is sparse (problem gets better if you zoom out more)
    
    TODO: need to enable depth buffering. With point clouds, points definitely in front can get painted behind.
    
    TODO: Speed up surface net
    
    TODO: Clean up PrimsMinSpan implementation
    
    Organization":
    
    Create simple connected/not connected graph type without values.
    
    
    similarity metric compatiability with certain segmenters is hard to tell without looking at code (for example,
    constructing a distance weighted graph requires a JME metric and a JBlas metric)
    
    Nice to have: enable/disable segmentation sliders based on the properties of the active segmenter
    Nice to have: Disable selections that are illegal/unnecessary for UI (e.g. picking any graph not necessary for Paintbrush segmenter)
    Nice to have: Implement more graphs/segmenters (a distance weighted angle normal one, for example)
    Nice to have: add a sim metric that is just cos angle +1. This allows ORIENTED normals to be leveraged, and back faces, for example, to not be selected when a front
    face is segmented. Then implement to be selectable in UI
    
    TODO: 
    
    Speed up graph recalculation when point moved in InteractivePointCloudManipulator
    
    After refactoring KDTree references into a more general NearestNeighborSearcher, lots of variables kept a name that specifically references KDTree. Change them.
    
    Note: cam.distanceToNearPlane does NOT return the same thing as cam.getScreenCoordinates(vec).getZ()! 
    
    Make it possible to get a subset of outedges from a graph node (useful so that not all connections to a node are calculated on the fly
    in the on the fly graph, if, for example, only nodes within a radius of the center are wanted)
    
    -------------------
    //see: https://wiki.jmonkeyengine.org/jme3/beginner/hello_material.html
    //for more info about transparent/non-opaque textures
    */
    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        test();
    }
    
    
    private void test() {
        Vector3f[] points = Test.generateRandomShapes(1000, 3, 3);
        /*
        Vector3f[] points = null;
        try {
            points = SimpleCloudLoader.load("C:/Users/Owner/Desktop/saved_cloud.txt"); ////Test.generateCubesVec3f(10000, new Vector3f[] {new Vector3f(0f, 0f, 0f), new Vector3f(-3f, -4f, -3f)}, new float[] {2f, 1f});
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        */
        PointCloud pointCloud = new PointCloud(assetManager, points, new ColorRGBA(1f, 0f, 0f, 1f), 20f);
        
        
        this.cloudController = new Controller(assetManager, inputManager, cam, new InteractivePointCloudManipulator(pointCloud));
        cloudController.setParent(rootNode);
    }
    
    
    
    
    /* Use the main event loop to trigger repeating actions. */
    @Override
    public void simpleUpdate(float tpf) {
        if(cloudController != null) cloudController.update(tpf);
    }
    
    
}
