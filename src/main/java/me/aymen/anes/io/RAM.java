package me.aymen.anes.io;

/**
 * Represents RAM accessed by bus
 */
public class RAM {
    public static int SIZE = 0x800;
    public final int[] memory;

    public RAM() {
        memory = new int[SIZE];
    }
}
