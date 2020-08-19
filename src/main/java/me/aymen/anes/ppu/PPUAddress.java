package me.aymen.anes.ppu;

/**
 * Represents a PPU internal address register in the way loopy, an NES emulator
 * developer explained the internals of NES PPU.
 */
public class PPUAddress {

    /**
     * 5 bits mask
     */
    public static final int MASK_COARSE = 0x1F;
    /**
     * 1 bit mask
     */
    public static final int MASK_NT = 0x1;
    /**
     * 3 bits mask
     */
    public static final int MASK_FINE = 0x7;

    /**
     * 12 bits mask
     */
    public static final int MASK_FULL_ADDR = 0xFFF;

    public static final int SHIFT_COARSE_Y = 5;
    public static final int SHIFT_H_NT = 10;
    public static final int SHIFT_V_NT = 11;
    public static final int SHIFT_FINE_Y = 12;

    /**
     * PPU Address consists of 15 bits as following:
     * 0-4:     Coarse X
     * 5-9:     Coarse Y
     * 10:      Horizontal name table
     * 11:      Vertical name table
     * 12-14:   Fine Y
     */
    private int coarseX;
    private int coarseY;
    private int hNT;
    private int vNT;
    private int fineY;

    public PPUAddress() {

    }

    public int getValue() {
        return getFineY() << SHIFT_FINE_Y | getFullAddress();
    }

    /**
     * Set value for whole address register. Only first 15 bits from provided
     * value are read
     */
    public void setValue(int value) {
        setFullAddress(value);
        setFineY(value >> SHIFT_FINE_Y);
    }

    /**
     * Retrieve the first 12 bits that forms full address of tile
     */
    public int getFullAddress() {
        return vNT << SHIFT_V_NT | hNT << SHIFT_H_NT |
                coarseY << SHIFT_COARSE_Y | coarseX;
    }

    /**
     * Set full of the tile Only first 12 bits from provided value are read
     */
    public void setFullAddress(int value) {
        setCoarseX(value);
        setCoarseY(value >> SHIFT_COARSE_Y);
        setHorizontalNameTable(value >> SHIFT_H_NT);
        setVerticalNameTable(value >> SHIFT_V_NT);
    }

    public int getCoarseX() {
        return coarseX;
    }

    /**
     * Accepts first five bits of provided value
     */
    public void setCoarseX(int value) {
        coarseX = value & MASK_COARSE;
    }

    public int getCoarseY() {
        return coarseY;
    }

    /**
     * Accepts first five bits of provided value
     */
    public void setCoarseY(int value) {
        coarseY = value & MASK_COARSE;
    }

    public int getHorizontalNameTable() {
        return hNT;
    }

    /**
     * Accepts first bit of provided value
     */
    public void setHorizontalNameTable(int value) {
        hNT = value & MASK_NT;
    }

    /**
     * Flips the value from 0 to 1, or 1 to 0
     */
    public void flipHorizontalNameTable() {
        hNT = ~hNT & MASK_NT;
    }

    public int getVerticalNameTable() {
        return vNT;
    }

    /**
     * Accepts first five bits of provided value
     */
    public void setVerticalNameTable(int value) {
        vNT = value & MASK_NT;
    }

    /**
     * Flips the value from 0 to 1, or 1 to 0
     */
    public void flipVerticalNameTable() {
        vNT = ~vNT & MASK_NT;
    }

    public int getNameTable() {
        return (vNT << 1) + hNT;
    }

    /**
     * Set both horizontal and vertical name tables.
     * Accepts first two bits of provided value
     */
    public void setNameTable(int value) {
        value &= 0x3;
        setHorizontalNameTable(value & MASK_NT);
        setVerticalNameTable(value >> 1);
    }

    /**
     * Accepts first five bits of provided value
     */
    public int getFineY() {
        return fineY;
    }

    /**
     * Accepts first three bits of provided value
     */
    public void setFineY(int value) {
        fineY = value & MASK_FINE;
    }

    /**
     * Increment register by provided value
     */
    public void increment(int value) {
        int newVal = getValue() + value;
        setValue(newVal);
    }

    /**
     * Takes the first 8 bit and set internal values as follows
     * 0-4: Coarse X
     * 5-7: least significant 3 bits of Coarse Y
     */
    public void setFirstHalf(int value) {
        coarseX = value & MASK_COARSE;
        // Erase first three bits
        int newCoarseY = coarseY & ~0x7;
        // Add bits 5 to 7
        newCoarseY |= (value & 0xE0) >> 5;

        coarseY = newCoarseY;
    }

    /**
     * Takes the first 7 bits and set internal values as follows:
     * 0-1: most significant 2 bits of Coarse Y
     * 2-3: Name Table
     * 4-6: Fine Y
     */
    public void setSecondHalf(int value) {
        coarseY = (value & 0x3) << 3 | (coarseY & 0x7);
        setNameTable(value >> 2);
        setFineY(value >> 4);
    }
}
