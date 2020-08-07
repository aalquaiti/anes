package me.aymen.anes.io;

import me.aymen.anes.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Access to Different memory portions in NES.
 * Internally encapsulates access to RAM, ROM and IO Registers
 */
public class Bus {
    private final Logger logger = LoggerFactory.getLogger(Bus.class);

    /**
     * Maximum address accessed by CPU
     */
    public static int CPU_MAX_ADDR = 0xFFFF;

    /**
     * Maximum Address PPU accessed by PPU
     */
    // TODO use it when implementing PPU by having own read and write methods
    private static int PPU_MAX_ADDR = 0x2FFF;

    public final RAM ram;
    public final Cartridge rom;

    public Bus() {
        ram = new RAM();
        rom = new Cartridge();
    }

    /**
     * Retrieve value accessible by cpu according to address. See cpuIndex for details.
     * @param address
     * @return
     */
    public int cpuRead(int address) {
        Pair<CPUIO, Integer> pair = cpuAccess(address);

        switch (pair.first) {
            case RAM:
                return ram.memory[pair.second];

            case PPU_IO:
                // TODO implement
                throw new IllegalArgumentException();

            case APU_IO:
                // TODO implement
                throw new IllegalArgumentException();

            case PGR_ROM:
                // Internally, the implementation of ROM starts at 0x0000
                // so mod is used
                return rom.memory[pair.second % 0x8000];

            default:
                // Should never happen
                throw new IllegalArgumentException("Device at address not supported");
        }
    }

    /**
     * Writes value to device according to address. See cpuIndex for details.
     * @param value
     * @param address
     */
    public void cpuWrite(int value, int address) {
        Pair<CPUIO, Integer> pair = cpuAccess(address);

        switch (pair.first) {
            case RAM:
                ram.memory[pair.second ] = value % 0xFF;
                break;
            case PPU_IO:
                // TODO implement
                throw new IllegalArgumentException();

            case APU_IO:
                // TODO implement
                throw new IllegalArgumentException();

            case PGR_ROM:
                // TODO implement SRAM (Save RAM)
                throw new IllegalArgumentException();

            default:
                // Should never happen
                throw new IllegalArgumentException("Device at address not supported");
        }
    }

    /**
     * Retrieve the right device to access with appropriate address if mirroring is expected.
     * 0x0000 - 0x07FF: RAM
     * 0x8000 - 0x1FFF: Mirroring RAM
     * 0x2000 - 0x2007: PPU IO Registers
     * 0x2008 - 0x3FFF: Mirroring PPU IO Registers
     * 0x4000 - 0x401F: APU IO Registers
     * 0x4020 - 0xFFFF: Program ROM
     * @param address Address to access
     * @return String of device name and address
     */
    private Pair<CPUIO, Integer> cpuAccess(int address) {
        Pair<CPUIO, Integer> pair = new Pair<>();
        validBoundary(CPU_MAX_ADDR, address);

        // 0x4020 - 0xFFFF: Program ROM
        if (address >= 0x4020) {
            // TODO check if any mirroring should be done
            pair.first = CPUIO.PGR_ROM;
            pair.second = address;
        }

        // 0x4000 - 0x401F: APU IO Registers
        else if ( address >= 0x4000) {
            pair.first = CPUIO.APU_IO;
            pair.second = address;
        }

        // 0x2000 - 0x2007: PPU IO Registers
        // 0x2008 - 0x3FFF: Mirroring PPU IO Registers
        else if ( address >= 0x2000) {
            // Repeat every 8 bytes
            // Address 0x2008 to 0x3FFF will mirror 0x2000 to 0x2007
            pair.first = CPUIO.PPU_IO;
            pair.second = address % 0x8;
        }

        // 0x0000 - 0x07FF: RAM
        // 0x8000 - 0x1FFF: Mirroring RAM
        else {
            pair.first = CPUIO.RAM;
            pair.second = address % 0x800;
        }

        return pair;
    }

    /**
     * Checks if 0 <= value <= max
     * @throws IllegalArgumentException if value outside boundary
     */
    private void validBoundary(int max, int value) {
        if(value < 0 || value > max) {
            logger.error("Error accessing memory at address {}", value);
            throw new IllegalArgumentException(String.format("Accessing beyond memory boundary at $%02X", value));
        }
    }
}
