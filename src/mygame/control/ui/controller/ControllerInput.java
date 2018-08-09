package mygame.control.ui.controller;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.Vector2f;
import mygame.control.ui.PeripheralInputTokenizer;
import mygame.control.ui.Updatable;

public class ControllerInput <GraphType extends Enum, SegmenterType extends Enum> extends ControllerToolboxFrame<GraphType, SegmenterType> implements Updatable {
    
    private static final Trigger DEFAULT_SELECT_TRIGGER = new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
            DEFAULT_CLEAR_TRIGGER = new KeyTrigger(KeyInput.KEY_C),
            DEFAULT_ERASE_TRIGGER = new KeyTrigger(KeyInput.KEY_E);
    private static final PeripheralInputTokenizer.ActionActiveState.ActivationType DEFAULT_SELECT_ACTIVE = PeripheralInputTokenizer.ActionActiveState.ActivationType.ON_WHILE_PRESSED, 
            DEFAULT_CLEAR_ACTIVE = PeripheralInputTokenizer.ActionActiveState.ActivationType.ON_WHILE_PRESSED,
            DEFAULT_ERASE_ACTIVE = PeripheralInputTokenizer.ActionActiveState.ActivationType.TOGGLE_ON_PRESS;
    private PeripheralInputTokenizer<ActionType> inputTokenizer;
    private Vector2f selectPos;
    private InputManager inputManager;
    
    public ControllerInput(InputManager inputManager, Class<GraphType> graphTypeClass, Class<SegmenterType> segmenterTypeClass) {
        super("Toolbox", graphTypeClass, segmenterTypeClass);
        this.inputManager = inputManager;
        initInputTokenizer();
    }
    
    private void initInputTokenizer() {
        this.inputTokenizer = new PeripheralInputTokenizer(inputManager);
        inputTokenizer.addMapping(ActionType.SELECT_ACTION, DEFAULT_SELECT_TRIGGER, DEFAULT_SELECT_ACTIVE);
        inputTokenizer.addMapping(ActionType.CLEAR_ACTION, DEFAULT_CLEAR_TRIGGER, DEFAULT_CLEAR_ACTIVE);
        inputTokenizer.addMapping(ActionType.ERASE_ACTION, DEFAULT_ERASE_TRIGGER, DEFAULT_ERASE_ACTIVE);
    }
    
    public Vector2f getSelectPos() {return selectPos;}

    public boolean actionActive(ActionType actionType) {
        return inputTokenizer.actionActive(actionType);
    }
    
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
