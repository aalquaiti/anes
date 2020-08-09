package me.aymen.anes.ppu;

import me.aymen.anes.io.Bus;

/**
 * Represents NES Picture Processing Unit
 */
public class PPU {
    public final static int OAM_SIZE = 0xFF;
    public final static int PALETTE_SIZE = 0xFF;

    // PPU Componenets

    // Object Attribute Memory
    // Controlled by cpu through memory mappers:
    // OAMADDR $2003
    // OAMDATA $2004
    // OAMDMA $4014
    // Can be seen as an array of 64 entries.
    // TODO Implement that as described at
    //  https://wiki.nesdev.com/w/index.php/PPU_programmer_reference#Hardware_mapping
    public int oam[];
    public int palette[];      // Palette Memory
    private Bus bus;
    private Screen screen;
    private ColorPalette colors;
    public int scanLine;
    public int cycle;
    private boolean complete;  // Indicates whole frame was drawn

    public PPU(Bus bus) {
        this.bus = bus;
        oam = new int[OAM_SIZE];
        palette = new int[PALETTE_SIZE];
        colors = new ColorPalette();
    }

    /**
     * Connects ppu to a screen
     * @param screen
     */
    public void connect(Screen screen) {
        this.screen = screen;
    }

    public void clock() {
        // Only write within visible boundary
        if (cycle < 256 && scanLine < 240)
            screen.setPixel(cycle, scanLine, colors.get(
                    (int)(Math.random()*0x40)));

        cycle++;

        if (cycle == 341) {
            cycle = 0;
            scanLine++;
        }

        if (scanLine == 262) {
            scanLine = 0;
            complete = true;
        }
    }

    public boolean isComplete() {
        return complete;
    }
}
