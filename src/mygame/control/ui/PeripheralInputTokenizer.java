package mygame.control.ui;

import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.Trigger;
import java.util.HashMap;
import java.util.Map;
import mygame.control.ui.PeripheralInputTokenizer.ActionActiveState.ActivationType;


/*
Contains a class that allows simple tracking of peripheral input states using different activiation patterns (on press,
on release, etc.). Cannot handle all types of interactions that may be wanted, so use in cases where
all functionality the class requires is simple enough that PeripheralInputTokenizer can handle it. Otherwise,
program custom logic in a new class or within class.
*/
public class PeripheralInputTokenizer <ActionType extends Enum> {
    
    private InputManager inputManager;
    private Map<String, ActionActiveState> actionNameStateMap = new HashMap<String, ActionActiveState>();
    
    public PeripheralInputTokenizer(InputManager inputManager) {
        this.inputManager = inputManager;
    }
    
    public void addMapping(ActionType actionName, Trigger whenFired, ActivationType whenActive) {
        inputManager.addMapping(actionName.toString(), whenFired);
        actionNameStateMap.put(actionName.toString(), new ActionActiveState(actionName.toString(), whenActive));
        inputManager.addListener(actionStateListener, actionName.toString());
    }
    
    private final ActionListener actionStateListener = new ActionListener() {
        @Override
        public void onAction(String actionName, boolean isPressed, float tpf) {
            for(String key : actionNameStateMap.keySet()) {
                //cycles through all actions for the future, if I add more complex actions that rely on other actions
                actionNameStateMap.get(key).setIsActive(actionName, isPressed);
            }
        } 
    };

    public boolean actionActive(ActionType actionName) {
        return actionNameStateMap.get(actionName.toString()).isActive();
    }
    
    public static class ActionActiveState {
        
        public enum ActivationType {
            TOGGLE_ON_PRESS, ON_WHILE_PRESSED, ON_WHILE_RELEASED;
        }
        
        private ActivationType whenActivated;
        private boolean isActive = false;
        private String actionName;
        
        private ActionActiveState(String actionName, ActivationType whenActivated) {
            this.actionName = actionName;
            this.whenActivated = whenActivated;
        }
        
        protected boolean isActive() {return isActive;}
        
        protected void setIsActive(String actionName, boolean isPressed) {
            if(actionName.equals(this.actionName)) {
                switch(whenActivated) {
                    case TOGGLE_ON_PRESS:
                        if(isPressed) isActive = !isActive;
                        break;
                    case ON_WHILE_PRESSED:
                        isActive = isPressed;
                        break;
                    case ON_WHILE_RELEASED:
                        isActive = !isPressed;
                        break;
                    default:
                        throw new UnsupportedOperationException("setIsActive not implemented for input trigger type: " + this);
                }
            }
        } 
    }
    
                
    
}
