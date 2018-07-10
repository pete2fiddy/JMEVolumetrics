/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import mygame.util.MyBufferUtil;
import mygame.util.PointUtil;


public class PointCloud implements Updatable {
    private AssetManager assetManager;
    private final Vector3f[] points;
    private ColorRGBA[] colors;
    private float[] sizes;
    
    private Node cloudNode = new Node();
    private Material pointMat;
    private Mesh pointMesh;
    private Geometry cloudGeom;
    
    private boolean doUpdateColors = true, doUpdatePoints = true, doUpdateSizes = true;
    
    private Camera cam;
    
    
    /*
    PROBLEM: Particles not zbuffered? (if a point is large and white, and in front of others behind it, the others can be seen
    on top of it)
    */
    /*
    
    Code mostly copy-pasted from Ogli's source here: 
    https://hub.jmonkeyengine.org/t/how-to-create-a-mesh-from-cloud-points/17290/9
    See thread for hints about how to further optimize, but currently it can pretty easily handle 
    ~3 million points
    */
    public PointCloud(AssetManager assetManager, Camera cam, Vector3f[] points, ColorRGBA[] colors, float[] sizes) {
        this.assetManager = assetManager;
        this.points = points;
        this.colors = colors;
        this.sizes = sizes;
        this.cam = cam;
        
        initPointMat();
        initPointMesh();
        initCloudGeom();
        cloudNode.attachChild(cloudGeom);
    }
    
    
    public PointCloud(AssetManager assetManager, Camera cam, Vector3f[] points, 
            ColorRGBA color, float size){
        this.sizes = new float[points.length];
        this.colors = new ColorRGBA[points.length];
        for(int i = 0; i < this.sizes.length; i++){
            this.sizes[i] = size;
            this.colors[i] = color;
        }
        this.assetManager = assetManager;
        this.points = points;
        this.cam = cam;
        
        initPointMat();
        initPointMesh();
        initCloudGeom();
        cloudNode.attachChild(cloudGeom);
    }
    
    private void initPointMat(){
        pointMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        
        pointMat.getAdditionalRenderState().setPointSprite(true);//why is this crossed out?
        pointMat.getAdditionalRenderState().setBlendMode(BlendMode.Off);
        pointMat.setBoolean("PointSprite", true);
        //not sure what this does
        pointMat.setFloat("Quadratic", 0.25f);
    }
    
    private void initPointMesh() {
        pointMesh = new Mesh();
        pointMesh.setMode(Mode.Points);
        updatePointBuffer();
        updateColorBuffer();
        updateSizeBuffer();
        pointMesh.setStatic();
        pointMesh.updateBound();
    }
    
    private void initCloudGeom() {
        cloudGeom = new Geometry("Point Cloud", pointMesh);
        cloudGeom.setShadowMode(ShadowMode.Off);
        cloudGeom.setQueueBucket(Bucket.Opaque);
        cloudGeom.setMaterial(pointMat);
    }
    
    public void setColor(int index, ColorRGBA color) {
        this.colors[index] = color;
        doUpdateColors = true;
    }
    
    
    public void setSize(int index, float size) {
        this.sizes[index] = size;
        doUpdateSizes = true;
    }
    
    public Node getCloudNode(){return cloudNode;}
    public int numPoints(){return points.length;}
    protected Camera getCam(){return cam;}
    protected Vector3f[] getPoints(){return points;}
    protected ColorRGBA getColor(int id) {return colors[id];}
    protected float getSize(int id){return sizes[id];}
    protected Vector3f getPoint(int id){return points[id];}
    
    
    private void updatePointBuffer() {
        if(doUpdatePoints) {
            pointMesh.setBuffer(VertexBuffer.Type.Position, 3, MyBufferUtil.createPointsBuffer(points));
            doUpdatePoints = false;
        }
    }
    
    private void updateColorBuffer() {
        if(doUpdateColors) {
            pointMesh.setBuffer(VertexBuffer.Type.Color, 4, MyBufferUtil.createColorBuffer(colors));
            doUpdateColors = false;
        }
    }
    
    private void updateSizeBuffer() {
        if(doUpdateSizes) {
            pointMesh.setBuffer(VertexBuffer.Type.Size, 1, MyBufferUtil.createFloatBuffer(sizes));
            doUpdateSizes = false;
        }
    }
    

    @Override
    public void update(float timePerFrame) {
        updatePointBuffer();
        updateColorBuffer();
        updateSizeBuffer();
    }
    
}
