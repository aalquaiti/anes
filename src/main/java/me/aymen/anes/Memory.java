package me.aymen.anes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Access to Different data portions in NES.
 * Internally encapsulates access to Memory, ROM and IO Registers
 */
public class Memory {
    private final Logger logger = LoggerFactory.getLogger(Memory.class);
    public static int SIZE = 0x10000;
    protected final int[] data;

    public Memory() {
        data = new int[SIZE];
    }

    /**
     * Retrieve data stored in memory according to index.
     * Takes into consideration mirror that make take place
     * @param index
     * @return
     */
    public int read(int index) {
        index = mapIndex(index);

        return data[index];
    }

    /**
     * Writes data to memory, except when ROM is expected
     * @param value
     * @param index
     */
    public void write(int value, int index) {
        if (index >= 0x8000)
            throw new IllegalArgumentException("Cannot write to ROM");

        index = mapIndex(index);

        data[index] = value;
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
            throw new IllegalArgumentException("Accessing beyond data boundary at");
        }

        // NES APU Space (0x4000 to 0x4017)
        // Disabled NES APU and IO registers Space (0x4018 to 0x401F)
        // Cartridge Space (0x4020 to 0xFFFF)
        // All accessed directly
        if ( (index & 0x4000) == 0x4000)
            return data[index];

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

    public void loadROM(Cartridge cart) {
        // TODO remove it from here
        // Either make it the responsibility of a board
        // or implement Memory so that it maps to different
        // memory components
        if(cart.hasTrainer()) {
            byte[] trainer = cart.getTrainer();
            for(int i =0 ; i < trainer.length; i++)
                data[0x7000 + i] = (int) trainer[i];
        }

        // Load ROM Data
        byte[] prg = cart.getPRG();
        for(int i = 0; i < prg.length; i++)
            // Read it as unisgned byte
            data[0x8000 + i] = prg[i] & 0xFF;

        // If prg blocks are only 1, copy data as well to 0xC000
        // This will cover all memory space from (0x8000 to 0xFFFF)
        if(cart.getPrgBank() == 1)
            for(int i = 0; i < prg.length; i++)
                data[0xC000 + i] = prg[i] & 0xFF;

        // TODO a better implementation would be to mirror values instead
    }
}
