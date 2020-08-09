package me.aymen.anes.io;

/**
 * Represents Video Memory used by PPU
 */
public class VRAM {
    public final static int SIZE = 0x800;
    protected final int memory[];

    public VRAM() {
        memory = new int[SIZE];
    }
}
