package me.aymen.anes;

/**
 * Processor Instructions. The suffix indicate the following addressing modes:
 * <p>PRE: Indirect pre-indexed with X</p>
 * <p>POS: Indirect post-indexed with Y</p>
 * <p>ZPG: Zero page</p>
 * <p>ACC: Accumulator</p>
 * <p>IMM: Immediate</p>
 * <p>ABS: Absolute (Direct)</p>
 * <p>IND: Indirect</p>
 * <p>ZPGX: Zero page indexed with X</p>
 * <p>ZPGY: Zero page indexed with Y</p>
 * <p>ABSX: Absolute indexed with X</p>
 * <p>ABSY: Absolute indexed with Y</p>
 */
public interface Inst4 {

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

    // INC
    int INC_ZPG = 0xE6;
    int INC_ABS = 0xEE;
    int INC_ZPGX = 0xF6;
    int INC_ABSX = 0xFE;

    int INX = 0xE8;
    int INY = 0xC8;

    // JMP
    int JMP_ABS = 0x4C;
    int JMP_IND = 0x6C;

    int JSR = 0x20;

    // LDA
    int LDA_PRE = 0xA1;
    int LDA_ZPG = 0xA5;
    int LDA_IMM = 0xA9;
    int LDA_ABS = 0xAD;
    int LDA_POS = 0xB1;
    int LDA_ZPGX = 0xB5;
    int LDA_ABSY = 0xB9;
    int LDA_ABSX = 0xBD;

    // LDX
    int LDX_IMM = 0xA2;
    int LDX_ZPG = 0xA6;
    int LDX_ABS = 0xAE;
    int LDX_ZPGY = 0xB6;
    int LDX_ABSY = 0xBE;

    // LDY
    int LDY_IMM = 0xA0;
    int LDY_ZPG = 0xA4;
    int LDY_ABS = 0xAC;
    int LDY_ZPGX = 0xB4;
    int LDY_ABSX = 0xBC;

    // LSR
    int LSR_ZPG = 0x46;
    int LSR_ACC = 0x4A;
    int LSR_ABS = 0x4E;
    int LSR_ZPGX = 0x56;
    int LSR_ABSX = 0x5E;

    int NOP = 0xEA;

    // ORA
    int ORA_PRE = 0x01;
    int ORA_ZPG = 0x05;
    int ORA_IMM = 0x09;
    int ORA_ABS = 0x0D;
    int ORA_POS = 0x11;
    int ORA_ZPGX = 0x15;
    int ORA_ABSY = 0x19;
    int ORA_ABSX = 0x1D;

    int PHA = 0x48;
    int PHP = 0x08;
    int PLA = 0x68;
    int PLP = 0x28;

    // ROL
    int ROL_ZPG = 0x26;
    int ROL_ACC = 0x2A;
    int ROL_ABS = 0x2E;
    int ROL_ZPGX = 0x36;
    int ROL_ABSX = 0X3E;

    // ROR
    int ROR_ZPG = 0x66;
    int ROR_ACC = 0x6A;
    int ROR_ABS = 0x6E;
    int ROR_ZPGX = 0x76;
    int ROR_ABSX = 0X7E;

    int RTI = 0x40;
    int RTS = 0x60;

    // SBC
    int SBC_PRE = 0xE1;
    int SBC_ZPG = 0xE5;
    int SBC_IMM = 0xE9;
    int SBC_ABS = 0xED;
    int SBC_POS = 0xF1;
    int SBC_ZPGX = 0xF5;
    int SBC_ABSY = 0xF9;
    int SBC_ABSX = 0xFD;


    int SEC = 0x38;
    int SED = 0xF8;
    int SEI = 0x78;

    // STA
    int STA_PRE = 0x81;
    int STA_ZPG = 0x85;
    int STA_ABS = 0x8D;
    int STA_POS = 0x91;
    int STA_ZPGX = 0x95;
    int STA_ABSY = 0x99;
    int STA_ABSX = 0x9D;

    // STX
    int STX_ZPG = 0x86;
    int STX_ABS = 0x8E;
    int STX_ZPGY = 0x96;

    // STY
    int STY_ZPG = 0x84;
    int STY_ABS = 0x8C;
    int STY_ZPGX = 0x94;

    int TAX = 0xAA;
    int TAY = 0xA8;
    int TSX = 0xBA;
    int TXA = 0X8A;
    int TXS = 0x9A;
    int TYA = 0x98;
}
