package mygame;

import mygame.control.NavigationController;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import java.util.Comparator;
import mygame.model.data.search.KDTree;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.pointcloud.InteractivePointCloudManipulator;
import mygame.view.pointcloud.PointCloud;
import javax.swing.UIManager;
import mygame.control.ui.controller.Controller;
import mygame.model.data.search.priorityqueue.ArrayBinaryMinHeap;
import mygame.util.JblasJMEConverter;

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
    
    private NavigationController navController;
    private Controller cloudController;
    
    /*
    
    TODO: Orient model faces so that closest faces, instead of far, are buffered on top (flip all normals of model so it looks correct)
    
    TODO: Add model resolution slider
    
    TODO: Make arrayBinaryHeap and IndexTrackingArrayBinaryHeap more private to GraphUtil, or extend a more general implementation (array binary heap should ideally not give
    normal operation access to indices and stuff, and both are a bit confusing for general use).
    
    TODO: Process of calculating a mesh volume (orient normals -> surface net) MEGA slow
    
    TODO: Ground plane selection (fill in gaps wth points)
    
    TODO: Add a panel to say which selection type (e.g. erase, clear, select) is active to ToolboxInteractiveCloudManipulatorController, and contain buttons for each time
    
    TODO: add some better compatibility checking (e.g. not selecting a graph type when using floodfill) to SimpleToolbarFrame (basically prevents crashes)
    
    TODO: Fix back-parts of cloud being selected by the bfs nearest enighbor search when cloud is sparse (problem gets better if you zoom out more)
    
    TODO: need to enable depth buffering. With point clouds, points definitely in front can get painted behind.
    
    TODO: Speed up surface net
    
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
        navController = new NavigationController(inputManager);
        navController.attachCamera(rootNode);
        
        
        //testQueue();
        
        test();
        //figure out how to close the UI frame when main frame is closed
        //make everything exit on escape
        //InteractivePointCloudToolController frame = new SegmenterSelectFrame(pointCloudController, 200, 700);
    }
    
    
    private void testQueue() {
        
        Integer[] data = {1,3,4,6,5,7,10,12,9};
        
        ArrayBinaryMinHeap<Integer> heap = new ArrayBinaryMinHeap(data.length);
        for(int d : data) {
            heap.add(d, d);
        }
        while(heap.size() > 0) {
            System.out.println("min: " + heap.pop());
        }
    }
    
    
    private void test() {
        Vector3f[] points = Test.generateSpheresVec3f(25000, new Vector3f[] {new Vector3f(0f, 0f, 0f), new Vector3f(-3f, -4f, -3f)}, new float[] {2f, 1f});
        
        PointCloud pointCloud = new PointCloud(assetManager, points, new ColorRGBA(1f, 0f, 0f, 1f), 20f);
        
        
        this.cloudController = new Controller(assetManager, inputManager, cam, new InteractivePointCloudManipulator(pointCloud));
        
        navController.attachChildren(pointCloud.getCloudNode());
        
        
        
        NearestNeighborSearcher pointsKDTree = new KDTree(JblasJMEConverter.toDoubleMatrix(points).toArray2());
        
        
        /*
        
        DoubleMatrix normals = CloudNormal.getUnorientedPCANormals(JblasJMEConverter.toDoubleMatrix(points), 
                pointsKDTree, 30);
        CloudNormal.hoppeOrientNormals(JblasJMEConverter.toDoubleMatrix(points), normals, pointsKDTree, 6);
        
        
        double cubeWidth = .5;
        //can leave holes if not enough points used for reconstruction. Likely just a flaw with Hoppe's isosurface function, though.
        Volume volume = NaiveSurfaceNet.getVolume(new HoppeMeshMaker(JblasJMEConverter.toDoubleMatrix(points), normals, pointsKDTree), 0, PointUtil.getPointBounds3d(
        JblasJMEConverter.toDoubleMatrix(points)), cubeWidth, 1);
        IndexedVolume indexedVolume = VolumeUtil.convertToIndexedVolume(volume, 0.000005);
        VolumeUtil.useCloudNormalsToOrientFaces(pointsKDTree, normals, indexedVolume);
        VolumeUtil.useCloudNormalsToOrientFaces(pointsKDTree, normals, volume);
        
        Map<NetCoord, SurfaceNetCube> cubeNet = NaiveSurfaceNet.getIntersectingCubes(new HoppeMeshMaker(JblasJMEConverter.toDoubleMatrix(points), normals, pointsKDTree), 0, PointUtil.getPointBounds3d(
        JblasJMEConverter.toDoubleMatrix(points)), cubeWidth);
        
        for(NetCoord key : cubeNet.keySet()) {
            Box b = cubeNet.get(key).getGeom();
            Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            m.setColor("Color", ColorRGBA.Yellow);
            m.getAdditionalRenderState().setWireframe(true);
            m.getAdditionalRenderState().setLineWidth(1f);
            Geometry g = new Geometry("Box", b);
            g.setMaterial(m);
            //pointCloud.getCloudNode().attachChild(g);
        }
        
        
        
        Mesh volumeMesh = MeshUtil.createNoIndexMesh(volume);//createIndexedMesh(indexedVolume);
        Geometry volumeGeom = new Geometry("volume geometry", volumeMesh);
        Geometry volumeFrameGeom = new Geometry("volume frame geometry", volumeMesh);
        Material volumeMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material volumeFrameMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        volumeFrameMat.setColor("Color", new ColorRGBA(1f,1f,1f,1f));
        volumeFrameMat.getAdditionalRenderState().setWireframe(true);
        volumeFrameMat.getAdditionalRenderState().setLineWidth(1f);
        volumeMat.setColor("Color", new ColorRGBA(1f,0f,1f, .5f));
        volumeGeom.setMaterial(volumeMat);
        volumeFrameGeom.setMaterial(volumeFrameMat);
        //pointCloud.getCloudNode().attachChild(volumeGeom);
        //pointCloud.getCloudNode().attachChild(volumeFrameGeom);
        
        System.out.println("VOLUME not indexed: " + VolumeSolver.calcVolume(volume));
        System.out.println("VOLUME indexed: " + VolumeSolver.calcVolume(indexedVolume));
        System.out.println("non indexed num facets: " + volume.numFacets());
        System.out.println("indexed num facets: " + indexedVolume.numFacets());
        */
        
    }
    
    /* Use the main event loop to trigger repeating actions. */
    @Override
    public void simpleUpdate(float tpf) {
        navController.update(tpf);
        if(cloudController != null) cloudController.update(tpf);
    }
    
    
}
