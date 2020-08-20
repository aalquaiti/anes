package me.aymen.anes.io;

import me.aymen.anes.exception.InvalidROMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
;

/**
 * Represents NES cartridge inserted into the system.
 */
public class Cartridge {

    /**
     * ROM bank size
     */
    public static final int BANK_SIZE = 0x4000;

    //region Flags

    // Flag 6
    private boolean hasTrainer;

    //endregion

    private final static Logger logger =
            LoggerFactory.getLogger(Cartridge.class);
    private final int HEADER_SIZE = 16;
    private byte header[];
    private byte trainer[];
    private byte prgMemory[];
    private int prgBank;
    private byte chrMemory[];
    private int chrBank;
    private int mapperType;
    private int mirroring;

    public Cartridge() {
        header = new byte[HEADER_SIZE];
    }

    /**
     * Reads iNES and NES 2.0 ROM files
     * @param file path to file
     */
    public void load(String file) {
        // TODO add documentation of file structure
        try(FileInputStream in = new FileInputStream(file)) {
            logger.info("Reading file '{}'", file);
            int read = in.read(header);

            if (read < HEADER_SIZE)
                throw new InvalidROMException();

            // Initialise Flags
            // TODO Complete Flags implementation
            // Refer to wiki.nesdev.com/w/index.php/INES

            // Flags 6
            mirroring = (header[6] & 0x1);
            hasTrainer = (header[6] & 0x8) == 0x8;
            mapperType = (byte) (header[6] >> 4);

            // Logging info
            logger.info("Mapper Type: {}", mapperType);

            // First bytes must be 'NES' followed by MS DOS EOF
            if( header[0] != 'N' || header[1] != 'E' || header [2] != 'S'
                    || header[3] != 0x1A)
                throw new InvalidROMException("Invalid header entry");

            // If file has trainer, read 512 bytes
            if (hasTrainer) {
                trainer = new byte[512];
                read = in.read(trainer);

                if(read < trainer.length)
                    throw new InvalidROMException("Trainer memory failed to be" +
                            "read. File might be corrupted");
                logger.info("Trainer size: {} bytes", trainer.length);
            }

            // Fourth header decides the size how many blocks PRG ROM memory
            // got. Each block is 0x4000 (16384) bytes
            prgBank = header[4];
            prgMemory = new byte[0x4000  * prgBank];
            read = in.read(prgMemory);
            if(read != prgMemory.length)
                throw new InvalidROMException("PRG ROM memory failed to be " +
                        "read. File might be corrupted");

            logger.info("PRG ROM size: {} bytes", prgMemory.length);

            // Fifth header decides the size of CHR ROM (if any). Each block
            // is 0x2000 (8192) bytes in size
            chrBank = header[5];
            chrMemory = new byte[0x2000 * chrBank];
            read = in.read(chrMemory);
            if(read != chrMemory.length)
                throw new InvalidROMException("CHR ROM memory failed to be " +
                        "read. File might be corrupted");

            logger.info("CHR ROM size: {} bytes", chrMemory.length);

            // TODO Implement the rest of INES and NES 2.0 memory
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO implement SRAM area
//        if(hasTrainer) {
//            for(int i =0 ; i < trainer.length; i++) {
//                //bus.memory[0x7000 + i] = (int) trainer[i];
//
//            }
//        }
    }

    /**
     * Reads value from PRG Bank. Notice this methods should be called from a bus,
     * which will mirror values correctly. Check romIndex for details.
     * @param address
     * @return
     */
    public int readPRG(int address) {

        // TODO implement access to other areas of PRG-ROM
        if (prgBank == 1)
            // If there is only one prg rom bank
            address %= 0x4000;

        // Return as unsigned byte
        return prgMemory[address] & 0xFF ;
    }

    /**
     * Reads value from CHR Bank. Notice this methods should be called from a bus,
     * which will mirror values correctly.
     * @param address
     * @return
     */
    public int readCHR(int address) {

        // TODO implement access to other areas of PRG-ROM

        // Return as unsigned byte
        return chrMemory[address] & 0xFF;
    }

    // TODO add romAccess method that define what part of PRG-ROM is being accessed:
    // 0x4020 - 0x5FFF: Expansion ROM
    // 0x6000 - 0x7FFF: SRAM
    // 0x8000 - 0xFFFF: Lower and Upper Bank

    public boolean hasTrainer() {
        return hasTrainer;
    }

    public byte[] getTrainer() {
        return trainer;
    }

    public byte[] getPRG() {
        return prgMemory;
    }

    public byte[] getCHR() {
        return chrMemory;
    }

    public int getMapperType() {
        return mapperType;
    }

    public int getPrgBank() {
        return prgBank;
    }

    public int getChrBank() {
        return chrBank;
    }

    // TODO implement Cartridge handling VRAM
    public boolean handlesVRAM() {
        return false;
    }

    public int map(int address) {
        // TODO implement mappers and proper mirroring mechanism
        // Horizontal
        if (mirroring == 0) {
            address %= 0x800;
        }
        // Vertical
        else {
            if (address < 0x800) {
                address %= 0x400;
            } else {
                address = (address % 0x400) + 0x400;
            }
        }

        return address;
    }

    public int readVRAM(int address) {
        // TODO implement

        logger.error("Reading VRAM through Cartridge is not implemented");
        System.exit(-1);

        // Not reachable
        return 0;
    }

    public void writeVRAM(int value, int address) {
        // TODO implement
    }
}
