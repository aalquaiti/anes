package me.aymen.anes;

/**
 * Processor Instructions. The suffix indicate the following addressing modes:
 * <p>PRE: Indirect pre-indexed with X</p>
 * <p>POS: Indirect post-indexed with Y</p>
 * <p>ZPG: Zero page</p>
 * <p>ACC: Accumulator</p>
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

    // ASL
    int ASL_ZPG = 0x06;
    int ASL_ACC = 0x0A;
    int ASL_ABS = 0x0E;
    int ASL_ZPGX = 0x16;
    int ASL_ABSX = 0x1E;

    int BCC = 0x90;
    int BCS = 0xB0;
    int BEQ = 0xF0;

    // BIT
    int BIT_ZPG = 0X24;
    int BIT_ABS = 0x2C;

    int BMI = 0x30;
    int BNE = 0xD0;
    int BPL = 0x10;
    int BRK = 0x00;
    int BVC = 0x50;
    int BVS = 0x70;
    int CLC = 0x18;
    int CLD = 0xD8;
    int CLI = 0x58;
    int CLV = 0xB8;

    // CMP
    int CMP_PRE = 0xC1;
    int CMP_ZPG = 0xC5;
    int CMP_IMM = 0xC9;
    int CMP_ABS = 0xCD;
    int CMP_POS = 0xD1;
    int CMP_ZPGX = 0xD5;
    int CMP_ABSY = 0xD9;
    int CMP_ABSX = 0xDD;

    // CPX
    int CPX_IMM = 0xE0;
    int CPX_ZPG = 0xE4;
    int CPX_ABS = 0xEC;

    // CPY
    int CPY_IMM = 0xC0;
    int CPY_ZPG = 0xC4;
    int CPY_ABS = 0xCC;

    // DEC
    int DEC_ZPG = 0xC6;
    int DEC_ABS = 0xCE;
    int DEC_ZPGX = 0xD6;
    int DEC_ABSX = 0xDE;

    int DEX = 0xCA;
    int DEY = 0x88;

    // EOR
    int EOR_PRE = 0x41;
    int EOR_ZPG = 0x45;
    int EOR_IMM = 0x49;
    int EOR_ABS = 0x4D;
    int EOR_POS = 0x51;
    int EOR_ZPGX = 0x55;
    int EOR_ABSY = 0x59;
    int EOR_ABSX = 0x5D;
}
