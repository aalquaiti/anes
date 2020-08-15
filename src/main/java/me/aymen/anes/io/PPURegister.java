package me.aymen.anes.io;

import me.aymen.anes.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents PPU IO Registers
 */
public class PPURegister {
    private final Logger logger = LoggerFactory.getLogger(PPURegister.class);

    /**
     * PPU Registers starting from memory address of 0x2000 to 0x2007.
     * Internally, they are viewed as 8 byte array
     */
    public final static int REG_SIZE = 8;

    // IO Components
    private final Bus bus;
    public int[] reg;

    // This variable is used to emulate the address latch effect that happens
    // when a write happens at PPU_ADDR. For CPU to write to VRAM,
    // it needs two writes at PPU_ADDR, to represent at 16 byte address.
    // The first right set the most significant byte, while the second sets
    // the least significant byte. Example to Write the address 0x1234
    // First Write: 0x12
    // Second Write: 0x34
    // This latch is reset when PPU_STATUS is read
    private boolean latchUsed;

    // Used to store the current address to write/read data to vram
    // when PPU_ADDR and PPU_DATA are used
    private int ppu_address;

    // Used to store previously read data. When reading data from PPU, it takes
    // another cycle to get the new value. The fetched value on first cycle is
    // just the previously buffered data. On the second read, new data is
    // buffered and fetched
    private int ppu_buffer;

    // Registers
    // For details:
    // https://wiki.nesdev.com/w/index.php/PPU_programmer_reference
    public static final int PPU_CTRL = 0;
    public static final int PPU_MASK = 1;
    public static final int PPU_STATUS = 2;
    public static final int OAM_ADDR = 3;
    public static final int OAM_DATA = 4;
    public static final int PPU_SCROLL = 5;
    public static final int PPU_ADDR = 6;
    public static final int PPU_DATA = 7;

    public PPURegister(Bus bus) {
        this.bus = bus;
        reg = new int[REG_SIZE];
    }

    /**
     * Read value of a register
     */
    public int read(int address) {
        int value = 0;

        switch (address) {
            case PPU_CTRL:
                value = reg[PPU_CTRL];
                break;
            case PPU_MASK:
                value = reg[PPU_MASK];
                break;
            case PPU_STATUS:
                // Reset address latch when status is read
                latchUsed = false;
                // TODO Check if this addition is important
                // Reading status set internal value to be as in the line below,
                // as being explained by Youtuber javidx9 in his video name:
                // NES Emulator Part #4
                // 0xE0 returns bits 7,6 5, while 0x1F handles the others from
                // the stored buffer value
                value = reg[PPU_STATUS] & 0xE0 | (ppu_buffer & 0x1F);
                // Reset Vertical Blanking when status is read
                setVerticalBlank(false);
                break;
            case OAM_ADDR:
                value = reg[OAM_ADDR];
                break;
            case OAM_DATA:
                // TODO implement increment to OAM_ADDR when not in vblank
                value = reg[OAM_DATA];
                reg[OAM_ADDR] = (reg[OAM_ADDR] + 1) & 0xFFFF;
                break;
            case PPU_SCROLL:
                logger.warn("Read from PPU SCROLL Register should not be done");
                value = reg[PPU_SCROLL];
                break;
            case PPU_ADDR:
                // This should not happen by developers, as this address is
                // supposed to be write only
                logger.warn("Read from PPU ADDR Register should not be done");
                break;
            case PPU_DATA:
                // Return the buffered data
                value = ppu_buffer;
                ppu_buffer = bus.ppuRead(ppu_address);

                // This is used to check type of accessed address, which
                // is used to check if it is for palette address. When accessing
                // PPU, it takes extra cycle to return fetched data, so reading
                // stored read data in a buffer and previous buffer value is
                // returned. Read from palette address, however, is returned
                // within same cycle.
                Pair<IO, Integer> pair = bus.ppuAccess(ppu_address);

                // Reading from PPU_DATA lead to increment of ppu address
                ppu_address = (ppu_address + getIncrementMode()) & 0xFFFF ;

                // Check if palette address is accessed
                if (pair.first == IO.PLTE) {
                    return ppu_buffer;
                }
                break;
            default:
                // Should never happen
                logger.error(String.format("Read from PPU Register to unknown" +
                        "address at %02x", address));
                System.exit(-1);
        }

        return value;
    }

    /**
     * Write value to a register. Depending on Register, this might lead to
     * writing value to other devices (e.g. vram)
     */
    public void write(int value, int address) {
        switch (address) {
            case PPU_CTRL:
                reg[PPU_CTRL] = value;
                break;
            case PPU_MASK:
                reg[PPU_MASK] = value;
                break;
            case PPU_STATUS:
                // This should not happen by developers, as this address is
                // supposed to be read only. Just log an error but do not exit
                logger.warn("Write to PPU STATUS Register is not supported");
                break;
            case OAM_ADDR:
                // TODO implement zeroing the value during specific ticks
                // Note from nesdev:
                // OAMADDR is set to 0 during each of ticks 257-320
                // of the pre-render and visible scanlines
                reg[OAM_ADDR] = value;
                break;
            case OAM_DATA:
                // TODO ignore writing during rendering period. Check
                // https://wiki.nesdev.com/w/index.php/PPU_programmer_reference#OAM_data_.28.242004.29_.3C.3E_read.2Fwrite
                // for details.
                reg[OAM_DATA] = value;
                bus.ppu.oam[reg[OAM_ADDR]] = value;
                reg[OAM_ADDR] = (reg[OAM_ADDR] + 1) & 0xFFFF;
                break;
            case PPU_SCROLL:
                updateInternalAddress(value);
                // TODO is this necessary
                reg[PPU_SCROLL] = value;
                break;
            case PPU_ADDR:
                updateInternalAddress(value);
                // TODO is the storage of the value necessary?
                 reg[PPU_ADDR] = value;
                break;
            case PPU_DATA:
                // VRAM address to write to should be ppu_address, which by now
                // should have been affected by two writes to PPU_ADDR
                bus.ppuWrite(value, ppu_address);
                // TODO should we write the value PPU_DATA as well?
                reg[PPU_DATA] = value;

                // Writing to PPU_DATA lead to increment of ppu address
                ppu_address = (ppu_address + getIncrementMode()) & 0xFFFF ;
                break;
            default:
                // Should never happen
                logger.error(String.format("Write to PPU Register to unknown" +
                        "address at %02x", address));
                System.exit(-1);
        }
    }

    /**
     * Update ppu internal address register. This register is used by different
     * PPU registers that uses vram for read/write
     */
    private void updateInternalAddress(int value) {
        // Set the address latch if not set.
        // The ppu_address will be updated accordingly
        // If latched is not used, then value will effect the
        // ppu_address most significant byte. If used, the address
        // will effect the ppu_address least significant byte
        if (!latchUsed) {
            latchUsed = true;
            ppu_address = (ppu_address & 0x00FF) + (value << 8);
        } else {
            latchUsed = false;
            ppu_address = (ppu_address & 0xFF00) + value;
        }
    }

    // PPU CTRL helper functions

    /**
     * Retrieve name table address that is stored in bits 0 and 1 of PPUCTRL.
     * Addresses of name table returned
     * 0: 0x2000
     * 1: 0x2400
     * 2: 0x2800
     * 3: 0x2C00
     */
    public int getBaseNameTableAddress() {
        int value =  reg[PPU_CTRL] & 0b11;
        switch (value) {
            case 0:
                return 0x2000;
            case 1:
                return 0x2400;
            case 2:
                return 0x2800;
            case 3:
                return 0x2C00;

        }

        // Must never happen. Indicates an implementation error
        throw new RuntimeException("Error while fetching PPU CTRL Name Table " +
                "Address");
    }

    /**
     * Retrieve address increment of VRAM address depending on bit 2 of
     * PPUCTRL:
     * 0: adds 1 (Go horizontal)
     * 1: adds 32 (Go vertical)
     * @return either 1 or 32 depend on bit 2 value
     */
    public int getIncrementMode() {
        if ( (reg[PPU_CTRL] & 0b100) == 0) {
            return 1;
        }

        return 32;
    }

    /**
     * Retrieve sprite name table address for 8x8 sprites in bit 3.
     * Addresses of name table returned
     * 0: 0x0000
     * 1: 0x1000
     * This register is ignored in 16x8 mode
     */
    public int getSpriteNameTableAddress() {
        return (reg[PPU_CTRL] & 0b1000) == 0? 0x0000: 0x1000;
    }

    /**
     * Retrieve background name table address in bit 4.
     * Addresses of name table returned
     * 0: 0x0000
     * 1: 0x1000
     */
    public int getBGNameTableAddress() {
        return (reg[PPU_CTRL] & 0x10) == 0? 0x0000: 0x1000;
    }

    /**
     * Determines is Sprite size is 8x8 pixel by checking value  in bit 5
     * 0: true, 8x8
     * 1: false, 16x8
     */
    public boolean isSprite8x8() {
        return (reg[PPU_CTRL] & 0x20) == 0;
    }

    /**
     * Non used mode for bit 6 that if enabled changes the palette index
     * for the background color, as being described in nesdev wiki.
     * Added here for completeness
     */
    public boolean isMasterMode() {
        return true;
    }

    // PPU Mask Helper functions

    /**
     * Determines if grey scale colors should be used instead of normal ones by
     * checking the value of bit 0.
     * When in greyscale mode, any values read from PPU Palette Address
     * (0x3F00 - 0x3FFF) is bitwise ANDed with 0x#30 which limits colors to
     * grey column (0x00, 0x10, 0x20, 0x30)
     * 0: Normal color
     * 1: greyscale colors
     */
    public boolean isGreyScaleMode() {
        return (reg[PPU_MASK] & 0b1) == 0b1;
    }

    /**
     * Determines whether to enable rendering background in the left most
     * 8 pixel columns, by checking the value of bit 1.
     * 0: disable
     * 1: enable
     */
    public boolean showLeftColumnBG(){
        return (reg[PPU_MASK] & 0b10) == 0b10;
    }

    /**
     * Determines whether to enable rendering sprites in the left most
     * 8 pixel columns, by checking the value of bit 2.
     * 0: disable
     * 1: enable
     */
    public boolean showLeftColumnSprite(){
        return (reg[PPU_MASK] & 0b100) == 0b100;
    }

    /**
     * Determines whether to enable rendering background, by checking the
     * value of bit 3.
     * 0: disable
     * 1: enable
     */
    public boolean showBG() {
        return (reg[PPU_MASK] & 0b1000) == 0b1000;
    }

    /**
     * Determines whether to enable rendering sprites, by checking the value
     * of bit 4.
     * 0: disable
     * 1: enable
     */
    public boolean showSprites() {
        return (reg[PPU_MASK] & 0x10) == 0x10;
    }

    /**
     * Determines whether to enable red color emphasize  by checking the value
     * of bit 5
     */
    public boolean emphasizeRed() {
        return (reg[PPU_MASK] & 0x20) == 0x20;
    }

    /**
     * Determines whether to enable red color emphasize  by checking the value
     * of bit 6
     */
    public boolean emphasizeGreen() {
        return (reg[PPU_MASK] & 0x40) == 0x40;
    }

    /**
     * Determines whether to enable red color emphasize  by checking the value
     * of bit 7
     */
    public boolean emphasizeBlue() {
        return (reg[PPU_MASK] & 0x80) == 0x80;
    }

    // PPU STATUS Helper functions

    /**
     * Determines if sprite overflow happened by checking bit 5.
     * Due to a hardware bug, the actual behaviour is different and false
     * positives exit.
     */
    public boolean isSpriteOverflow() {
        // TODO implement usage
        // Note from nesdev: This flag is set during sprite
        // evaluation and cleared at dot 1 (the second dot) of the
        // pre-render line.
        return (reg[PPU_STATUS] & 0x20) == 0x20;
    }

    /**
     * Set PPU STATUS bit 5 to indicate if sprite overflow occurred. It is
     * supposed to show that more than 8 sprites appears in the scanline, but
     * due to a hardware bug the actual behaviour is different and false
     * positives exit.
     */
    public void setSpriteOverflow(boolean overflow) {
        int value = overflow ? 0x20 : 0x20;
        reg[PPU_STATUS] |= value;
    }

    /**
     * Determines if a nonzero pixel of sprite 0 overlaps a nonzero background
     * pixel by checking bit 6
     */
    public boolean isSpriteZeroHit() {
        // TODO implement usage
        // Note from nesdev: cleared at dot 1 of the pre-render
        // line.  Used for raster timing
        return (reg[PPU_STATUS] & 0x40) == 0x40;
    }

    /**
     * Set PPU STATUS bit 6 to indicate a sprite zero hit happened, which occurs
     * when a nonzero pixel of sprite 0 overlaps a nonzero background pixel
     */
    public void setSpriteZeroHit(boolean hit) {
        int value = hit ? 0x40 : 0x40;
        reg[PPU_STATUS] |= value;
    }

    /**
     * Determines if nmi is enabled by checking the value of bit 7.
     * 0: NMI disabled
     * 1: NMI enabled
     */
    public boolean nmiEnabled() {
        // TODO implement usage
        // Note from nesdev:
        // Set at dot 1 of line 241 (the line *after* the post-render
        // line); cleared after reading $2002 and at dot 1 of the
        // pre-render line.
        return (reg[PPU_CTRL] & 0x80) == 0x80;
    }

    /**
     * Set PPU Status bit 7 to enable NMI
     * @param enable if true, sets bit 7 to 1, else sets it to 0
     */
    public void setVerticalBlank(boolean enable) {
        int value = enable ? 0x80 : 0x00;
        reg[PPU_STATUS] |= value;
    }
}
