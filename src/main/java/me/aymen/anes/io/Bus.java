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

    public Bus() {
        ram = new RAM();
        vram = new VRAM();
        rom = new Cartridge();
        cpu = new CPU(this);
        ppu = new PPU(this);
    }

    /**
     * Retrieve value from a device according to address. See cpuAccess for
     * details.
     * @param address
     * @return
     */
    public int cpuRead(int address) {
        Pair<IO, Integer> pair = cpuAccess(address);

        switch (pair.first) {
            case RAM:
                return ram.memory[pair.second];

            case PPU_IO:
                return ppu.read(pair.second);

            case APU_IO:
                // TODO implement
                logger.error("Reading APU Register not supported");

            case OAM_DMA:
                // TODO implement
                logger.error("Reading OAM_DMA not supported");
                return 0;

            case PGR_ROM:
                // Internally, the implementation of ROM starts at 0x0000
                // so mod is used
                return rom.readPRG(pair.second % 0x8000);

            default:
                // Should never happen
                logger.error(String.format("CPU Read for unknown device at " +
                        "address $%04X not supported", pair.second));
                System.exit(-1);

                // This statement will never be reached
                return 0;
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
                ram.memory[pair.second] = value & 0xFF;
                break;
            case PPU_IO:
                ppu.write(value & 0xFF, pair.second);
                break;
            case APU_IO:
                // TODO implement
                logger.error("Write to APU registers not supported");
                break;
            case OAM_DMA:
                // CPU Address to start copying 0xFF bytes from to OAM memory
                // is value written to Register * 0x100
                int index = 0x100 * value;
                for(int i = 0; i < PPU.OAM_SIZE; i++) {
                    ppu.oam[i] = cpuRead(index + i);
                }
                // It takes 513 or 514 cycles to copy data from cpu to ppu
                // The +1 happens on an odd cpu cycle

                cpu.addCycles(512);
                break;
            case PGR_ROM:
                // TODO implement SRAM (Save RAM)
                logger.error("Write to PGR ROM not supported");
                break;
            default:
                // Should never happen
                logger.error(String.format("CPU Write for unknown device at " +
                        "address $%04X not supported", pair.second));
                System.exit(-1);
        }
    }

    /**
     * Retrieve the right device to access by cpu with appropriate address
     * if mirroring is expected.
     * 0x0000 - 0x07FF: RAM
     * 0x8000 - 0x1FFF: Mirroring RAM
     * 0x2000 - 0x2007: PPU IO Registers (additionally 0x4014)
     * 0x2008 - 0x3FFF: Mirroring PPU IO Registers
     * 0x4000 - 0x401F: APU IO Registers (Except for 0x4014, which belongs
     *                  to PPU)
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

        // OAM_DMA at 0x4014 (PPU Register used for Data Management access)
        else if (address == 0x4014) {
            pair.first = IO.OAM_DMA;
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
     * Retrieve value from a device according to address. See ppuAccess for
     * details.
     * @param address
     * @return
     */
    public int ppuRead(int address) {
        Pair<IO, Integer> pair = ppuAccess(address);

        switch (pair.first) {
            case CHR_ROM:
                return rom.readCHR(pair.second);
            case VRAM:
                if (rom.handlesVRAM()) {
                    return rom.readVRAM(pair.second);
                }

                return vram.memory[rom.map(pair.second)];
            case PLTE:
                int value = ppu.palette[pair.second];
                // Limit colors to grey column is greyscale flag is on
                // TODO test greyscale mode
                if (ppu.isGreyScaleMode()) {
                    value &= 0x30;
                }
                return value;
            default:
                // Should never happen
                logger.error(String.format("PPU Read for unknown device at " +
                        "address $%04X not supported", pair.second));
                System.exit(-1);

                // This statement will never be reached
                return 0;
        }
    }

    /**
     * Writes value to device according to address. See ppuAccess for details.
     * @param value
     * @param address
     */
    public void ppuWrite(int value, int address) {
        Pair<IO, Integer> pair = ppuAccess(address);

        switch (pair.first) {
            case CHR_ROM:
                // TODO check if CHR ROM can be treated as RAM
                logger.error("Write to CHR ROM not supported");
                break;
            case VRAM:
                if (rom.handlesVRAM()) {
                    rom.writeVRAM(value, pair.second);
                } else {
                    vram.memory[rom.map(pair.second)] = value;
                }
                break;
            case PLTE:
                ppu.palette[pair.second] = value;
                break;
            default:
                // Should never happen
                logger.error(String.format("PPU Write for unknown device " +
                        "address $%04X not supported", pair.second));
                System.exit(-1);
        }
    }

    /**
     * Retrieve the right device to access by ppu with appropriate address
     * if mirroring is expected.
     * 0x0000 - 0x1FFF: Pattern Tables (CHR-ROM)
     * 0x2000 - 0x2FFF: Name Tables (VRAM)
     * 0x3000 - 0x3EFF: Mirrors 0x2000 to 0x2EFF
     * 0x3F00 - 0x3F19: Palettes (PPU Internal Memory)
     * 0x3F20 - 0x3FFF: Mirrors 0x3F00 to 0x3F19
     * 0x4000 - 0xFFFF: Mirrors 0x0000 to 0x3FFF
     * @param address Address to access
     * @return Enum of device name and address
     */
    public Pair<IO, Integer> ppuAccess(int address) {
        Pair<IO, Integer> pair = new Pair<>();
        validBoundary(address);

        // 0x4000 - 0xFFFF: Mirrors 0x0000 to 0x3FFF
        address %= 0x4000;

        // 0x3F00 - 0x3FFF: Palettes (PPU Internal Memory)
        if (address >= 0x3F00) {

            // 0x3F20 - 0x3FFF: Mirrors 0x3F00 to 0x3F19
            address %= 0x20;
            // Color at 0x3F00 is the background which is mirrored every four
            // bytes. This affects addresses 0x3F04, 0x3F08, 0x3F0C, 0x3F10,
            // 0x3F14, 0x3F18, 0x3F1C
            if (address == 0x04) address = 0x00;
            if (address == 0x08) address = 0x00;
            if (address == 0x0C) address = 0x00;
            if (address == 0x10) address = 0x00;
            if (address == 0x14) address = 0x00;
            if (address == 0x18) address = 0x00;
            if (address == 0x1C) address = 0x00;

            pair.first = IO.PLTE;
            pair.second = address;
        }

        // TODO implement access to other addresses
        else if (address >= 0x2000) {
            pair.first = IO.VRAM;
            // TODO part of the vram memory is mirrored but can be remapped
            //  to RAM on cartridge according to
            //  https://wiki.nesdev.com/w/index.php/PPU_programmer_reference#Hardware_mapping

            // Address is mod with 0x1000 as there could be four name tables
            // and attribute tables from 0x2000 to 0x3000
            // Although internal PPU VRAM is 0x800 in size, hardware mapping
            // through cartridge could extend this to access additional memory.
            // This is why further cartridge mapping happens in ppuRead and
            // ppuWrite methods
            pair.second = address % 0x1000;
        }

        // 0x0000 - 0x1FFF: Pattern Tables
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
            logger.error(String.format(
                    "Cannot access memory at address $%04X", value));
            System.exit(-1);
        }
    }

    /**
     * Executes one clock cycle (Hertz) for the system.
     */
    public void clock() {
        // PPU clock runs three times faster than cpu due to Johnson counter
        // See https://wiki.nesdev.com/w/index.php/Cycle_reference_chart
        // TODO move this function to be part of bus clock as this helps
        //  to manage bus related functionality such as DMA
        ppu.clock();
        ppu.clock();
        ppu.clock();


        cpu.clock();

        if (ppu.isNmi()) {
            ppu.setNmi(false);
            cpu.nmi();
        }
    }
}

