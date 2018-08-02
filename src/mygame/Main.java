package mygame;

import mygame.pointcloud.InteractivePointCloud;
import mygame.input.VolumetricsCamera;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mygame.data.search.KDTree;
import mygame.graph.Graph;
import mygame.graph.SparseGraph;
import mygame.ml.similarity.jme.JMEKMeansClusterer;
import mygame.ml.similarity.jme.JMERadialBasisSimilarity;
import mygame.pointcloud.CloudPoint;
import mygame.pointcloud.LineCloud;
import mygame.pointcloud.PointCloud;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import mygame.util.MeshUtil;
import mygame.util.PointUtil;
import mygame.util.VolumeUtil;
import mygame.volumetrics.CloudNormal;
import mygame.volumetrics.surfaceextraction.convexhull.ConvexHull;
import mygame.volumetrics.Facet;
import mygame.volumetrics.HoppeMeshMaker;
import mygame.volumetrics.IndexedVolume;
import mygame.volumetrics.surfaceextraction.NetCoord;
import mygame.volumetrics.surfaceextraction.NaiveSurfaceNet;
import mygame.volumetrics.surfaceextraction.SurfaceNetCube;
import mygame.volumetrics.surfaceextraction.SurfaceNetCube;
import mygame.volumetrics.Volume;
import mygame.volumetrics.VolumeSolver;
import org.jblas.DoubleMatrix;
import org.lwjgl.opengl.GL11;

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
    
    
    /*
    
    TODO: ensure SimpleSurfaceNet creates surfaces with all CCW faces (possible to guarantee by setting a specific order
    for how the neighbors to a NetCoord are returned?)
    ---------------------------------------------------------------
    
    TODO: create a function that converts a Volume to a PointSubsetVolume, merging very near points to one, creating a global points list, and
    setting the faces to have indexes for the appropriate vertices
    
    TODO: no reason to store DoubleMatrix in PointSubsetFacets (extra memory, would be better to just pass point set when needed) --
    find some way to extend Facet that doesn't require the points. Could modify facet to store a generic for args (i.e. index facet would store an Integer[] array,
    while normal facet without indices would store a DoubleMatrix[] array)
    
    
    
    TODO: SimpleSurfaceNet sometimes appears to have illegal edge connections that cross interior/other facets. Seems to only appear in areas of large curvature -- am
    guessing it has something to do with the 3d marchiing cube border search window -- perhaps make the neighborhood of a coord only 4 coordinates,
    oriented to best fit the tangent around its neighborhood? (Using smaller cube width minimizes (minimizes the surface area of erroneous triangles)
    the issue, but it is still present). (Occurs when diagonal cubes are connected when coplanar cubes make a better fit
    to the mesh. One route is to use the calculated normals to remove faces that strongly disagree with the normal,
    but isn't an ideal fix)
    
    TODO: Figure out how to display wireframes/3d meshes -- using line clouds takes up a lot of memory. (see: https://wiki.jmonkeyengine.org/jme3/advanced/custom_meshes.html)
    
    NOTE: Normal orientation on cubes (because they have such sharp angles) requires a large PCA window (num neighbors) and/or a smaller
    orientation neighbor flip search window (num neighbors) to work correctly (not doing so will leave holes in the generated model)
    
    TODO: oritented normals from calculation process more accurate than Volume's cross product -- use indices of facets to 
    just average the normals calculated from my method when calculating volumes
    
    TODO: need to enable depth buffering. With point clouds, points definitely in front can get painted behind.
    
    
    Organization":
  
    Change hash code on int arr implementation to use HashUtil's methods
    
    Change kmeans jme clusterer to usedouble matrix
    
    Note: Many graph sparsity constraints prevent selection types that instead only check similarity to the click selection point
    from working correctly (they do not contain direct links to all candidate points being checked)
    
    Create simple connected/not connected graph type without values.
    
    Figure out how to segment, sensibly, between JBLAS and JME vectors. Irritating to keep switching back and forth and 
    implement overloaded methods to address both (i'm thinking default to Vector3f then switch to DoubleMatrix wherever more intense
    math is needed)
    
    similarity metric compatiability with certain segmenters is hard to tell without looking at code (for example,
    constructing a distance weighted graph requires a JME metric and a JBlas metric)
    
    
    Nice-to-haves:
    Double-edged graphs (doesn't speed anythign up, since nodes still need a double reference to each other, but prevents
    the possibility of the programmer forgetting to make a graph symmetrical)
    
    TODO: 
    
    Create an interface for searching for points, and implement with KDTree. Allows for point search methods that aren't kdtree to be passed around instead. (general point search can be used instead of only KDTree)
    
    BIG BUG; Check all uses of cam.distanceToNearPlane -- does NOT return the same thing as cam.getScreenCoordinates(vec).getZ()!
    
    Make it possible to get a subset of outedges from a graph node (useful so that not all connections to a node are calculated on the fly
    in the on the fly graph, if, for example, only nodes within a radius of the center are wanted)
    
    Can make graphs work with all points, but be very sparse, by utilizing the KDTree when constructing
    the graphs so that sufficiently far points do not have connection entries (use n nearest neighbors, or local
    radius) (doing so VERY likely requires
    switching from using matrices to an object structure to represent the graphs)
    (Be wary when doing so, makes a segmenter like SimilarityToSelectionPointCloudSEgmenter not able to
    work since it doesn't have entries from each point to every point)
    (better idea: create a nested object graph -- one with centroids, one without that is sparse, and
    the segmenters choose the one that better fits what it does. Represent this as graphMap<String (graph name), Graph>)
    
    
    Add a segmenter that grows a radius when user drags
    
    (Eventually...) create the model-to-cloud-fit algorithm
    
    
    
    -------------------
    //see: https://wiki.jmonkeyengine.org/jme3/beginner/hello_material.html
    //for more info about transparent/non-opaque textures
    */
    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        volCam = new VolumetricsCamera(inputManager);
        volCam.attachCamera(rootNode);
        test();
        
    }
    
    
    
    private void test() {
        
        
        Vector3f[] points = Test.generateSphereVec3f(10000, Vector3f.ZERO, 2f);
        pointCloud = new InteractivePointCloud(assetManager, cam, points, new ColorRGBA(1f,0f,0f,0f), 40f, 
                inputManager);
        pointCloud.enableNNSearchThread(true);
        volCam.attachChildren(pointCloud.getCloudNode());
        //pointCloud.getCloudNode().setLocalTranslation(0f,0f,0f);
        
        
        
        KDTree pointsKDTree = new KDTree(JblasJMEConverter.toDoubleMatrix(points).toArray2());
        
        
        
        
        DoubleMatrix normals = CloudNormal.getUnorientedPCANormals(JblasJMEConverter.toDoubleMatrix(points), 
                pointsKDTree, 30);
        CloudNormal.hoppeOrientNormals(JblasJMEConverter.toDoubleMatrix(points), normals, pointsKDTree, 6);
        
        //LineCloud normalCloud = new LineCloud(assetManager, points, JblasJMEConverter.toVector3f(normals), 1);
        //pointCloud.getCloudNode().attachChild(normalCloud.getCloudNode());
        
        double cubeWidth = .25
                ;
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
        volumeMat.setColor("Color", new ColorRGBA(1f,0f,0f, 1f));
        volumeGeom.setMaterial(volumeMat);
        volumeFrameGeom.setMaterial(volumeFrameMat);
        //pointCloud.getCloudNode().attachChild(volumeGeom);
        pointCloud.getCloudNode().attachChild(volumeFrameGeom);
        
        System.out.println("VOLUME not indexed: " + VolumeSolver.calcVolume(volume));
        System.out.println("VOLUME indexed: " + VolumeSolver.calcVolume(indexedVolume));
        System.out.println("non indexed num facets: " + volume.numFacets());
        System.out.println("indexed num facets: " + indexedVolume.numFacets());
        
    }
    
    /* Use the main event loop to trigger repeating actions. */
    @Override
    public void simpleUpdate(float tpf) {
        volCam.update(tpf);
        pointCloud.update(tpf);
    }
    
    
}
