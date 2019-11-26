package me.aymen.anes.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Access to Different memory portions in NES.
 * Internally encapsulates access to RAM, ROM and IO Registers
 */
public class Bus {
    private final Logger logger = LoggerFactory.getLogger(Bus.class);

    // Overall Bus size of 64k
    private static int SIZE = 0x10000;

    // Maximum Address PPU can Read
    // TODO use it when implementing PPU by having own read and write methods
    private static int PPU_MAX_ADDR = 0x2FFF;

    // Contains all memory a bus can have
    // Some of the memory locations will not be used due to mirroring
    // The decision not to have several variables is to ease access
    public final int[] memory;

    public Bus() {
        memory = new int[SIZE];
    }

    /**
     * Retrieve memory stored in memory according to index.
     * Takes into consideration mirror that make take place
     * @param index
     * @return
     */
    public int read(int index) {
        index = mapIndex(index);

        return memory[index];
    }

    /**
     * Writes memory to memory, except when ROM is expected
     * @param value
     * @param index
     */
    public void write(int value, int index) {
        if (index >= 0x8000)
            throw new IllegalArgumentException("Cannot write to ROM");

        index = mapIndex(index);

        memory[index] = value;
    }

    /**
     * Retreive the right index to access. This helps in cases where
     * mirroring is expected
     * @param index
     * @return
     */
    private int mapIndex(int index) {
        if(index < 0 || index > SIZE - 1) {
            logger.error("Error accessing memory at index {}", index);
            throw new IllegalArgumentException("Accessing beyond memory boundary at");
        }

        // Cartridge Space (0x4020 to 0xFFFF)
        if (index >= 0x4020)
            return index;

        // NES APU Space (0x4000 to 0x4017)
        // Disabled NES APU and IO registers Space (0x4018 to 0x401F)
        // Cartridge Space (0x4020 to 0xFFFF)
        // All accessed directly
        if ( (index & 0x4000) == 0x4000)
            return index;

        // IO Registers Space (0x2000 to 0x3FFF)
        if ( (index & 0x2000) == 0x2000) {
            // Repeat every 8 bytes
            // Address 0x2008 to 0x3FFF will mirror 0x2000 to 0x2007

            return (index % 8) + 0x2000;
        }

        // RAM Space (Ox0000 to 0x1FFF)
        // 2KB Internal RAM (0x0000 to 0x7FF)
        // Rest is mirrored back to Internal RAM
        return index % 0x800;
    }
}
