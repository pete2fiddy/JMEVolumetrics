
package mygame.input;

import com.jme3.input.InputManager;
import com.jme3.math.Vector2f;
import mygame.ui.SegmenterToolControllerInterface;



public class SegmenterToolControllerImpl implements SegmenterToolControllerInterface {
    private final KeyboardSegmenterToolController KEYBOARD_SEGMENTER_TOOL_CONTROLLER;
            
    public SegmenterToolControllerImpl(InputManager inputManager) {
        this.KEYBOARD_SEGMENTER_TOOL_CONTROLLER = new KeyboardSegmenterToolController(inputManager);
    }
    
    @Override
    public boolean selectActive() {
        return KEYBOARD_SEGMENTER_TOOL_CONTROLLER.getIfDiscreteAction("SELECT_TOGGLE");
    }

    @Override
    public boolean clearActive() {
        return KEYBOARD_SEGMENTER_TOOL_CONTROLLER.getIfDiscreteAction("CLEAR_TOGGLE");
    }

    @Override
    public boolean eraseActive() {
        return KEYBOARD_SEGMENTER_TOOL_CONTROLLER.getIfDiscreteAction("ERASE_TOGGLE");
    }

    @Override
    public Vector2f getSelectPos() {
        return KEYBOARD_SEGMENTER_TOOL_CONTROLLER.getSelectPos();
    }

    @Override
    public Vector2f getCursorPos() {
        return KEYBOARD_SEGMENTER_TOOL_CONTROLLER.getCursorPos();
    }

    @Override
    public float getSelectRadius() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
