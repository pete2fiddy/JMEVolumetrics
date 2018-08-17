/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.control;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.control.ui.Updatable;
import mygame.util.JblasJMEConverter;
import org.jblas.DoubleMatrix;



public class NavigationController implements Updatable {
    private final Map<String, Trigger> analogActionTriggerMap = new HashMap<String, Trigger>();
    private final Map<String, Trigger> discreteActionTriggerMap = new HashMap<String, Trigger>();
    
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
    public NavigationController(InputManager inputManager) {
        this.cameraNode = new VolumetricsCameraNode();
        this.inputManager = inputManager;
        initMouseState();
        addMappings();
    }
    
    private void addMappings() {
        analogActionTriggerMap.put("FORWARD", new KeyTrigger(KeyInput.KEY_W));
        analogActionTriggerMap.put("BACKWARD",  new KeyTrigger(KeyInput.KEY_S));
        analogActionTriggerMap.put("LEFT", new KeyTrigger(KeyInput.KEY_D));
        analogActionTriggerMap.put("RIGHT", new KeyTrigger(KeyInput.KEY_A));
        analogActionTriggerMap.put("UP", new KeyTrigger(KeyInput.KEY_SPACE));
        analogActionTriggerMap.put("DOWN", new KeyTrigger(KeyInput.KEY_LSHIFT));
        analogActionTriggerMap.put("ZOOM_IN", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        analogActionTriggerMap.put("ZOOM_OUT", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        
        discreteActionTriggerMap.put("ROTATE_TOGGLE", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        discreteActionTriggerMap.put("DRAG_TOGGLE", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        for(String discreteActionName : discreteActionTriggerMap.keySet()) {
            addDiscreteMapping(discreteActionName, discreteActionTriggerMap.get(discreteActionName));
        }
        for(String analogActionName : analogActionTriggerMap.keySet()) {
            addAnalogMapping(analogActionName, analogActionTriggerMap.get(analogActionName));
        }
    }
    
    private void addDiscreteMapping(String actionName, Trigger trigger) {
        inputManager.addMapping(actionName, trigger);
        discreteActionStates.put(actionName, false);
        inputManager.addListener(discreteActionListener, actionName);
        
    }
    
    private void addAnalogMapping(String actionName, Trigger trigger) {
        inputManager.addMapping(actionName, trigger);
        inputManager.addListener(continuousActionListener, actionName);
    }
    
    private void initMouseState(){
        discreteActionStates.put("ROTATE_TOGGLE", false);
        discreteActionStates.put("DRAG_TOGGLE", false);
    }
    
    private final AnalogListener continuousActionListener = new AnalogListener() {
        @Override
        public void onAnalog(String actionName, float value, float tpf) {
            switch(actionName) {
                case "FORWARD": 
                    cameraNode.move(0f,0f,MOVE_VELOCITY*tpf);
                    break;
                case "BACKWARD":
                    cameraNode.move(0,0,-MOVE_VELOCITY*tpf);
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
    
    public void setParent(Node attachee) {
        attachee.attachChild(cameraNode);
    }
    
    public void unsetParent(Node attachee) {
        attachee.detachChild(cameraNode);
    }

    @Override
    public void update(float timePerFrame) {
        Vector2f newMousePos = inputManager.getCursorPosition().clone();
        if(discreteActionStates.get("ROTATE_TOGGLE")) {
            Vector2f mouseDelta = newMousePos.subtract(mousePos);
            Vector2f rotateToggleDelta = newMousePos.subtract(lastRotateOrDragTogglePos);
            
            //comment out below to turn off free rotation
            cameraNode.spinLeftRight(mouseDelta.x*ROTATE_VELOCITY_PER_PIXEL);
            float newDownUpSpin = cameraNode.getDownUpSpin()-mouseDelta.y*ROTATE_VELOCITY_PER_PIXEL;
            if(newDownUpSpin < 0) newDownUpSpin = 0f;
            if(newDownUpSpin > Math.PI/2.0) newDownUpSpin = (float)(Math.PI/2.0);
            cameraNode.setDownUpSpin(newDownUpSpin);
            
            //comment out below to turn off choosing only one rotation direction at a time 
           /*
            if(Math.abs(rotateToggleDelta.x) > Math.abs(rotateToggleDelta.y)){
                cameraNode.spinLeftRight(mouseDelta.x*ROTATE_VELOCITY_PER_PIXEL);
            } else {
                float newDownUpSpin = cameraNode.getDownUpSpin()-mouseDelta.y*ROTATE_VELOCITY_PER_PIXEL;
                if(newDownUpSpin < 0) newDownUpSpin = 0f;
                if(newDownUpSpin > Math.PI/2.0) newDownUpSpin = (float)(Math.PI/2.0);
                cameraNode.setDownUpSpin(newDownUpSpin);
            }
            */
        }
        if(discreteActionStates.get("DRAG_TOGGLE")) {
            Vector2f mouseDelta = newMousePos.subtract(mousePos);
            /*
            need to scale both so that it "drags" naturally along with the cursor such that the cursor stays at the same point in the cloud while dragging
            (has something to do with multiplying by a factor determined by altitude, and maybe something to do with perspective shift?)
            */
            cameraNode.move(DRAG_VELOCITY_PER_PIXEL*mouseDelta.x*cameraNode.getLocalScale().x, 0f, 0f);
            cameraNode.move(0f,0f,-DRAG_VELOCITY_PER_PIXEL*mouseDelta.y*cameraNode.getLocalScale().x);
        }
        mousePos = newMousePos;
        
    }
    
    public Map<String, Trigger> getAnalogBindings(){return analogActionTriggerMap;}
    
    public Map<String, Trigger> getDiscreteBindings(){return discreteActionTriggerMap;}
    
    private class VolumetricsCameraNode extends Node {
        private Node leftRightRotNode = new Node();
        private Node moveNode = new Node();
        
        public VolumetricsCameraNode() {
            super();
            super.attachChild(leftRightRotNode);
            leftRightRotNode.attachChild(moveNode);
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
        
        
        @Override
        public Spatial move(float x, float y, float z) {
            DoubleMatrix rotProjMat = JblasJMEConverter.toDoubleMatrix(leftRightRotNode.getLocalRotation().toRotationMatrix()).transpose();
            DoubleMatrix p = new DoubleMatrix(new double[][]{{x, y, z}});
            DoubleMatrix moveProjs = rotProjMat.mmul(p.transpose());
            return moveNode.move((float)moveProjs.get(0),
                    (float)moveProjs.get(1),
                    (float)moveProjs.get(2));
        }
        
        @Override
        public int attachChild(Spatial s) {
            return moveNode.attachChild(s);
        }
        
        @Override
        public int detachChild(Spatial s) {
            return moveNode.detachChild(s);
        }
    }
}
