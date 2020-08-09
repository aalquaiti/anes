package me.aymen.anes.io;

import me.aymen.anes.cpu.CPU;
import me.aymen.anes.ppu.PPU;
import me.aymen.anes.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Access to Different memory portions in NES.
 * Internally encapsulates access to all devices, working as a mother board
 * for the system
 */
public class Bus {
    private final Logger logger = LoggerFactory.getLogger(Bus.class);

    /**
     * Maximum address accessed by CPU and PPU
     */
    public static int MAX_ADDR = 0xFFFF;

    public final CPU cpu;
    public final PPU ppu;
    public final RAM ram;
    public final VRAM vram;
    public final Cartridge rom;
    public final PPURegisters ppuReg;

    public Bus() {

        ram = new RAM();
        vram = new VRAM();
        rom = new Cartridge();
        ppuReg = new PPURegisters();

        cpu = new CPU(this);
        ppu = new PPU(this);
    }

    /**
     * Retrieve value accessible by cpu according to address. See cpuAccess for details.
     * @param address
     * @return
     */
    public int cpuRead(int address) {
        Pair<IO, Integer> pair = cpuAccess(address);

        switch (pair.first) {
            case RAM:
                return ram.memory[pair.second];

            case PPU_IO:
                return ppuReg.memory[pair.second];

            case APU_IO:
                // TODO implement
                throw new IllegalArgumentException(String.format("%2X", pair.second));

            case PGR_ROM:
                // Internally, the implementation of ROM starts at 0x0000
                // so mod is used
                return rom.readPRG(pair.second % 0x8000);

            default:
                // Should never happen
                throw new IllegalArgumentException("Device at address not supported");
        }
    }

    /**
     * Writes value to device according to address. See cpuAccess for details.
     * @param value
     * @param address
     */
    public void cpuWrite(int value, int address) {
        Pair<IO, Integer> pair = cpuAccess(address);

        switch (pair.first) {
            case RAM:
                ram.memory[pair.second ] = value & 0xFF;
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
     * Retrieve the right device to access by cpu with appropriate address
     * if mirroring is expected.
     * 0x0000 - 0x07FF: RAM
     * 0x8000 - 0x1FFF: Mirroring RAM
     * 0x2000 - 0x2007: PPU IO Registers
     * 0x2008 - 0x3FFF: Mirroring PPU IO Registers
     * 0x4000 - 0x401F: APU IO Registers
     * 0x4020 - 0xFFFF: Program ROM
     * @param address Address to access
     * @return Enum of device name and address
     */
    private Pair<IO, Integer> cpuAccess(int address) {
        Pair<IO, Integer> pair = new Pair<>();
        validBoundary(address);

        // 0x4020 - 0xFFFF: Program ROM
        if (address >= 0x4020) {
            // TODO check if any mirroring should be done
            pair.first = IO.PGR_ROM;
            pair.second = address;
        }

        // 0x4000 - 0x401F: APU IO Registers
        else if ( address >= 0x4000) {
            pair.first = IO.APU_IO;
            pair.second = address;
        }

        // 0x2000 - 0x2007: PPU IO Registers
        // 0x2008 - 0x3FFF: Mirroring PPU IO Registers
        else if ( address >= 0x2000) {
            // Repeat every 8 bytes
            // Address 0x2008 to 0x3FFF will mirror 0x2000 to 0x2007
            pair.first = IO.PPU_IO;
            pair.second = address % 0x8;
        }

        // 0x0000 - 0x07FF: RAM
        // 0x8000 - 0x1FFF: Mirroring RAM
        else {
            pair.first = IO.RAM;
            pair.second = address & 0x7FF;
        }

        return pair;
    }

    /**
     * Retrieve value accessible by ppu according to address. See ppuAccess for details.
     * @param address
     * @return
     */
    public int ppuRead(int address) {
        Pair<IO, Integer> pair = ppuAccess(address);

        switch (pair.first) {
            case CHR_ROM:
                return rom.readPRG(pair.second);
            case VRAM:
                return vram.memory[pair.second];
            case PLTE:
                return ppu.palette[pair.second];
            default:
                // Should never happen
                throw new IllegalArgumentException("Device at address not supported");
        }
    }

    public void ppuWrite() {
        // TODO implement
        throw new IllegalArgumentException();
    }

    /**
     * Retrieve the right device to access by ppu with appropriate address
     * if mirroring is expected.
     * 0x0000 - 0x01FF: Pattern Tables (CHR-ROM)
     * 0x2000 - 0x2FFF: Name Tables (VRAM)
     * 0x3000 - 0x3EFF: Mirrors 0x2000 to 0x2EFF
     * 0x3F00 - 0x3FFF: Palettes (PPU Internal Memory)
     * 0x4000 - 0xFFFF: Mirrors 0x0000 to 0x3FFF
     * @param address Address to access
     * @return Enum of device name and address
     */
    private Pair<IO, Integer> ppuAccess(int address) {
        Pair<IO, Integer> pair = new Pair<>();
        validBoundary(address);

        // 0x4000 - 0xFFFF: Mirrors 0x0000 to 0x3FFF
        address %= 0x4000;

        // 0x3F00 - 0x3FFF: Palettes (PPU Internal Memory)
        if (address >= 0x3F00) {
            pair.first = IO.PLTE;
            pair.second = address % 0x100;
        }

        // TODO implement access to other addresses
        else if (address >= 0x2000) {
            pair.first = IO.VRAM;
            // TODO part of the vram memory is mirrored but can be remapped
            //  to RAM on cartridge according to
            //  https://wiki.nesdev.com/w/index.php/PPU_programmer_reference#Hardware_mapping
            pair.second = address % 0x800;
        }

        // 0x0000 - 0x01FF: Pattern Tables
        else {
            pair.first = IO.CHR_ROM;
            pair.second = address;
        }

        return pair;
    }

    /**
     * Checks if 0 <= value <= MAX_ADDRESS
     * @throws IllegalArgumentException if value outside boundary
     */
    private void validBoundary(int value) {
        if(value < 0 || value > MAX_ADDR) {
            logger.error("Error accessing memory at address {}", value);
            throw new IllegalArgumentException(String.format("Accessing beyond memory boundary at $%02X", value));
        }
    }
}

