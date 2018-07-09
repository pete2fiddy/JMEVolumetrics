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
import mygame.util.MyBufferUtils;
import mygame.util.PointUtils;

/**
 *
 * @author Owner
 */
public class PointCloud implements Updatable {
    private AssetManager assetManager;
    private Vector3f[] points;
    private ColorRGBA[] colors;
    private float[] sizes;
    private KMeansSearchTree pointSearchTree;
    
    private Node cloudNode = new Node();
    private Material pointMat;
    private Mesh pointMesh;
    private Geometry cloudGeom;
    
    private boolean doUpdateColors = true, doUpdatePoints = true, doUpdateSizes = true;
        
    /*
    Code mostly copy-pasted from Ogli's source here: 
    https://hub.jmonkeyengine.org/t/how-to-create-a-mesh-from-cloud-points/17290/9
    See thread for hints about how to further optimize, but currently it can pretty easily handle 
    ~3 million points
    */
    public PointCloud(AssetManager assetManager, Vector3f[] points, ColorRGBA[] colors, float[] sizes) {
        this.assetManager = assetManager;
        this.points = points;
        this.colors = colors;
        this.sizes = sizes;
        this.pointSearchTree = new KMeansSearchTree(this.points);
        this.pointSearchTree.fit(5, 20, 100);
        
        initPointMat();
        initPointMesh();
        initCloudGeom();
        cloudNode.attachChild(cloudGeom);
    }
    
    public static PointCloud initWithFixedColorAndSize(AssetManager assetManager, Vector3f[] points, 
            ColorRGBA color, float size){
        float[] sizes = new float[points.length];
        ColorRGBA[] colors = new ColorRGBA[points.length];
        for(int i = 0; i < sizes.length; i++){
            sizes[i] = size;
            colors[i] = color;
        }
        return new PointCloud(assetManager, points, colors, sizes);
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
    
    public void setPoint(int index, Vector3f point) {
        this.points[index] = point;
        doUpdatePoints = true;
    }
    
    public void setSize(int index, float size) {
        this.sizes[index] = size;
        doUpdateSizes = true;
    }
    
    public Node getCloudNode(){return cloudNode;}
    public int numPoints(){return points.length;}
    
    
    public int getNearestScreenNeighborId(Vector2f point, Camera cam) {
        return pointSearchTree.getNearestScreenNeighborId(point, 
                cloudNode.getWorldTransform().toTransformMatrix(), cam);
        //return PointUtils.getNearestScreenNeighborId(points, point, 
        //    cloudNode.getWorldTransform().toTransformMatrix(), cam);
    }
    
    
    private void updatePointBuffer() {
        if(doUpdatePoints) {
            pointMesh.setBuffer(VertexBuffer.Type.Position, 3, MyBufferUtils.createPointsBuffer(points));
            doUpdatePoints = false;
        }
    }
    
    private void updateColorBuffer() {
        if(doUpdateColors) {
            pointMesh.setBuffer(VertexBuffer.Type.Color, 4, MyBufferUtils.createColorBuffer(colors));
            doUpdateColors = false;
        }
    }
    
    private void updateSizeBuffer() {
        if(doUpdateSizes) {
            pointMesh.setBuffer(VertexBuffer.Type.Size, 1, MyBufferUtils.createFloatBuffer(sizes));
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
