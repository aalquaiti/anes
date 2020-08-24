package me.aymen.anes.ppu;

import me.aymen.anes.io.Bus;
import me.aymen.anes.io.IO;
import me.aymen.anes.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Represents NES Picture Processing Unit
 */
public class PPU {
    private final Logger logger = LoggerFactory.getLogger(PPU.class);

    /**
     * PPU Registers accessed by cpu through bus from memory address of
     * 0x2000 to 0x2007.
     * Internally, they are viewed as 8 byte array
     */
    public final static int REG_SIZE = 8;
    public final static int OAM_SIZE = 0xFF;
    public final static int PALETTE_SIZE = 0x20;
    /**
     * Background Pattern Table 16-bit Shift register
     */
    public final static int BG_NT_SIZE = 16;

    /**
     * Background Pattern Table 8-bit Shift register
     */
    public final static int BG_AT_SIZE = 8;

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

    // PPU Components
    private Bus bus;
    public int[] reg;
    // Object Attribute Memory
    // Controlled by cpu through memory mappers:
    // OAMADDR $2003
    // OAMDATA $2004
    // OAMDMA $4014
    // Can be seen as an array of 64 entries.
    // TODO Implement that as described at
    //  https://wiki.nesdev.com/w/index.php/PPU_programmer_reference#Hardware_mapping
    // TODO consider having oam and palette memory as separated from PPU
    //   Although they are internal to PPU, they are accessed through bus which
    //   makes the architecture cleaner and easier to follow
    public final int[] oam;
    public final int[] palette;      // Palette Memory
    private Screen screen;
    public final ColorPalette colors;
    public int scanLine;
    public int cycle;
    private boolean complete;  // Indicates whole frame was drawn
    // Indicates the ppu entered vblank and wants to trigger a non-maskable
    // interrupt
    private boolean nmi;

    // PPU internal registers

    // This variable is used to emulate the address latch effect that happens
    // when a write happens at PPU_ADDR or PPU_SCROLL. For CPU to communicate
    // to VRAM, it needs two writes at PPU_ADDR or PPU_SCROLL, to represent at
    // 16 byte address. The first right set the most significant 8 bits,
    // while the second sets the least significant 8 bits. Example to Write
    // the address 0x1234:
    // First Write: 0x12
    // Second Write: 0x34
    // This latch is reset when PPU_STATUS is read
    // Also known as w
    private boolean ppu_w;

    // Used to store the current address to write/read data to vram.
    // Consists of 15 bits
    public final PPUAddress ppu_v;

    // Used to store the address of the top left onscreen tile
    // Consists of 15 bits, and with ppu_address used for rendering and
    // scrolling
    public final PPUAddress ppu_t;

    // Used to indicate the current column of the tile being rendered
    // Consists of 3 bits
    private int fineX;

    // Used to store previously read data. When reading data from PPU, it takes
    // another cycle to get the new value. The fetched value on first cycle is
    // just the previously buffered data. On the second read, new data is
    // buffered and fetched
    private int ppu_buffer;

    // Four Background Registers used for rendering
    // NT for Name Table
    // AT for Attribute Table
    private int currentNT;
    private int nextNT;
    private int currentAT;
    private int nextAT;

    // stores values used for fetch process during background evaluation
    private int nextNTAddr;
    private int nextATBits;
    private int lowBGByte;
    private int highBGByte;

    public PPU(Bus bus) {
        this.bus = bus;
        reg = new int[REG_SIZE];
        oam = new int[OAM_SIZE];
        palette = new int[PALETTE_SIZE];
        colors = new ColorPalette();

        ppu_v = new PPUAddress();
        ppu_t = new PPUAddress();

        // Set scanline to prerender
        scanLine = -1;
        cycle = 0;
    }

    /**
     * Connects ppu to a screen
     * @param screen
     */
    public void connect(Screen screen) {
        this.screen = screen;
    }

    public boolean isNmi() {
        return nmi;
    }

    public void setNmi(boolean nmi) {
        this.nmi = nmi;
    }

//    /**
//     * Read value of a register
//     */
//    public int read(int address) {
//        return read(address, false);
//    }

    /**
     * Read value of a register
     * @param update whether to increment ppu_v when data is read
     *               (when address is PPU_DATA)
     */
    public int read(int address, boolean update) {
        int value = 0;

        switch (address) {
            case PPU_CTRL:
                value = reg[PPU_CTRL];
                break;
            case PPU_MASK:
                value = reg[PPU_MASK];
                break;
            case PPU_STATUS:
                if (update) {
                    // Reset address latch when status is read
                    ppu_w = false;
                    // Reset Vertical Blanking when status is read
                    setVerticalBlank(false);
                }
                // TODO Check if this addition is important
                // Reading status set internal value to be as in the line below,
                // as being explained by Youtuber javidx9 in his video name:
                // NES Emulator Part #4
                // 0xE0 returns bits 7,6 5, while 0x1F handles the others from
                // the stored buffer value
                value = (reg[PPU_STATUS] & 0xE0) | (ppu_buffer & 0x1F);

                break;
            case OAM_ADDR:
                value = reg[OAM_ADDR];
                break;
            case OAM_DATA:
                // TODO implement the effect of returning internal data
                //   when reading while ppu is rendering
                value = oam[OAM_ADDR];
                if (update) {
                    if (!getVerticalBlank()) {
                        reg[OAM_ADDR] = (reg[OAM_ADDR] + 1) & 0xFF;
                    }
                }
                break;
            case PPU_SCROLL:
                value = reg[PPU_SCROLL];
                break;
            case PPU_ADDR:
                value = reg[PPU_ADDR];
                break;
            case PPU_DATA:

                // Return the real value when no update is intentioned
                // This is for when reading value from non instruction
                // Used for debuging, deassembly and other internal needs
                if(!update) {
                    return bus.ppuRead(ppu_v.getValue());
                }

                // Return the buffered data
                value = ppu_buffer;
                ppu_buffer = bus.ppuRead(ppu_v.getValue());

                // This is used to check type of accessed address, which
                // is used to check if it is for palette address. When accessing
                // PPU, it takes extra cycle to return fetched data, so reading
                // stored read data in a buffer and previous buffer value is
                // returned. Read from palette address, however, is returned
                // within same cycle.
                Pair<IO, Integer> pair = bus.ppuAccess(ppu_v.getValue());

                // Reading from PPU_DATA lead to increment of ppu address
                ppu_v.increment(getIncrementMode());

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
    public void write(int value, int address, boolean update) {
        switch (address) {
            case PPU_CTRL:
                reg[PPU_CTRL] = value;
                // name table address stored in bits 10 and 11
                // t: ...BA.. ........ = d: ......BA
                ppu_t.setNameTable(value);
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
                // Data is written only when not within pre-render and
                // visible scanline, assuming background or sprite rendering
                // is enabled
                if (scanLine >= 240 && !renderEnabled()) {
                    oam[reg[OAM_ADDR]] = value;
                }
                else {
                    // TODO implement the glitch that modifies and bumps
                    //  the high 6 bits of OAM_ADDR
                }
                reg[OAM_ADDR] = (reg[OAM_ADDR] + 1) & 0xFF;
                break;
            case PPU_SCROLL:
                reg[PPU_SCROLL] = value;
                 /*
                    First Write:
                    t: ....... ...HGFED = d: HGFED...
                    x:              CBA = d: .....CBA
                    w:                  = 1
                  */
                if(!ppu_w) {
                    ppu_t.setCoarseX(value >> 3);
                    fineX = value & 0x7;
                    ppu_w = true;
                }
                /*
                    Second Write:
                    t: CBA..HG FED..... = d: HGFEDCBA
                    w:                  = 0
                 */
                else {
                    ppu_t.setCoarseY(value  >> 3);
                    ppu_t.setFineY(value);
                    ppu_w = false;
                }
                break;
            case PPU_ADDR:
                reg[PPU_ADDR] = value;
                /*
                    First Write:
                    t: .FEDCBA ........ = d: ..FEDCBA
                    t: X...... ........ = 0
                    w:                  = 1
                 */
                if(!ppu_w) {
                    int data = value & 0x3F;
                    ppu_t.setSecondHalf(data);
                    ppu_w = true;
                }
                else {
                /*
                    Second Write:
                    t: ....... HGFEDCBA = d: HGFEDCBA
                    v                   = t
                    w:                  = 0
                 */
                    int data = value & 0xFF;
                     ppu_t.setFirstHalf(data);
                     ppu_v.setValue(ppu_t.getValue());
                     ppu_w = false;
                }
                break;
            case PPU_DATA:
                // VRAM address to write to should be ppu_address, which by now
                // should have been affected by two writes to PPU_ADDR
                bus.ppuWrite(value, ppu_v.getValue());
                // TODO should we write the value PPU_DATA as well?
                reg[PPU_DATA] = value;

                // Writing to PPU_DATA lead to increment of ppu address
                if(update) {
                    ppu_v.increment(getIncrementMode());
                }
                break;
            default:
                // Should never happen
                logger.error(String.format("Write to PPU Register to unknown" +
                        "address at %02x", address));
                System.exit(-1);
        }
    }

    // PPU CTRL helper functions

    /**
     * Retrieve name table address, which were set through bit 0 and 1 of
     * PPU_CTRL. These bits are stored in ppu_v bits 10 and 11
     * Addresses of name table returned
     * 0: 0x2000
     * 1: 0x2400
     * 2: 0x2800
     * 3: 0x2C00
     */
    public int getBaseNameTable() {
        int value =  ppu_v.getNameTable();
        switch (value) {
            case 0:
                return 0x2000;
            case 1:
                return 0x2400;
            case 2:
                return 0x2800;
            case 3:
                return 0x2C00;
            default:
                // Must never happen. Indicates an implementation error
                throw new RuntimeException("Error while fetching PPU CTRL " +
                        "Name Table Address");
        }
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
     * Retrieve sprite pattern table address for 8x8 sprites in bit 3.
     * Addresses of name table returned
     * 0: 0x0000
     * 1: 0x1000
     * This register is ignored in 16x8 mode
     */
    public int getSprtPatternTableAddr() {
        return (reg[PPU_CTRL] & 0b1000) == 0? 0x0000: 0x1000;
    }

    /**
     * Retrieve background pattern name table address in bit 4.
     * Addresses of name table returned
     * 0: 0x0000
     * 1: 0x1000
     */
    public int getBGPatternTableAddr() {
        return (reg[PPU_CTRL] & 0x10) == 0? 0x0000: 0x1000;
    }

    /**
     * Determines is Sprite size is 8x8 pixel by checking value in bit 5
     * 0: true, 8x8
     * 1: false, 16x8
     */
    public boolean isSprite8x8() {
        return (reg[PPU_CTRL] & 0x20) == 0;
    }

    /**
     * Not used mode of PPU_CTRL bit 6 that if enabled changes the palette
     * index for the background color, as being described in nesdev wiki.
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
        int value = overflow ? 0x20 : 0x00;
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
        int value = hit ? 0x40 : 0x00;
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
     * Determine if vblank flag is set by retrieving PPU Status bit 7
     * @return true if value equals 1
     */
    public boolean getVerticalBlank() {
        return (reg[PPU_STATUS] & 0x80) == 0x80;
    }

    /**
     * Set PPU Status bit 7 to enable NMI
     * @param enable if true, sets bit 7 to 1, else sets it to 0
     */
    public void setVerticalBlank(boolean enable) {
        int value = enable ? 0x80 : 0x00;
        reg[PPU_STATUS] |= value;
    }

    /**
     * Increment CoarseX by 1 with wrapping.
     * As coarse X is 5 bits (bit 0-4 of ppu_v), maximum value is 31 (0x1F).
     * When incremented, it wraps back to 0, and switch bit 10 of ppu_v, which
     * is responsible for horizontal name table.
     * Only happens when rendering is enabled
     */
    public void incCoarseX() {
        if (!renderEnabled()) {
            return;
        }

        if (ppu_v.getCoarseX() == 31) {
            ppu_v.setCoarseX(0);
            ppu_v.flipHorizontalNameTable();
        }
        else {
            ppu_v.setCoarseX(ppu_v.getCoarseX() + 1);
        }
    }

    /**
     * Increment fineY by 1 with wrapping.
     * As fine Y is 3 bits (bit 12-14 of ppu_v), maximum value is 7.
     * When incremented, it wraps back to 0, and increment coarseY.
     * Only happens when rendering is enabled
     */
    public void incFineY() {
        if (!renderEnabled()) {
            return;
        }

        if(ppu_v.getFineY()==7) {
            ppu_v.setFineY(0);
            incCoarseY();
        }
        else {
            ppu_v.setFineY(ppu_v.getFineY() + 1);
        }
    }

    /**
     * Increment CoarseX by 1 with wrapping.
     * Coarse X is 5 bits (bit 0-4 of ppu_v), by maximum value is 29 due to that
     * there is only 30 row tiles.
     * When CoarseY is 29 and is incremented, it wraps back to 0, and switch
     * bit 11 of ppu_v, which is responsible for vertical name table
     * However, due to that CoarseY could be set to more than 29, this would
     * lead to reading from attribute table. When coarseY is 31 and incremented,
     *  it wraps back to 0 but without switching bit 11 of ppu_v.
     */
    public void incCoarseY() {
        if (ppu_v.getCoarseY() == 29) {
            ppu_v.setCoarseY(0);
            ppu_v.flipVerticalNameTable();     // Switch value of bit 11
        }
        else if (ppu_v.getCoarseY() == 31) {
            ppu_v.setCoarseY(0);
        }
        else {
            ppu_v.setCoarseY(ppu_v.getCoarseY() + 1);
        }
    }

    /**
     * Emulates one tick of PPU time.
     * PPU has 262 scanlines (-1 to 261). Each scan consists of 341 cycles
     * (0 to 340). Scanline (-1) is a pre-render scanline that is responsible
     * of loading the first two tiles to the registers.
     * In visible area (i.e. scan lines 0 to 239 and cycles 1 to 256),
     * if rendering is enabled, a pixel is drawn in each tick, where
     * x = cycle and y = scanline.
     * NTSC PPU Frame reference:
     * https://wiki.nesdev.com/w/images/4/4f/Ppu.svg
     */
    public void clock() {

//        System.out.println(String.format("PPU: %03d, %03d  fineX: %02d  " +
//                        "CoarseX: %02d  fineY: %02d  CoarseY: %02d",
//                cycle, scanLine, shiftPos, ppu_v.getCoarseX(), ppu_v.getFineY(),
//                ppu_v.getCoarseY()));

        // Scanline -1 to 239: Pre-render and visible scan lines
        // if rendering is enabled, draw pixel with each tick
        // according to PPU Frame reference
        if (scanLine >= -1 && scanLine < 240) {

            // When in odd frame (scanline 0, cycle 0), skip one cycle.
            // This only happens when render is enabled
            // nes dev describe this as important to overcome the way ppu
            // hardware renders to screen
            if (scanLine == 0 && cycle == 0 & renderEnabled()) {
                cycle = 1;
            }

            // In pre-rendering, clear vblank, sprite 0 and overflow
            if (scanLine == -1 && cycle == 1) {
                setVerticalBlank(false);
                // TODO add sprite 0 and overflow clearing
            }

            // Cycle 0: Nothing happens

            // Cycle 1 - 256: Visible cycles
            // When within visible scanline
            // Draw pixel
            // Initialise Background Registers (every 8 cycles)
            if (scanLine != -1 && cycle >= 1 && cycle <=256 && renderEnabled()) {
                drawPixel();
                evaluateBGRegisters();
            }

            // Cycle 256: Visible cycle
            // Increase Y position if rendering enabled
            if (cycle == 256 && renderEnabled()) {
                incFineY();
            }

            // Cycle 257:
            // If rendering enabled, copy all horizontal position bits
            // from ppu_t to ppu_v
            // v: ....F.. ...EDCBA = t: ....F.. ...EDCBA
            if (cycle == 257 && renderEnabled()) {
                // Get the value of horizontal name table
                int nt = (ppu_t.getValue() & 0x400);
                ppu_v.setHorizontalNameTable(nt);
                ppu_v.setCoarseX(ppu_t.getCoarseX());
            }

            // Cycle 280 to 304
            // Each tick in pre-render scanline, if rendering enabled then
            // copy all vertical position bits from ppu_t to ppu_v
            // v: IHGF.ED CBA..... = t: IHGF.ED CBA.....
            if (scanLine == -1 && cycle >= 280 && cycle <= 304 && renderEnabled()) {
                // Get Coarse Y value
                ppu_v.setCoarseY(ppu_t.getCoarseY());
                // Get the value of vertical name table
                ppu_v.setVerticalNameTable(ppu_t.getVerticalNameTable());
                // Get Fine Y value
                ppu_v.setFineY(ppu_t.getFineY());
            }

            // Cycle 321 to 336
            // Fetch first two tiles for the new forthcoming scanline
            // Initialise Background Registers (every 8 cycles)
            if (cycle >= 321 && cycle <= 340) {
                evaluateBGRegisters();
            }

            // Cycle 337 to 340
            // Fetch two unused Name Table Bytes
            // Nothing is implemented
            // Used for sync in some Mappers
            // TODO implement
        }

        // Scanline 240: Post-render scanline
        // Nothing happens

        // Scanline 241: Post-render scanline
        // Set Vblank flag in second tick (cycle 1)
        if (scanLine == 241) {
            if (cycle == 1) {
                // Indicate the end of frame
                complete = true;
                // Set Vblank flag
                setVerticalBlank(true);
                // If nmi flag is enabled in PPU_CTRL, enable nmi routine
                if (nmiEnabled()) {
                    setNmi(true);
                }
            }
        }

        // Scanline 242 to 260: Post-render scanline
        // Nothing happens

        // Advance tick
        cycle++;

        // When cycle overflows, go back
        if (cycle == 341) {
            cycle = 0;
            scanLine++;

            // When scanline overflows, go back to pre-render (-1)
            if (scanLine == 261) {
                scanLine = -1;
            }
        }
    }

    // TODO explain how register shifting is emulated by reading the position
    // depending on fine X value
    public void drawPixel() {
        // Retrieve the first most significant bit from the register
        // This is masked with fineX to further move the position wanted
        // as part of scrolling
        int lsByte = (currentNT & (0x8000 >> fineX)) == (0x8000 >> fineX)? 1:0;

        // Retrieve the second bit 8 bits after the first one
        int msByte = (currentNT & (0x80 >> fineX)) == (0x80 >> fineX)? 1: 0;

        int pixel = (msByte << 1) + lsByte;

        screen.setPixel(cycle - 1, scanLine, getPaletteColor(nextAT, pixel));


        // shift registers
        currentNT = (currentNT << 1) & 0xFFFF;
    }

    /**
     * Determines if render is enabled by checking if background rendering
     * or sprite rendering is enabled
     */
    public boolean renderEnabled() {
        return showBG() || showSprites();
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /**
     * Initialize Background registers depending on cycle. PPU needs two
     * cycles to fetch one byte. To initialise all registers, 8 cycles are
     * needed.
     * Cycle 1-2: Name Table byte. Used for fetch in cycle 5 and 7
     * Cycle 3-4: Attribute Table byte
     * Cycle 5-6: Low background tile byte.
     * Cycle 7-8: High background tile byte.
     * Only the first cycle is used to simulate the fetch.
     * Cycle 8 (i.e cycle 0): background registers are updated. The PPU has two
     * 16 bit background register that contains info of two tiles. When a pixel
     * is drawn, the register is shifted one bit. After eight shifts, the next
     * tile info will become current, and new bits will be stored every eight
     * cycles, which this function is emulating.
     */
    public void evaluateBGRegisters() {

        switch (cycle % 8) {
        // Cycle 8
        case 0:
            loadBGReg();
            // Increment to retrieve the next tile
            incCoarseX();
            break;

        // Cycle 1-2
        case 1:
            // TODO explain
            nextNTAddr = bus.ppuRead(0x2000 + ppu_v.getFullAddress());
            break;
        // Cycle 3-4
        case 3:
            // TODO explain
            // TODO change name into something descriptive
            nextATBits = getBaseNameTable() + 0x3C0
                    + ((ppu_v.getCoarseY() & 0x1C) << 1)
                    + ((ppu_v.getCoarseX() & 0x1C) >> 2);

            // Determine what is attribute bits (2 bits) that belongs to
            // the next tile fetched
            // Top left:    0-1
            // Top Right:   2-3
            // Bottom Left: 4-5
            // Bott Right:  6-7
            // This can be obtained by looking into CoarseX and CoarseY
            // as follows
            if (ppu_v.getCoarseX() % 4 < 2) {
                nextATBits >>= 2;
            }
            if (ppu_v.getCoarseY() % 4 < 2) {
                nextATBits >>= 4;
            }
            // The first two bits will now contain what is needed
            nextATBits &=0x3;
            break;
        // Cycle 5-6
        case 5:
            // TODO explain from
            //   https://wiki.nesdev.com/w/index.php/PPU_programmer_reference#Addressing
            lowBGByte = bus.ppuRead(getBGPatternTableAddr() + nextNTAddr * 16 + ppu_v.getFineY());
            break;
        // Cycle 7-8
        case 7:
            // TODO explain
            highBGByte = bus.ppuRead(getBGPatternTableAddr() + nextNTAddr * 16 + ppu_v.getFineY() + 8);
        }

    }

    /**
     * Update background render Registers with next tile bytes
     */
    public void loadBGReg() {

        // shift name table registers
        currentNT = nextNT;
        if(lowBGByte != 0 && highBGByte != 0 ) {
            System.out.println("Found it");
        }
        nextNT = lowBGByte << 8;
        nextNT += highBGByte;

        // Shift attribute table registers
        currentAT = nextAT;
        nextAT = nextATBits;
    }

    public int[][] getTile(int col, int row) {
        int[][] tile = new int[8][8];
        // Each tile is 16 bytes in size
        int offset = col * 16 + (row * 256);

        // TODO complete
        for (int y = 0; y < 8; y ++) {
            int lsByte = bus.ppuRead(getBGPatternTableAddr() + offset + y);
            int msByte = bus.ppuRead(getBGPatternTableAddr() + offset + y + 8);
            for(int x = 0; x < 8; x++) {
                int lsBit = (lsByte >> 7 - x) & 0x1;
                int msBit = (msByte >> 7 - x) & 0x1;
                int value = lsBit + msBit;
                tile[x][y] = value;
            }
        }

        return tile;
    }

    public Color getPaletteColor(int palette, int value) {
        int paletteAddr =  0x3F00 + (palette << 2) + value;
        int index = bus.ppuRead(paletteAddr);
        return colors.color[index];
    }

    public int[][] getPalette(int index) {
        int[][] tile = new int[128][128];

        for (int yTile = 0; yTile < 16; yTile++) {
            for(int xTile = 0; xTile < 16; xTile ++) {

                int offset = (yTile * 256) + (xTile * 16);

                for (int row = 0; row < 8; row ++) {
                    int lsb = bus.ppuRead(index * 0x1000 + offset + row);
                    int msb = bus.ppuRead(index * 0x1000 + offset + row + 8);
                    for(int col = 0; col < 8; col++) {
                        int lsBit = (lsb >> 7 - col) & 0x1;
                        int msBit = (msb >> 7 - col) & 0x1;
                        int value = lsBit + msBit;
                        tile[xTile * 8 + col][yTile * 8 + row] = value;
                    }
                }
            }

        }

        return tile;
    }
}
