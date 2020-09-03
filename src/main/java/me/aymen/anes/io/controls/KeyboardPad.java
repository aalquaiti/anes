package me.aymen.anes.io.controls;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.Keyboard;

/**
 * Uses keyboard to model a game pad
 */
public class KeyboardPad implements GamePad{
    private final Controller controller;
    private net.java.games.input.Keyboard keyboard;

    // Value for buttons
    private Identifier.Key buttonA;
    private Identifier.Key buttonB;
    private Identifier.Key buttonSelect;
    private Identifier.Key buttonStart;
    private Identifier.Key buttonUp;
    private Identifier.Key buttonDown;
    private Identifier.Key buttonLeft;
    private Identifier.Key buttonRight;

    // Temporary value hold while a key is pressed and is lost when
    // key released, representing the value held by an inner latch
    private int tempVal;
    // Stored value when game pad is polled
    private int buffer;

    public KeyboardPad(Controller controller) {
        this.controller = controller;

        // Default mapping to gamepad 1
        buttonA = Identifier.Key.Z;
        buttonB = Identifier.Key.X;
        buttonSelect = Identifier.Key.BACK;
        buttonStart = Identifier.Key.RETURN;
        buttonUp = Identifier.Key.UP;
        buttonDown = Identifier.Key.DOWN;
        buttonLeft = Identifier.Key.LEFT;
        buttonRight = Identifier.Key.RIGHT;

        // Initialzie JInput by finding keyboard component
        net.java.games.input.Controller[] conts =
                ControllerEnvironment.getDefaultEnvironment().getControllers();
        for(net.java.games.input.Controller c : conts) {
            if (c.getType() == net.java.games.input.Controller.Type.KEYBOARD) {
                keyboard = (Keyboard)c;
            }
        }
        // TODO change to more appropriate exception
        if(keyboard == null) {
            System.out.println("Could not find keyboard. Exiting...");
            System.exit(-1);
        }
    }

    public void setButtonA(Identifier.Key buttonA) {
        this.buttonA = buttonA;
    }

    public void setButtonB(Identifier.Key buttonB) {
        this.buttonB = buttonB;
    }

    public void setButtonSelect(Identifier.Key buttonSelect) {
        this.buttonSelect = buttonSelect;
    }

    public void setButtonStart(Identifier.Key buttonStart) {
        this.buttonStart = buttonStart;
    }

    public void setButtonUp(Identifier.Key buttonUp) {
        this.buttonUp = buttonUp;
    }

    public void setButtonDown(Identifier.Key buttonDown) {
        this.buttonDown = buttonDown;
    }

    public void setButtonLeft(Identifier.Key buttonLeft) {
        this.buttonLeft = buttonLeft;
    }

    public void setButtonRight(Identifier.Key buttonRight) {
        this.buttonRight = buttonRight;
    }

    @Override
    public int read() {
        int result = buffer & 0x1;
        buffer>>=1;

        return result;
    }

    @Override
    public void poll() {
        buffer = tempVal;
    }

    @Override
    public void update() {
        keyboard.poll();
        tempVal = 0;
        tempVal |= keyboard.isKeyDown(buttonA) ? A : 0;
        tempVal |= keyboard.isKeyDown(buttonB) ? B : 0;
        tempVal |= keyboard.isKeyDown(buttonSelect) ? SELECT : 0;
        tempVal |= keyboard.isKeyDown(buttonStart) ? START : 0;
        tempVal |= keyboard.isKeyDown(buttonUp) ? UP : 0;
        tempVal |= keyboard.isKeyDown(buttonDown) ? DOWN : 0;
        tempVal |= keyboard.isKeyDown(buttonLeft) ? LEFT : 0;
        tempVal |= keyboard.isKeyDown(buttonRight) ? RIGHT : 0;
    }
}
