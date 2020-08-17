package me.aymen.anes.io;

/**
 * Represents VRAM accessed by bus
 */
public class VRAM {
    public final static int SIZE = 0x800;
    public final int[] memory;

    public VRAM() {
        memory = new int[SIZE];
    }
}
