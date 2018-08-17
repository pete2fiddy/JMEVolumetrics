package mygame.control.ui.controller;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mygame.control.NavigationController;
import mygame.control.ui.Updatable;
import mygame.model.data.ml.similarity.jblas.JblasRadialBasisSimilarity;
import mygame.model.data.ml.similarity.jme.JMECosAngleSquaredSimilarity;
import mygame.model.data.ml.similarity.jme.JMERadialBasisSimilarity;
import mygame.model.data.search.KDTree;
import mygame.model.data.search.NearestNeighborSearcher;
import mygame.model.data.search.PointSelectBFSNearestNeighborSearch;
import mygame.model.graph.Graph;
import mygame.model.graph.OnTheFlySimilarityGraph;
import mygame.model.graph.SimilarityGraphUtil;
import mygame.model.pointcloud.InteractivePointCloudManipulator;
import mygame.model.segment.FloodfillSegmenter;
import mygame.model.segment.PaintbrushSegmenter;
import mygame.model.segment.Segmenter;
import mygame.model.segment.SegmenterVisitor;
import mygame.model.segment.SelectionSimilarityConstrainedPaintbrushSegmenter;
import mygame.model.segment.SelectionSimilarityConstrainedPaintbrushSegmenter.SelectionSimilarityConstrainedPaintbrushSegmenterArgs;
import mygame.model.volumetrics.CloudNormal;
import mygame.model.volumetrics.scalarfield.HoppeMeshMaker;
import mygame.model.volumetrics.scalarfield.ScalarField;
import mygame.model.volumetrics.Volume;
import mygame.model.volumetrics.VolumeSolver;
import mygame.model.volumetrics.scalarfield.RBFMeshMaker;
import mygame.model.volumetrics.surfaceextraction.NaiveSurfaceNet;
import mygame.model.volumetrics.surfaceextraction.convexhull.ConvexHull;
import mygame.util.GraphUtil;
import mygame.util.JblasJMEConverter;
import mygame.util.MeshUtil;
import mygame.util.PointUtil;
import mygame.util.VolumeUtil;
import org.jblas.DoubleMatrix;


public class Controller implements Updatable, SegmenterVisitor<Set<Integer>> {
    private static final double RECONSTRUCTION_ISO_VALUE = 0;
    private static final int N_NORMALS_NEIGHBOR = 20, N_ORIENTATION_NEIGHBORS = 12;
    private static final double SURFACE_NETS_CUBE_DIM = 0.25;
    private final Material MODEL_MAT;
    private static final ColorRGBA MODEL_COLOR = new ColorRGBA(0f,1f,1f,1f);
    private UIController<GraphType, SegmenterType> input;
    private InteractivePointCloudManipulator model;
    private NearestNeighborSearcher neighborSearcher;
    private PointSelectBFSNearestNeighborSearch screenNeighborSearcher;
    private Map<GraphType, Graph> graphMap = new HashMap<GraphType, Graph>();
    private Map<SegmenterType, Segmenter> segmenterMap = new HashMap<SegmenterType, Segmenter>();
    private Set<Integer> selectedPoints = new HashSet<Integer>();
    private DoubleMatrix points;
    private DoubleMatrix normals;
    private Volume activeModel = null;
    private Geometry activeModelGeom = null;
    private NavigationController navController;
    
    public Controller(AssetManager assetManager, InputManager inputManager, Camera cam, InteractivePointCloudManipulator model) {
        this.navController = new NavigationController(inputManager);
        this.navController.attachChildren(model.getCloud().getCloudNode());
        Vector3f[] pointsVec = model.getPointClones();
        this.MODEL_MAT = getModelMat(assetManager);
        this.points = JblasJMEConverter.toDoubleMatrix(pointsVec);
        
        this.model = model;
        this.input = new UIController<GraphType, SegmenterType>(this, inputManager, GraphType.class, SegmenterType.class);
        this.neighborSearcher = new KDTree(JblasJMEConverter.toArr(pointsVec));
        this.normals = CloudNormal.getUnorientedPCANormals(points, neighborSearcher, N_NORMALS_NEIGHBOR);
        fillMaps(pointsVec, CloudNormal.getUnorientedPCANormals(JblasJMEConverter.toDoubleMatrix(pointsVec), neighborSearcher, N_NORMALS_NEIGHBOR));
        initScreenNeighborSearcher(cam, pointsVec);
    }
    
    private Material getModelMat(AssetManager assetManager) {
        Material out = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        out.setColor("Color", MODEL_COLOR);
        out.getAdditionalRenderState().setWireframe(true);
        out.getAdditionalRenderState().setLineWidth(1);
        return out;
    }
    
    private void initScreenNeighborSearcher(Camera cam, Vector3f[] pointsVec) {
        this.screenNeighborSearcher = new PointSelectBFSNearestNeighborSearch(cam, pointsVec);
        Thread t = new Thread(this.screenNeighborSearcher);
        t.setDaemon(true);//must be set to daemon or else it won't close when the JME window is closed
        t.start();
    }
    
    private void fillMaps(Vector3f[] points, DoubleMatrix normals) {
        segmenterMap.put(SegmenterType.FLOODFILL, new FloodfillSegmenter());
        segmenterMap.put(SegmenterType.PAINTBRUSH, new PaintbrushSegmenter());
        segmenterMap.put(SegmenterType.CONSTRAINED_PAINTBRUSH, new SelectionSimilarityConstrainedPaintbrushSegmenter());

        graphMap.put(GraphType.SPARSE_DISTANCE, SimilarityGraphUtil.constructSparseSimilarityGraph(points, new JMERadialBasisSimilarity(), neighborSearcher, 6));
        graphMap.put(GraphType.SPARSE_ANGLE, SimilarityGraphUtil.constructSparseSimilarityGraph(JblasJMEConverter.toVector3f(normals), new JMECosAngleSquaredSimilarity(), neighborSearcher, 6));
        graphMap.put(GraphType.ON_THE_FLY_ANGLE, new OnTheFlySimilarityGraph(JblasJMEConverter.toVector3f(normals), new JMECosAngleSquaredSimilarity()));
    }
    
    private Set<Integer> segment(Segmenter s) {
        return (Set<Integer>)s.accept(this);
    }
    
    @Override
    public Set<Integer> visit(PaintbrushSegmenter brushSegmenter) {
        int nearestScreenNeighbor = getNearestScreenNeighbor();
        if(nearestScreenNeighbor < 0) return new HashSet<Integer>();
        PaintbrushSegmenter.PaintbrushSegmenterArgs args = new PaintbrushSegmenter.PaintbrushSegmenterArgs(neighborSearcher, 
                points.getRow(nearestScreenNeighbor).toArray(),
                input.getSegmentRadius());
        
        return brushSegmenter.segment(args);
    }

    @Override
    public Set<Integer> visit(SelectionSimilarityConstrainedPaintbrushSegmenter constrainedPaintSegmenter) {
        int nearestScreenNeighbor = getNearestScreenNeighbor();
        if(nearestScreenNeighbor < 0) return new HashSet<Integer>();
        SelectionSimilarityConstrainedPaintbrushSegmenterArgs args = new SelectionSimilarityConstrainedPaintbrushSegmenterArgs(
                graphMap.get(input.getActiveGraph()), 
                neighborSearcher, 
                points.getRow(nearestScreenNeighbor).toArray(), 
                input.getSegmentRadius(), 
                input.getSegmentTolerance());
        return constrainedPaintSegmenter.segment(args);
    }
    
    @Override
    public Set<Integer> visit(FloodfillSegmenter fillSegmenter) {
        int nearestScreenNeighbor = getNearestScreenNeighbor();
        if(nearestScreenNeighbor < 0) return new HashSet<Integer>();
        FloodfillSegmenter.FloodfillSegmenterArgs args = new FloodfillSegmenter.FloodfillSegmenterArgs(graphMap.get(input.getActiveGraph()), nearestScreenNeighbor, input.getSegmentTolerance());
        return fillSegmenter.segment(args);
    }
    
    @Override
    public void update(float timePerFrame) {
        navController.update(timePerFrame);
        screenNeighborSearcher.setTransform(model.getCloud().getCloudNode().getWorldTransform().toTransformMatrix());
        if(input.getActiveSegmenter() != null) {
            if(input.actionActive(UIController.ActionType.SELECT_ACTION)) {
                if(!input.actionActive(UIController.ActionType.ERASE_ACTION)) {
                    selectedPoints.addAll(segment(segmenterMap.get(input.getActiveSegmenter())));
                } else {
                    selectedPoints.removeAll(segment(segmenterMap.get(input.getActiveSegmenter())));
                }
            }
        }
        if(input.actionActive(UIController.ActionType.CLEAR_ACTION)) {
            selectedPoints = new HashSet<Integer>();
        }
        updateActiveModel(input.getModelFitType());
        model.selectPoints(selectedPoints);
        model.update(timePerFrame);
        input.update(timePerFrame);
    }
    
    private int getNearestScreenNeighbor() {
        if(!input.actionActive(UIController.ActionType.SELECT_ACTION)) return -1;
        Vector2f selectPos = input.getSelectPos();
        if(selectPos == null) return -1;
        return screenNeighborSearcher.getNearestNeighborId(new Vector3f(selectPos.x, selectPos.y, 0), 10);
    }

    private void updateActiveModel(ModelFitType modelType) {
        DoubleMatrix pointSubset = getPointsSubset(points, selectedPoints);
        switch(modelType) {
            case HOPPE:
                DoubleMatrix normalsSubset = getPointsSubset(normals, selectedPoints);
                NearestNeighborSearcher subsetSearcher = new KDTree(pointSubset.toArray2());
                CloudNormal.hoppeOrientNormals(pointSubset, normalsSubset, subsetSearcher, N_ORIENTATION_NEIGHBORS);
                this.activeModel = NaiveSurfaceNet.getVolume(getReconstructionField(pointSubset, normalsSubset, subsetSearcher), 
                    RECONSTRUCTION_ISO_VALUE, 
                    PointUtil.getPointBounds3d(pointSubset), 
                    SURFACE_NETS_CUBE_DIM, 
                    1);
                VolumeUtil.useCloudNormalsToOrientFaces(subsetSearcher, normalsSubset, activeModel);
                
                break;
            case CONV_HULL:
                this.activeModel = ConvexHull.quickhull3d(pointSubset);
                break;
            case NONE:
                break;
        }
        if(modelType != ModelFitType.NONE) {
           if(this.activeModelGeom != null) model.getCloud().getCloudNode().detachChild(this.activeModelGeom);
           this.activeModelGeom = createModelGeometry(activeModel);
           model.getCloud().getCloudNode().attachChild(this.activeModelGeom);   
        }        
    }
    
    protected double calcActiveModelVolume() {
        if(this.activeModel == null) return -1;
        return VolumeSolver.calcVolume(this.activeModel);
    }
    
    private Geometry createModelGeometry(Volume v) {
        Mesh m = MeshUtil.createNoIndexMesh(v);
        Geometry out = new Geometry("volume geometry", m);
        out.setMaterial(MODEL_MAT);
        return out;
    }
    
    private ScalarField<DoubleMatrix> getReconstructionField(DoubleMatrix subsetPoints, DoubleMatrix orientedSubsetNormals, NearestNeighborSearcher subsetSearcher) {
        /*RBFMeshMaker out = new RBFMeshMaker(subsetPoints, orientedSubsetNormals, new JblasRadialBasisSimilarity(1));
        out.fit();
        return out;*/
        return new HoppeMeshMaker(subsetPoints, orientedSubsetNormals, subsetSearcher);
    }
    
    private DoubleMatrix getPointsSubset(DoubleMatrix vecs, Set<Integer> inds) {
        DoubleMatrix pointSubset = DoubleMatrix.zeros(inds.size(), 3);
        int i = 0;
        for(int id : inds) {
            pointSubset.putRow(i++, vecs.getRow(id));
        }
        return pointSubset;
    }
    
    
    private static enum GraphType {
        ON_THE_FLY_ANGLE("On the fly angle"), SPARSE_DISTANCE("Sparse distance"), SPARSE_ANGLE("Sparse angle");
        private final String BUTTON_TEXT;
        private GraphType(String buttonText) {this.BUTTON_TEXT = buttonText;}
        @Override
        public String toString() {return BUTTON_TEXT;}
    }

    private static enum SegmenterType {
        PAINTBRUSH("Paintbrush"), FLOODFILL("Floodfill"), CONSTRAINED_PAINTBRUSH("Constrained paintbrush");
        private final String BUTTON_TEXT;
        private SegmenterType(String buttonText) {this.BUTTON_TEXT = buttonText;}
        @Override
        public String toString() {return BUTTON_TEXT;}
    }
    
    public void setParent(Node node){ 
        navController.setParent(node);
    }
    
    protected Map<String, Trigger> getBindings() {
        Map<String, Trigger> out = navController.getAnalogBindings();
        out.putAll(navController.getDiscreteBindings());
        out.putAll(input.getBindings());
        return out;
    }
    
    protected static enum ModelFitType {
        HOPPE, CONV_HULL, NONE;
    }
}
