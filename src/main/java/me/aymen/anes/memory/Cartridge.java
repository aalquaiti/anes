package me.aymen.anes.memory;

import me.aymen.anes.exception.InvalidROMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
;

/**
 * Representes NES cartridge inserted into the system.
 * Works in conjunction with Bus Object, where SRAM and ROM memory is stored
 */
public class Cartridge {

    //region Flags

    // Flags 6
    private boolean hasTrainer;

    //endregion

    private final static Logger logger =
            LoggerFactory.getLogger(Cartridge.class);
    private final Bus bus;
    private final int HEADER_SIZE = 16;
    private byte header[];
    private byte trainer[];
    private byte prg[];
    private int prgBank;
    private byte chr[];
    private int chrBank;
    private int mapperType;

    public Cartridge(Bus bus) {
        this.bus = bus;
        header = new byte[HEADER_SIZE];
    }

    /**
     * Read iNES and NES 2.0 ROM files
     * @param file path to file
     */
    public void load(String file) {
        try(FileInputStream in = new FileInputStream(file)) {
            logger.info("Reading file '{}'", file);
            int read = in.read(header);

            if (read < HEADER_SIZE)
                throw new InvalidROMException();

            // Initialise Flags
            // TODO Complete Flags implementation
            // Refer to wiki.nesdev.com/w/index.php/INES

            // Flags 6
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
            // got. Each block is 16384 bytes
            prgBank = header[4];
            prg = new byte[16384 * prgBank];
            read = in.read(prg);
            if(read < prg.length)
                throw new InvalidROMException("PRG ROM memory failed to be " +
                        "read. File might be corrupted");

            logger.info("PRG ROM size: {} bytes", prg.length);

            // Fifth header decides the size of CHR ROM (if any). Each block
            // is 8192 bytes in size
            chrBank = header[5];
            chr = new byte[8192 * chrBank];
            read = in.read(chr);
            if(read < chr.length)
                throw new InvalidROMException("CHR ROM memory failed to be " +
                        "read. File might be corrupted");

            logger.info("CHR ROM size: {} bytes", chr.length);

            // TODO Implement the rest of INES and NES 2.0 memory
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        loadToBus();
    }

    /**
     * Loads Cartridge ROM memory to bus
     */
    public void loadToBus() {

        if(hasTrainer) {
            for(int i =0 ; i < trainer.length; i++)
                bus.memory[0x7000 + i] = (int) trainer[i];
        }

        // Load ROM Data
        for(int i = 0; i < prg.length; i++)
            // Read it as unsigned byte
            bus.memory[0x8000 + i] = prg[i] & 0xFF;

        // If prg blocks are only 1, copy memory as well to 0xC000
        // This will cover all memory space from (0x8000 to 0xFFFF)
        if(prgBank == 1)
            for(int i = 0; i < prg.length; i++)
                bus.memory[0xC000 + i] = prg[i] & 0xFF;

        // TODO a better implementation would be to mirror values instead
    }

    public boolean hasTrainer() {
        return hasTrainer;
    }

    public byte[] getTrainer() {
        return trainer;
    }

    public byte[] getPRG() {
        return prg;
    }

    public byte[] getCHR() {
        return chr;
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
}
