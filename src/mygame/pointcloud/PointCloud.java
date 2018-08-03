/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.pointcloud;

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
import mygame.ui.Updatable;
import mygame.util.MyBufferUtil;
import mygame.util.PointUtil;


public class PointCloud {
    private AssetManager assetManager;
    protected CloudPoint[] points;
    
    private Node cloudNode = new Node();
    private Material pointMat;
    private Mesh pointMesh;
    private Geometry cloudGeom;
    
    
    
    
    
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
    public PointCloud(AssetManager assetManager,CloudPoint[] points) {
        this.assetManager = assetManager;
        this.points = points;
        
        initPointMat();
        initPointMesh();
        initCloudGeom();
        cloudNode.attachChild(cloudNode);
    }
    
    
    public PointCloud(AssetManager assetManager, Vector3f[] points, ColorRGBA color, float size){
        this.points = new CloudPoint[points.length];
        for(int i = 0; i < this.points.length; i++){
            this.points[i] = new CloudPoint(points[i], color, size);
        }
        this.assetManager = assetManager;
        
        initPointMat();
        initPointMesh();
        initCloudGeom();
        cloudNode.attachChild(cloudGeom);
    }
    
    
    public CloudPoint getPoint(int index) {
        return points[index];
    }
    
    protected void bufferPoints() {
        pointMesh.setBuffer(VertexBuffer.Type.Position, 3, MyBufferUtil.createPointsBuffer(CloudPoint.extractPoints(points)));
    }
    
    protected void bufferColors() {
        pointMesh.setBuffer(VertexBuffer.Type.Color, 4, MyBufferUtil.createColorBuffer(CloudPoint.extractColors(points)));
    }
    
    protected void bufferSizes() {
        pointMesh.setBuffer(VertexBuffer.Type.Size, 1, MyBufferUtil.createFloatBuffer(CloudPoint.extractSizes(points)));
    }
    
    public Node getCloudNode(){return cloudNode;}
    public int numPoints(){return points.length;}
    
    
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
        bufferPoints();
        bufferColors();
        bufferSizes();
        pointMesh.setStatic();
        pointMesh.updateBound();
    }
    
    private void initCloudGeom() {
        cloudGeom = new Geometry("Point Cloud", pointMesh);
        cloudGeom.setShadowMode(ShadowMode.Off);
        cloudGeom.setQueueBucket(Bucket.Opaque);
        cloudGeom.setMaterial(pointMat);
    }
    
}
