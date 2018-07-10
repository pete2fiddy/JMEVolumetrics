/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.Updatable;



public class VolumetricsCamera implements Updatable {
    private static final float MOVE_VELOCITY = 6f;
    private static final float ROTATE_VELOCITY_PER_PIXEL = 0.005f;
    private static final float SCALE_PER_NOTCH = 0.1f;
    private static final float DRAG_VELOCITY_PER_PIXEL = 0.02f;
    private VolumetricsCameraNode cameraNode;
    private InputManager inputManager;
    private Vector2f mousePos;
    private Vector2f lastRotateOrDragTogglePos;
    private HashMap<String, Boolean> discreteActionStates = new HashMap<String, Boolean>();
    
    /*
    Should all rotating be down about the center of the screen as opposed to what I have now?
    */
    public VolumetricsCamera(InputManager inputManager) {
        this.cameraNode = new VolumetricsCameraNode();
        //this.cameraNode.setLocalTranslation(0f,0f,0f);
        this.inputManager = inputManager;
        initBindings();
        initMouseState();
        addListeners();
        
    }
    
    private void initBindings(){
        inputManager.addMapping("FORWARD", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("BACKWARD", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("LEFT", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("RIGHT", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("UP", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("DOWN", new KeyTrigger(KeyInput.KEY_LSHIFT));
        
        inputManager.addMapping("ZOOM_IN", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("ZOOM_OUT", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        
        
        inputManager.addMapping("ROTATE_TOGGLE", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("DRAG_TOGGLE", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        
    }
    
    private void initMouseState(){
        discreteActionStates.put("ROTATE_TOGGLE", false);
        discreteActionStates.put("DRAG_TOGGLE", false);
    }
    
    private void addListeners(){
        inputManager.addListener(continuousActionListener, "FORWARD", "BACKWARD", "LEFT", "RIGHT", "UP", "DOWN",
                "ZOOM_IN", "ZOOM_OUT");
        
        inputManager.addListener(discreteActionListener, "ROTATE_TOGGLE", "DRAG_TOGGLE");
    }
    
    
    private final AnalogListener continuousActionListener = new AnalogListener() {
        @Override
        public void onAnalog(String actionName, float value, float tpf) {
            switch(actionName) {
                case "FORWARD": 
                    cameraNode.slideBackwardForward(MOVE_VELOCITY*tpf);
                    //cameraNode.move(0f,0f,MOVE_VELOCITY*tpf);
                    break;
                case "BACKWARD":
                    cameraNode.slideBackwardForward(-MOVE_VELOCITY*tpf);
                    //cameraNode.move(0f,0f,-MOVE_VELOCITY*tpf);
                    break;
                case "LEFT":
                    cameraNode.move(-MOVE_VELOCITY*tpf,0f,0f);
                    break;
                case "RIGHT":
                    cameraNode.move(MOVE_VELOCITY*tpf,0f,0f);
                    break;
                case "UP":
                    cameraNode.move(0f,-MOVE_VELOCITY*tpf,0f);
                    break;
                case "DOWN": 
                    cameraNode.move(0f,MOVE_VELOCITY*tpf,0f);
                    break;
                case "ZOOM_IN":
                    cameraNode.scale(1f+SCALE_PER_NOTCH);
                    break;
                case "ZOOM_OUT":
                    cameraNode.scale(1f-SCALE_PER_NOTCH);
                    break;
            }
        }
    };
    
    private final ActionListener discreteActionListener = new ActionListener() {
        @Override
        public void onAction(String actionName, boolean isPressed, float tpf) {
            discreteActionStates.put(actionName, isPressed);
            if(actionName.equals("ROTATE_TOGGLE") || actionName.equals("DRAG_TOGGLE")) {
                lastRotateOrDragTogglePos = inputManager.getCursorPosition().clone();
            }
        } 
    };
    
    
    public void attachChildren(Spatial... spatials) {
        for(Spatial s : spatials) {
            cameraNode.attachChild(s);
        }
    }
    
    public void detachChildren(Spatial... spatials) {
        for(Spatial s : spatials) {
            cameraNode.detachChild(s);
        }
    }
    
    public void attachCamera(Node attachee) {
        attachee.attachChild(cameraNode);
    }
    
    public void detachCamera(Node attachee) {
        attachee.detachChild(cameraNode);
    }

    @Override
    public void update(float timePerFrame) {
        Vector2f newMousePos = inputManager.getCursorPosition().clone();
        if(discreteActionStates.get("ROTATE_TOGGLE")) {
            Vector2f mouseDelta = newMousePos.subtract(mousePos);
            Vector2f rotateToggleDelta = newMousePos.subtract(lastRotateOrDragTogglePos);
            if(Math.abs(rotateToggleDelta.x) > Math.abs(rotateToggleDelta.y)){
                cameraNode.spinLeftRight(mouseDelta.x*ROTATE_VELOCITY_PER_PIXEL);
            } else {
                float newDownUpSpin = cameraNode.getDownUpSpin()-mouseDelta.y*ROTATE_VELOCITY_PER_PIXEL;
                if(newDownUpSpin < 0) newDownUpSpin = 0f;
                if(newDownUpSpin > Math.PI/2.0) newDownUpSpin = (float)(Math.PI/2.0);
                cameraNode.setDownUpSpin(newDownUpSpin);
            }
        }
        if(discreteActionStates.get("DRAG_TOGGLE")) {
            Vector2f mouseDelta = newMousePos.subtract(mousePos);
            /*
            need to scale both so that it "drags" naturally along with the cursor such that the cursor stays at the same point in the cloud while dragging
            (has something to do with multiplying by a factor determined by altitude, and maybe something to do with perspective shift?)
            */
            cameraNode.move(DRAG_VELOCITY_PER_PIXEL*mouseDelta.x*cameraNode.getLocalScale().x, 0f, 0f);
            cameraNode.slideBackwardForward(-DRAG_VELOCITY_PER_PIXEL*mouseDelta.y*cameraNode.getLocalScale().x);
        }
        mousePos = newMousePos;
        
    }
    
    private class VolumetricsCameraNode extends Node {
        private Node leftRightRotNode = new Node();
        
        public VolumetricsCameraNode() {
            super();
            super.attachChild(leftRightRotNode);
        }
        
        protected void spinLeftRight(float angle) {
            leftRightRotNode.rotate(0f, angle, 0f);
        }
        
        protected void spinDownUp(float angle) {
            this.rotate(angle, 0f, 0f);
        }
        
        protected void setDownUpSpin(float angle) {
            float rotation = this.getDownUpSpin();
            this.rotate(angle - rotation, 0f, 0f);
        }
        
        public float getDownUpSpin() {
            return this.getLocalRotation().toAngles(new float[3])[0];
        }
        
        //slides this camera node such that its height remains constant (like dragging a piece of paper on the table)
        public void slideBackwardForward(float amount) {
            double angle = -getDownUpSpin();
            this.move(0f, (float)(Math.sin(angle)*amount), (float)(Math.cos(angle)*amount));
        }
        
        @Override
        public int attachChild(Spatial s) {
            return leftRightRotNode.attachChild(s);
        }
        
        @Override
        public int detachChild(Spatial s) {
            return leftRightRotNode.detachChild(s);
        }
    }
}
