package me.aymen.anes.io;

/**
 * Represents PPU IO Registers that are accessible through bus at 0x2000 - 0x2007
 */
public class PPURegisters {
    public final static int SIZE = 8;
    protected int memory[];

    public PPURegisters() {
        memory = new int[SIZE];
    }
}
