/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.view.pointcloud;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;

/**
 *
 * @author Owner
 */
public class LineCloud {
    
    private Node cloudNode = new Node();
    
    public LineCloud(AssetManager assetManager, Vector3f[] basePoints, Vector3f[] directions, float lineMagMultiplier) {
        for(int i = 0; i < basePoints.length; i++) {
            Vector3f start = basePoints[i];
            Vector3f end = start.add(directions[i].mult(lineMagMultiplier));
            Line l = new Line(start, end);
            Geometry g = new Geometry("Line " + Integer.toString(i), l);
            g.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
            cloudNode.attachChild(g);
        }
    }
    
    public LineCloud(AssetManager assetManager, Vector3f[] basePoints, Vector3f[] endPoints) {
        for(int i = 0; i < basePoints.length; i++) {
            Line l = new Line(basePoints[i], endPoints[i]);
            Geometry g = new Geometry("Line " + Integer.toString(i), l);
            g.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
            cloudNode.attachChild(g);
        }
    }
    
    public Node getCloudNode() {return cloudNode;}
    
    public void attachTo(Node n) {n.attachChild(cloudNode);}
}
