package me.aymen.anes.anes;

/**
 * Processor Instructions. The suffix indicate the following addressing modes:
 * <p>PRE: Indirect pre-indexed with X</p>
 * <p>POS: Indirect post-indexed with Y</p>
 * <p>ZPG: Zero page</p>
 * <p>IMM: Immediate</p>
 * <p>ABS: Absolute</p>
 * <p>ZPGX: Zero page indexed with X</p>
 * <p>ABSX: Absolute indexed with X</p>
 * <p>ABSY: Absolute indexed with Y</p>
 */
public interface Inst {

    // ADC
    int ADC_PRE = 0x61;
    int ADC_ZPG = 0x65;
    int ADC_IMM = 0x69;
    int ADC_ABS = 0x6D;
    int ADC_POS = 0x71;
    int ADC_ZPGX = 0x75;
    int ADC_ABSY = 0x79;
    int ADC_ABSX = 0x7D;

    // AND
    int AND_PRE = 0x21;
    int AND_ZPG = 0x25;
    int AND_IMM = 0x29;
    int AND_ABS = 0x2D;
    int AND_POS = 0x31;
    int AND_ZPGX = 0x35;
    int AND_ABSY = 0x39;
    int AND_ABSX = 0x3D;

}
