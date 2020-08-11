package me.aymen.anes.ppu;

import me.aymen.anes.io.Bus;

/**
 * Represents NES Picture Processing Unit
 */
public class PPU {
    public final static int VRAM_SIZE = 0x800;
    public final static int OAM_SIZE = 0xFF;
    public final static int PALETTE_SIZE = 0xFF;

    // PPU Components
    private Bus bus;
    public final int vram[];
    // Object Attribute Memory
    // Controlled by cpu through memory mappers:
    // OAMADDR $2003
    // OAMDATA $2004
    // OAMDMA $4014
    // Can be seen as an array of 64 entries.
    // TODO Implement that as described at
    //  https://wiki.nesdev.com/w/index.php/PPU_programmer_reference#Hardware_mapping
    // TODO consider having oam and palette memory as separated from PPU
    //   Although they are internal to PPU, they are accessed through bus which
    //   makes the architecture cleaner and easier to follow
    public final int oam[];
    public final int palette[];      // Palette Memory
    private Screen screen;
    private final ColorPalette colors;
    public int scanLine;
    public int cycle;
    private boolean complete;  // Indicates whole frame was drawn

    public PPU(Bus bus) {
        this.bus = bus;
        vram = new int[VRAM_SIZE];
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

    public int[][] getTile(int index) {
        int[][] tile = new int[8][8];
        // Each tile is 16 bytes in size
        int offset = index * 16;

        // TODO complete
        for (int i = 0; i < 8; i ++) {
            int lsByte = bus.ppuRead(0x1000 + offset + i);
            int msByte = bus.ppuRead(0x1000 + offset + i + 8);
            for(int j = 0; j < 8; j++) {
                int lsBit = (lsByte >> 8 - i) & 0x1;
                int msBit = (msByte >> 8 - i) & 0x1;
                int value = lsBit + (msBit << 1);
                tile[i][j] = value;
            }
        }

        return tile;
    }
}
