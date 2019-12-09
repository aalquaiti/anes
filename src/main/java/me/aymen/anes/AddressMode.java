package me.aymen.anes;

/**
 * Reference to difference address mode methods used.
 * It does refer directly to address modes related to 6502. Although they are
 * related, this file is mostly to refer to variations in how to execute
 * these address modes inside code. E.g: 'pos' mode exist in reality, but no
 * mode is called 'posExtra'. The second is just 'pos' mode with extra clock
 * added.
 */
public interface AddressMode {
    /**
     * Indexed at one
     */
    int SIZE = 17;

    /**
     * Implied
     */
    int IMPL = 1;

    /**
     * Accumulator
     */
    int ACC = 2;

    /**
     * Immediate
     */
    int IMM = 3;

    /**
     * Zero Page
     */
    int ZPG = 4;

    /**
     * Zero page indexed with X
     */
    int ZPGX = 5;

    /**
     * Zero page indexed with Y
     */
    int ZPGY = 6;

    /**
     * Relative
     */
    int REL = 7;

    /**
     * Absolute
     */
    int ABS = 8;

    /**
     * Absolute indexed with X
     */
    int ABSX_P = 9;

    /**
     * Absolute indexed with X.
     * No cycle is incremented when cross page occurs.
     */
    int ABSX_O = 10;

    /**
     * Absolute indexed with Y
     */
    int ABSY_P = 11;

    /**
     * Absolute indexed with Y.
     * No cycle is incremented when cross page occurs.
     */
    int ABSY_O = 12;

    /**
     * Indirect
     */
    int IND = 13;

    /**
     * Indirect Y. Also known as  Indexed Indirect
     */
    int INDX = 14;

    /**
     * Indirect Y. Also known as  Indirect Indexed
     */
    int INDY_P = 15;

    /**
     * Indirect Y. Also known as  Indirect Indexed.
     * No cycle is incremented when cross page occurs. Used only with STA
     */
    int INDY_O = 16;
}
