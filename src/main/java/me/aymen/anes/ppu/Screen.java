package me.aymen.anes.ppu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

/**
 * Represents Screen that output NES graphics received from PPU
 */
public class Screen extends JPanel {

    /**
     * NES width resolution
     */
    public final int WIDTH = 256;

    /**
     * NES height resolution
     */
    public final int HEIGHT = 240;

    Color[][] pixels;
    int widthScale;
    int heightScale;
    JFrame gui;

    /**
     *
     * @param screenWidth The displayed Window Width
     * @param screenHeight The displayed Window Height
     */
    public Screen(int screenWidth, int screenHeight) {
        this.widthScale = screenWidth / WIDTH;
        this.heightScale = screenHeight / HEIGHT;

        pixels = new Color[WIDTH][HEIGHT];

        gui = new JFrame("ANES");
        gui.setSize(screenWidth, screenHeight);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.add(this);

        init();
        gui.setVisible(true);
    }

    private void init() {
        Color black = Color.BLACK;
        for(int x =0; x < WIDTH; x++) {
            for(int y = 0; y < HEIGHT; y++) {
                pixels[x][y] = black;
            }
        }
    }

    /**
     * Reset screen and set pixels to black
     */
    public void reset() {
        init();
        repaint();
    }

    /**
     * Set a specifc color for a pixel
     * @param x
     * @param y
     * @param color
     */
    public void setPixel(int x, int y, Color color) {
        pixels[x][y] = color;
    }

    @Override
    public void paintComponent(Graphics g) {
        for(int x =0; x < WIDTH; x++) {
            for(int y = 0; y < HEIGHT; y++) {
                g.setColor(pixels[x][y]);
                g.fillRect(x * widthScale, y * heightScale, widthScale, heightScale);
            }
        }
    }

    public void setKeyListener(KeyListener listener) {
        gui.addKeyListener(listener);
    }
}
