package mygame;

import mygame.control.VolumetricSceneController;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import mygame.model.data.search.KDTree;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.pointcloud.InteractivePointCloudManipulator;
import mygame.view.pointcloud.PointCloud;
import defunct.SegmenterControllerImpl;
import defunct.SegmenterController;
import mygame.control.ui.ToolboxInteractiveCloudManipulatorController;
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
    
    
    private VolumetricSceneController volCam;
    //private SegmenterController cloudController;
    private ToolboxInteractiveCloudManipulatorController cloudController;
    
    /*
    
    TODO: Create segmentation methods with minimal arguments (e.g. floodfill: give graph, start node, tolerance)
    
   
    
    
    
    
    
    TODO: Create a method invoker using the command pattern
    
    TODO: Create an abstract extension of the method invoker, with an abstract method that updates all fields the controller would have access to, and has
    another abstract method to invoke the m
    
    TODO: Move graph algos from segmenters into graph utils, or perhaps dedicate classes to graph algos since graphutils is getting pretty big. Make segmenter's 
    sole job to perform graph algorithm given the minimum necessary params (which are completely detached from any kind of UI at all, thatis the control's job to
    tell the model what the params are)
    
    Control's job is to select the currnetly active segmenter and tell it what commands to use that are constructed using UI input (slider for tolerance, etc.)

    TODO: Use reflection to hold a segmentation method along with its arguments, since number of arguments can vary.
    
    TODO: Remove dud classes
    
    TODO: When switching segmentation types, sometimes segmented point list gets wiped. Not sure of a good way to fix --
    have to somehow pass the actively selected points from the old segmenter to the new one
    
    TODO: Find a better looking way to show a button as selected
    
    TODO: Finish implementing a clicky panel UI.
    
    TODO: Add buttons for erase toggle, etc for painting segmentation.
    
    TODO: Fix segmenters having their own selction pools when switching between them. Somehow pool their selection pool? (i.e. I might want to floodfill a face then paint another one,
    but switching to paint clears the floodfill)
    
    TODO: need to enable depth buffering. With point clouds, points definitely in front can get painted behind.
    
    TODO: Change InteractivePointCloud's selectPoint to selectPoint(int... ids) (same with unselect points)
    
    TODO: Better split the segmenters from their roles in the UI (i.e. create a segmenter, then a wrapper for the segmenter
    that lets it "talk" with Segmenter Controller). (wrapper funnels info from the controller into the segmenter, on update ideal so that
    segmenter never has to "know" about anything other than the sim graph and what is TOLD to it (rather than what it pulls))
    
    Organization":
    
    
    Change kmeans jme clusterer to use double matrix
    
    Create simple connected/not connected graph type without values.
    
    
    similarity metric compatiability with certain segmenters is hard to tell without looking at code (for example,
    constructing a distance weighted graph requires a JME metric and a JBlas metric)
    
    
    
    TODO: 
    
    Speed up graph recalculation when point moved in InteractivePointCloudManipulator
    
    Move the final grpahs from interactive point cloud to the implemetnation of SegmenterToolController
    
    Rename KeyboardSegmenterToolController to PeripheralSegmenterToolController (works with mouse and keyboard -- peripherals)
    
    After refactoring KDTree references into a more general NearestNeighborSearcher, lots of variables kept a name that specifically references KDTree. Change them.
    
    Note: cam.distanceToNearPlane does NOT return the same thing as cam.getScreenCoordinates(vec).getZ()! 
    
    Make it possible to get a subset of outedges from a graph node (useful so that not all connections to a node are calculated on the fly
    in the on the fly graph, if, for example, only nodes within a radius of the center are wanted)
    
    
    
    Add a segmenter that grows a radius when user drags
    
    
    
    -------------------
    //see: https://wiki.jmonkeyengine.org/jme3/beginner/hello_material.html
    //for more info about transparent/non-opaque textures
    */
    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        volCam = new VolumetricSceneController(inputManager);
        volCam.attachCamera(rootNode);
        
        
        test();
        //figure out how to close the UI frame when main frame is closed
        //make everything exit on escape
        //InteractivePointCloudToolController frame = new SegmenterSelectFrame(pointCloudController, 200, 700);
    }
    
    
    
    private void test() {
        Vector3f[] points = Test.generateCubesVec3f(25000, new Vector3f[] {new Vector3f(0f, 0f, 0f), new Vector3f(-3f, -4f, -3f)}, new float[] {2f, 1f});
        
        PointCloud pointCloud = new PointCloud(assetManager, points, new ColorRGBA(1f, 0f, 0f, 1f), 20f);
        
        //this.cloudController = new SegmenterControllerImpl(inputManager, cam,  new InteractivePointCloudManipulator(pointCloud));
        this.cloudController = new ToolboxInteractiveCloudManipulatorController(inputManager, cam, new InteractivePointCloudManipulator(pointCloud));
        //pointCloudController.enableNNSearchThread(true);
        volCam.attachChildren(pointCloud.getCloudNode());
        
        
        
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
        volCam.update(tpf);
        if(cloudController != null) cloudController.update(tpf);
    }
    
    
}
