package mygame.control.ui.controller;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.Vector2f;
import java.util.HashMap;
import java.util.Map;
import mygame.control.ui.PeripheralInputTokenizer;
import mygame.control.ui.Updatable;

public class UIController <GraphType extends Enum, SegmenterType extends Enum> extends ControllerToolboxFrame<GraphType, SegmenterType> implements Updatable {
    private static final Trigger[] DEFAULT_TRIGGERS = {new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
        new KeyTrigger(KeyInput.KEY_C),
        new KeyTrigger(KeyInput.KEY_E)};
    private static final PeripheralInputTokenizer.ActionActiveState.ActivationType[] DEFAULT_TRIGGER_ACTIVATION_TYPES = {PeripheralInputTokenizer.ActionActiveState.ActivationType.ON_WHILE_PRESSED, 
            PeripheralInputTokenizer.ActionActiveState.ActivationType.ON_WHILE_PRESSED,
            PeripheralInputTokenizer.ActionActiveState.ActivationType.TOGGLE_ON_PRESS};
    
    /*
    private static final Trigger DEFAULT_SELECT_TRIGGER = new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                DEFAULT_CLEAR_TRIGGER = new KeyTrigger(KeyInput.KEY_C),
                DEFAULT_ERASE_TRIGGER = new KeyTrigger(KeyInput.KEY_E);
    
    private static final PeripheralInputTokenizer.ActionActiveState.ActivationType DEFAULT_SELECT_ACTIVE = PeripheralInputTokenizer.ActionActiveState.ActivationType.ON_WHILE_PRESSED, 
            DEFAULT_CLEAR_ACTIVE = PeripheralInputTokenizer.ActionActiveState.ActivationType.ON_WHILE_PRESSED,
            DEFAULT_ERASE_ACTIVE = PeripheralInputTokenizer.ActionActiveState.ActivationType.TOGGLE_ON_PRESS;
    */
    private Map<String, Trigger> bindings = new HashMap<String, Trigger>();
    
    private PeripheralInputTokenizer<ActionType> inputTokenizer;
    private Vector2f selectPos;
    private InputManager inputManager;
    
    public UIController(Controller controller, InputManager inputManager, Class<GraphType> graphTypeClass, Class<SegmenterType> segmenterTypeClass) {
        super(controller, "Toolbox", graphTypeClass, segmenterTypeClass);
        this.inputManager = inputManager;
        initInputTokenizer();
    }
    
    private void initInputTokenizer() {
        this.inputTokenizer = new PeripheralInputTokenizer(inputManager);
        int i = 0;
        for(ActionType actionType : ActionType.values()) {
            inputTokenizer.addMapping(actionType, DEFAULT_TRIGGERS[i], DEFAULT_TRIGGER_ACTIVATION_TYPES[i]);
            bindings.put(actionType.toString(), DEFAULT_TRIGGERS[i]);
            i++;
        }
    }
    
    public Vector2f getSelectPos() {return selectPos;}

    public boolean actionActive(ActionType actionType) {
        return inputTokenizer.actionActive(actionType);
    }
    
    public Map<String, Trigger> getBindings() {return bindings;}
    
    @Override
    public void update(float timePerFrame) {
        if(inputTokenizer.actionActive(ActionType.SELECT_ACTION)) {
            selectPos = inputManager.getCursorPosition();
        }
    }
    
    public static enum ActionType {
        SELECT_ACTION("SELECT"), CLEAR_ACTION("CLEAR"), ERASE_ACTION("ERASE");
        
        private String tokenString;
        private ActionType(String tokenString) {
            this.tokenString = tokenString;
        }
        
        @Override
        public String toString() {
            return tokenString;
        }
    }
}
