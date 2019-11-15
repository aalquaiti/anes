package me.aymen.anes;

import me.aymen.anes.util.Function;

/**
 * Contains info related to each ops code instruction to be executed.
 * The suffix indicate the following addressing modes:
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
public class Inst {

    // ADC
    public static int ADC_PRE = 0x61;
    public static int ADC_ZPG = 0x65;
    public static int ADC_IMM = 0x69;
    public static int ADC_ABS = 0x6D;
    public static int ADC_POS = 0x71;
    public static int ADC_ZPGX = 0x75;
    public static int ADC_ABSY = 0x79;
    public static int ADC_ABSX = 0x7D;

    // AND
    public static int AND_PRE = 0x21;
    public static int AND_ZPG = 0x25;
    public static int AND_IMM = 0x29;
    public static int AND_ABS = 0x2D;
    public static int AND_POS = 0x31;
    public static int AND_ZPGX = 0x35;
    public static int AND_ABSY = 0x39;
    public static int AND_ABSX = 0x3D;

    // ASL
    public static int ASL_ZPG = 0x06;
    public static int ASL_ACC = 0x0A;
    public static int ASL_ABS = 0x0E;
    public static int ASL_ZPGX = 0x16;
    public static int ASL_ABSX = 0x1E;

    public static int BCC = 0x90;
    public static int BCS = 0xB0;
    public static int BEQ = 0xF0;

    // BIT
    public static int BIT_ZPG = 0X24;
    public static int BIT_ABS = 0x2C;

    public static int BMI = 0x30;
    public static int BNE = 0xD0;
    public static int BPL = 0x10;
    public static int BRK = 0x00;
    public static int BVC = 0x50;
    public static int BVS = 0x70;
    public static int CLC = 0x18;
    public static int CLD = 0xD8;
    public static int CLI = 0x58;
    public static int CLV = 0xB8;

    // CMP
    public static int CMP_PRE = 0xC1;
    public static int CMP_ZPG = 0xC5;
    public static int CMP_IMM = 0xC9;
    public static int CMP_ABS = 0xCD;
    public static int CMP_POS = 0xD1;
    public static int CMP_ZPGX = 0xD5;
    public static int CMP_ABSY = 0xD9;
    public static int CMP_ABSX = 0xDD;

    // CPX
    public static int CPX_IMM = 0xE0;
    public static int CPX_ZPG = 0xE4;
    public static int CPX_ABS = 0xEC;

    // CPY
    public static int CPY_IMM = 0xC0;
    public static int CPY_ZPG = 0xC4;
    public static int CPY_ABS = 0xCC;

    // DEC
    public static int DEC_ZPG = 0xC6;
    public static int DEC_ABS = 0xCE;
    public static int DEC_ZPGX = 0xD6;
    public static int DEC_ABSX = 0xDE;

    public static int DEX = 0xCA;
    public static int DEY = 0x88;

    // EOR
    public static int EOR_PRE = 0x41;
    public static int EOR_ZPG = 0x45;
    public static int EOR_IMM = 0x49;
    public static int EOR_ABS = 0x4D;
    public static int EOR_POS = 0x51;
    public static int EOR_ZPGX = 0x55;
    public static int EOR_ABSY = 0x59;
    public static int EOR_ABSX = 0x5D;

    // INC
    public static int INC_ZPG = 0xE6;
    public static int INC_ABS = 0xEE;
    public static int INC_ZPGX = 0xF6;
    public static int INC_ABSX = 0xFE;

    public static int INX = 0xE8;
    public static int INY = 0xC8;

    // JMP
    public static int JMP_ABS = 0x4C;
    public static int JMP_IND = 0x6C;

    public static int JSR = 0x20;

    // LDA
    public static int LDA_PRE = 0xA1;
    public static int LDA_ZPG = 0xA5;
    public static int LDA_IMM = 0xA9;
    public static int LDA_ABS = 0xAD;
    public static int LDA_POS = 0xB1;
    public static int LDA_ZPGX = 0xB5;
    public static int LDA_ABSY = 0xB9;
    public static int LDA_ABSX = 0xBD;

    // LDX
    public static int LDX_IMM = 0xA2;
    public static int LDX_ZPG = 0xA6;
    public static int LDX_ABS = 0xAE;
    public static int LDX_ZPGY = 0xB6;
    public static int LDX_ABSY = 0xBE;

    // LDY
    public static int LDY_IMM = 0xA0;
    public static int LDY_ZPG = 0xA4;
    public static int LDY_ABS = 0xAC;
    public static int LDY_ZPGX = 0xB4;
    public static int LDY_ABSX = 0xBC;

    // LSR
    public static int LSR_ZPG = 0x46;
    public static int LSR_ACC = 0x4A;
    public static int LSR_ABS = 0x4E;
    public static int LSR_ZPGX = 0x56;
    public static int LSR_ABSX = 0x5E;

    public static int NOP = 0xEA;

    // ORA
    public static int ORA_PRE = 0x01;
    public static int ORA_ZPG = 0x05;
    public static int ORA_IMM = 0x09;
    public static int ORA_ABS = 0x0D;
    public static int ORA_POS = 0x11;
    public static int ORA_ZPGX = 0x15;
    public static int ORA_ABSY = 0x19;
    public static int ORA_ABSX = 0x1D;

    public static int PHA = 0x48;
    public static int PHP = 0x08;
    public static int PLA = 0x68;
    public static int PLP = 0x28;

    // ROL
    public static int ROL_ZPG = 0x26;
    public static int ROL_ACC = 0x2A;
    public static int ROL_ABS = 0x2E;
    public static int ROL_ZPGX = 0x36;
    public static int ROL_ABSX = 0X3E;

    // ROR
    public static int ROR_ZPG = 0x66;
    public static int ROR_ACC = 0x6A;
    public static int ROR_ABS = 0x6E;
    public static int ROR_ZPGX = 0x76;
    public static int ROR_ABSX = 0X7E;

    public static int RTI = 0x40;
    public static int RTS = 0x60;

    // SBC
    public static int SBC_PRE = 0xE1;
    public static int SBC_ZPG = 0xE5;
    public static int SBC_IMM = 0xE9;
    public static int SBC_ABS = 0xED;
    public static int SBC_POS = 0xF1;
    public static int SBC_ZPGX = 0xF5;
    public static int SBC_ABSY = 0xF9;
    public static int SBC_ABSX = 0xFD;

    public static int SEC = 0x38;
    public static int SED = 0xF8;
    public static int SEI = 0x78;

    // STA
    public static int STA_PRE = 0x81;
    public static int STA_ZPG = 0x85;
    public static int STA_ABS = 0x8D;
    public static int STA_POS = 0x91;
    public static int STA_ZPGX = 0x95;
    public static int STA_ABSY = 0x99;
    public static int STA_ABSX = 0x9D;

    // STX
    public static int STX_ZPG = 0x86;
    public static int STX_ABS = 0x8E;
    public static int STX_ZPGY = 0x96;

    // STY
    public static int STY_ZPG = 0x84;
    public static int STY_ABS = 0x8C;
    public static int STY_ZPGX = 0x94;

    public static int TAX = 0xAA;
    public static int TAY = 0xA8;
    public static int TSX = 0xBA;
    public static int TXA = 0X8A;
    public static int TXS = 0x9A;
    public static int TYA = 0x98;


    /**
     * Operation Code
     */
    public String name;

    /**
     * Number of cycles an operation consume
     */
    public int cycles;

    /**
     * Address mode of the operation
     */
    public int mode;

    /**
     * Operation code to perform
     */
    public Function operation;

    public Inst(String name, int cycles, int mode, Function operation) {
        this.name = name;
        this.cycles = cycles;
        this.mode = mode;
        this.operation = operation;
    }
}
