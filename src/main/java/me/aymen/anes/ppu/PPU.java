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
    public final int[] vram;
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
    public final int[] oam;
    public final int[] palette;      // Palette Memory
    private Screen screen;
    private final ColorPalette colors;
    public int scanLine;
    public int cycle;
    private boolean complete;  // Indicates whole frame was drawn
    // Indicates the ppu entered vblank and wants to trigger a non-maskable
    // interrupt
    private boolean nmi;

    // TODO delete this
    public int[][] patternTable;


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

    public boolean isNmi() {
        return nmi;
    }

    public void setNmi(boolean nmi) {
        this.nmi = nmi;
    }

    public void clock() {
        if (scanLine == 0 && cycle == 1) {
            setNmi(false);
        }

        // Only write within visible boundary
        if (cycle < 256 && scanLine < 240)
            if (scanLine < 128 && cycle < 128) {
                int color = bus.ppuRead(0x3F00 + patternTable[cycle][scanLine]);
                screen.setPixel(cycle, scanLine, colors.get(color));
            }

        cycle++;

        // Set Vertical Blank Period when reaching scan line 240
        if (scanLine >= 240) {
            bus.ppuIO.setVerticalBlank(true);
            if (bus.ppuIO.nmiEnabled()) {
                setNmi(true);
            }
        }

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
        for (int y = 0; y < 8; y ++) {
            int lsByte = bus.ppuRead(0x1000 + offset + y);
            int msByte = bus.ppuRead(0x1000 + offset + y + 8);
            for(int x = 0; x < 8; x++) {
                int lsBit = (lsByte >> 7 - x) & 0x1;
                int msBit = (msByte >> 7 - x) & 0x1;
                int value = lsBit + msBit;
                tile[x][y] = value;
            }
        }

        return tile;
    }

    public int[][] getPalette(int index) {
        int[][] tile = new int[128][128];

        for (int yTile = 0; yTile < 16; yTile++) {
            for(int xTile = 0; xTile < 16; xTile ++) {

                int offset = (yTile * 256) + (xTile * 16);

                for (int row = 0; row < 8; row ++) {
                    int lsb = bus.ppuRead(index * 0x1000 + offset + row);
                    int msb = bus.ppuRead(index * 0x1000 + offset + row + 8);
                    for(int col = 0; col < 8; col++) {
                        int lsBit = (lsb >> 7 - col) & 0x1;
                        int msBit = (msb >> 7 - col) & 0x1;
                        int value = lsBit + msBit;
                        tile[xTile * 8 + col][yTile * 8 + row] = value;
                    }
                }
            }

        }

        return tile;
    }
}
