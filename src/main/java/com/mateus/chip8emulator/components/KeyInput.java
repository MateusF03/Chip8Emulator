package com.mateus.chip8emulator.components;

import javafx.scene.input.KeyCode;

public class KeyInput {

    private final boolean[] keys = new boolean[16];

    public void setKeyValue(KeyCode keyCode, boolean value) {
        switch (keyCode) {
            case DIGIT1:
                keys[0] = value;
                break;
            case DIGIT2:
                keys[1] = value;
                break;
            case DIGIT3:
                keys[2] = value;
                break;
            case DIGIT4:
                keys[3] = value;
                break;
            case Q:
                keys[4] = value;
                break;
            case W:
                keys[5] = value;
                break;
            case E:
                keys[6] = value;
                break;
            case R:
                keys[7] = value;
                break;
            case A:
                keys[8] = value;
                break;
            case S:
                keys[9] = value;
                break;
            case D:
                keys[10] = value;
                break;
            case F:
                keys[11] = value;
                break;
            case Z:
                keys[12] = value;
                break;
            case X:
                keys[13] = value;
                break;
            case C:
                keys[14] = value;
                break;
            case V:
                keys[15] = value;
                break;
        }
    }

    public boolean getKey(int idx) {
        return keys[idx];
    }

    public boolean[] getKeys() {
        return keys;
    }
}
