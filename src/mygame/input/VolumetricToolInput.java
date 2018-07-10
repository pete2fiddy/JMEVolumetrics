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
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import mygame.Updatable;

/**
 *
 * @author Owner
 */
public class VolumetricToolInput implements Updatable {
    private InputManager inputManager;
    private HashMap<String, Boolean> discreteActionStates = new HashMap<String, Boolean>();
    private Vector2f lastSelectPos = new Vector2f();
    
    /*
    Should all rotating be down about the center of the screen as opposed to what I have now?
    */
    public VolumetricToolInput(InputManager inputManager) {
        this.inputManager = inputManager;
        initBindings();
        initMouseState();
        addListeners();
        
    }
    
    private void initBindings(){
        inputManager.addMapping("SELECT_TOGGLE", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    }
    
    private void initMouseState(){
        discreteActionStates.put("SELECT_TOGGLE", false);
    }
    
    private void addListeners(){
        inputManager.addListener(discreteActionListener, "SELECT_TOGGLE");
    }
    
    private final ActionListener discreteActionListener = new ActionListener() {
        @Override
        public void onAction(String actionName, boolean isPressed, float tpf) {
            discreteActionStates.put(actionName, isPressed);
            if(actionName.equals("SELECT_TOGGLE")) {
                lastSelectPos = inputManager.getCursorPosition().clone();
            }
        } 
    };
    
    public boolean getIfDiscreteAction(String actionName) {return discreteActionStates.get(actionName);}
    public Vector2f getSelectPos() {return lastSelectPos;}
    
    @Override
    public void update(float timePerFrame) {
        if(discreteActionStates.get("SELECT_TOGGLE")) {
            lastSelectPos = inputManager.getCursorPosition().clone();
        }
    }
}
