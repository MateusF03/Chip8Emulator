package com.mateus.chip8emulator.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Display extends Canvas {

    private final GraphicsContext graphicsContext;
    private final int[][] values = new int[64][32];

    public Display() {
        super (640, 320);
        setFocusTraversable(true);
        graphicsContext = getGraphicsContext2D();
        clear();
        render();
    }

    public void clear() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                values[x][y] = 0;
            }
        }
    }

    public void render() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                Color fillColor = values[x][y] == 0 ? Color.BLACK : Color.WHITE;
                graphicsContext.setFill(fillColor);
                graphicsContext.fillRect(x*10,y*10,10,10);
            }
        }
    }

    public void setPixel(int x, int y) {
        values[x][y] ^= 1;
    }

    public int getPixelValue(int x, int y) {
        return values[x][y];
    }
}
