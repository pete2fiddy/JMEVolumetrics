package defunct;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import mygame.control.ui.Updatable;

/**
 *
 * @author Owner
 */
public class KeyboardSegmenterToolController {
    private static final String[] DISCRETE_ACTION_NAMES = {"SELECT_TOGGLE", "CLEAR_TOGGLE", "ERASE_TOGGLE"};
    private static final Trigger[] DEFAULT_DISCRETE_ACTION_TRIGGERS = {new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
        new KeyTrigger(KeyInput.KEY_C),
        new KeyTrigger(KeyInput.KEY_E)};
    
    private InputManager inputManager;
    private HashMap<String, Boolean> discreteActionStates = new HashMap<String, Boolean>();
    private Vector2f lastSelectPos = new Vector2f();
    
    /*
    Should all rotating be down about the center of the screen as opposed to what I have now?
    */
    public KeyboardSegmenterToolController(InputManager inputManager) {
        this.inputManager = inputManager;
        addMappings();
    }
    
    private void addMappings() {
        for(int i = 0; i < DISCRETE_ACTION_NAMES.length; i++) {
            addMapping(DISCRETE_ACTION_NAMES[i], DEFAULT_DISCRETE_ACTION_TRIGGERS[i]);
        }
    }
    
    private void addMapping(String actionName, Trigger trigger) {
        inputManager.addMapping(actionName, trigger);
        discreteActionStates.put(actionName, false);
        inputManager.addListener(discreteActionListener, actionName);
    }
    
    private final ActionListener discreteActionListener = new ActionListener() {
        @Override
        public void onAction(String actionName, boolean isPressed, float tpf) {
            if(actionName.equals("ERASE_TOGGLE")) {
                if(isPressed) discreteActionStates.put(actionName, !discreteActionStates.get(actionName));
            } else {
                discreteActionStates.put(actionName, isPressed);
                if(actionName.equals("SELECT_TOGGLE") && isPressed) {
                    lastSelectPos = inputManager.getCursorPosition().clone();
                }
            }
        } 
    };
    
    public boolean getIfDiscreteAction(String actionName) {return discreteActionStates.get(actionName);}
    public Vector2f getSelectPos() {return lastSelectPos;}
    public Vector2f getCursorPos() {return inputManager.getCursorPosition();}
    
}
