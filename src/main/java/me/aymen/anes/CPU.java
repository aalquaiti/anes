package me.aymen.anes;

import me.aymen.anes.memory.Bus;
import me.aymen.anes.util.Function;
import static me.aymen.anes.AddressMode.*;

public class CPU {

    // CPU Components
    private Bus bus;
    private int cycles;

    // Registers
    private int A;          // Accumulator
    private int X;          // X Index
    private int Y;          // Y Index
    private int PC;         // Program Counter
    private int SP;         // Stack Pointer
    private Flags P;        // Process Status

    // Others
    private int op1;        // First Operand
    private int op2;        // Second Operand
    // Used by executing instruction if needed
    // populated by appropriate addressing mode
    // Basically equals to op1 | (op2 << 8)
    private int address;
    // The value at memory address
    // populated by addressing mode if needed
    private int value;

    // All 6502 OPCodes. Unsupported unofficial instructions are
    // set to null, which will lead to crashing
    private Inst[] opcodes = new Inst[256];

    private Function[] addressMode = new Function[AddressMode.SIZE];

    // All Address modes

    public CPU(Bus bus) {
        this.bus = bus;
        reset();

        // populates addressModes
        addressMode[IMPL] =  () -> {};
        addressMode[ACC] = this::acc;
        addressMode[IMM] = this::imm;
        addressMode[ZPG] = this::zpg;
        addressMode[ZPGX] = this::zpgx;
//        addressMode[ZPGY] = this::zpgy;
        addressMode[REL] = this::rel;
        addressMode[ABS] = this::abs;
        addressMode[ABSX] = this::absx;
        addressMode[ABSX_PLUS] = this::absxPlus;
        addressMode[ABSY] = this::absy;
        addressMode[ABSY_PLUS] = this::absyPlus;
        addressMode[IND] = this::ind;
        addressMode[INDX] = this::indx;
        addressMode[INDY] = this::indy;
        addressMode[INDY_PLUS ] = this::indyPlus;

        // 0x0#
        opcodes[0x00] = new Inst("BRK", 7, IMPL, this::brk);
        opcodes[0x01] = new Inst("ORA", 6, INDX, this::ora);
        opcodes[0x02] = null;
        opcodes[0x03] = null;
        opcodes[0x04] = null;
        opcodes[0x05] = new Inst("ORA", 3, ZPG, this::ora);
        opcodes[0x06] = new Inst("ASL", 5, ZPG, this::aslM);
        opcodes[0x07] = null;
        opcodes[0x08] = new Inst("PHP", 3, IMPL, this::php);
        opcodes[0x09] = new Inst("ORA", 2, IMM, this::ora);
        opcodes[0x0A] = new Inst("ASL", 2, ACC, this::aslA);
        opcodes[0x0B] = null;
        opcodes[0x0C] = null;
        opcodes[0x0D] = new Inst("ORA", 4, ABS, this::ora);
        opcodes[0x0E] = new Inst("ASL", 6, ABS, this::aslM);
        opcodes[0x0F] = null;

        // 0x1#
        opcodes[0x10] = new Inst("BPL", 2, REL, this::bpl);
        opcodes[0x11] = new Inst("ORA", 5, INDY, this::ora);
        opcodes[0x12] = null;
        opcodes[0x13] = null;
        opcodes[0x14] = null;
        opcodes[0x15] = new Inst("ORA", 5, ZPGX, this::ora);
        opcodes[0x16] = new Inst("ASL", 6, ZPGX, this::aslM);
        opcodes[0x17] = null;
        opcodes[0x18] = new Inst("CLC", 2, IMPL, this::clc);
        opcodes[0x19] = new Inst("ORA", 4, ABSY, this::ora);
        opcodes[0x1A] = null;
        opcodes[0x1B] = null;
        opcodes[0x1C] = null;
        opcodes[0x1D] = new Inst("ORA", 4, ABSX, this::ora);
        opcodes[0x1E] = new Inst("ASL", 7, ABSX_PLUS, this::aslM);
        opcodes[0x1F] = null;

        // 0x2#
        opcodes[0x20] = new Inst("JSR", 6, ABS, this::jsr);
        opcodes[0x21] = new Inst("AND", 6, INDX, this::and);
        opcodes[0x22] = null;
        opcodes[0x23] = null;
        opcodes[0x24] = new Inst("BIT", 3, ZPG, this::bit);
        opcodes[0x25] = new Inst("AND", 3, ZPG, this::and);
        opcodes[0x26] = new Inst("ROL", 5, ZPG, this::rolM);
        opcodes[0x27] = null;
        opcodes[0x28] = new Inst("PLP", 4, IMPL, this::plp);
        opcodes[0x29] = new Inst("AND", 2, IMM, this::and);
        opcodes[0x2A] = new Inst("ROL", 2, ACC, this::rolA);
        opcodes[0x2B] = null;
        opcodes[0x2C] = new Inst("BIT", 4, ABS, this::bit);
        opcodes[0x2D] = new Inst("AND", 4, ABS, this::and);
        opcodes[0x2E] = new Inst("ROL", 6, ABS, this::rolM);
        opcodes[0x2F] = null;

        // 0x3#
        opcodes[0x30] = new Inst("BMI", 2, REL, this::bmi);
        opcodes[0x31] = new Inst("AND", 5, INDY, this::and);
        opcodes[0x32] = null;
        opcodes[0x33] = null;
        opcodes[0x34] = null;
        opcodes[0x35] = new Inst("AND", 4, ZPGX, this::and);
        opcodes[0x36] = new Inst("ROL", 6, ZPGX, this::rolM);
        opcodes[0x37] = null;
        opcodes[0x38] = new Inst("SEC", 2, IMPL, this::sec);
        opcodes[0x39] = new Inst("AND", 4, ABSY, this::and);
        opcodes[0x3A] = null;
        opcodes[0x3B] = null;
        opcodes[0x3C] = null;
        opcodes[0x3D] = new Inst("AND", 4, ABSX, this::and);
        opcodes[0x3E] = new Inst("ROL", 7, ABSX_PLUS, this::rolM);
        opcodes[0x3F] = null;

        // 0x4#
        opcodes[0x40] = new Inst("RTI", 6, IMPL, this::rti);
        opcodes[0x41] = new Inst("EOR", 6, INDX, this::eor);
        opcodes[0x42] = null;
        opcodes[0x43] = null;
        opcodes[0x44] = null;
        opcodes[0x45] = new Inst("EOR", 3, ZPG, this::eor);
        opcodes[0x46] = new Inst("LSR", 5, ZPG, this::lsrM);
        opcodes[0x47] = null;
        opcodes[0x48] = new Inst("PHA", 3, IMPL, this::pha);
        opcodes[0x49] = new Inst("EOR", 2, IMM, this::eor);
        opcodes[0x4A] = new Inst("LSR", 2, ACC, this::lsrA);
        opcodes[0x4B] = null;
        opcodes[0x4C] = new Inst("JMP", 3, ABS, this::jmp);
        opcodes[0x4D] = new Inst("EOR", 4, ABS, this::eor);
        opcodes[0x4E] = new Inst("LSR", 6, ABS, this::lsrM);
        opcodes[0x4F] = null;

        // 0x5#
        opcodes[0x50] = new Inst("BVC", 2, REL, this::bvc);
        opcodes[0x51] = new Inst("EOR", 5, INDY, this::eor);
        opcodes[0x52] = null;
        opcodes[0x53] = null;
        opcodes[0x54] = null;
        opcodes[0x55] = new Inst("EOR", 4, ZPGX, this::eor);
        opcodes[0x56] = new Inst("LSR", 6, ZPGX, this::lsrM);
        opcodes[0x57] = null;
        opcodes[0x58] = new Inst("CLI", 2, IMPL, this::cli);
        opcodes[0x59] = new Inst("EOR", 4, ABSY, this::eor);
        opcodes[0x5A] = null;
        opcodes[0x5B] = null;
        opcodes[0x5C] = null;
        opcodes[0x5D] = new Inst("EOR", 4, ABSY, this::eor);
        opcodes[0x5E] = new Inst("LSR", 7, ABSX_PLUS, this::lsrM);
        opcodes[0x5F] = null;

        // 0x6#
        opcodes[0x60] = new Inst("RTS", 6, IMPL, this::rts);
        opcodes[0x61] = new Inst("ADC", 6, INDX, this::adc);
        opcodes[0x62] = null;
        opcodes[0x63] = null;
        opcodes[0x64] = null;
        opcodes[0x65] = new Inst("ADC", 3, ZPG, this::adc);
        opcodes[0x66] = new Inst("ROR", 5, ZPG, this::rorM);
        opcodes[0x67] = null;
        opcodes[0x68] = new Inst("PLA", 4, IMPL, this::pla);
        opcodes[0x69] = new Inst("ADC", 2, IMM, this::adc);
        opcodes[0x6A] = new Inst("ROR", 2, ACC, this::rorA);
        opcodes[0x6B] = null;
        opcodes[0x6C] = new Inst("JMP", 5, IND, this::jmp);
        opcodes[0x6D] = new Inst("ADC", 4, ABS, this::adc);
        opcodes[0x6E] = new Inst("ROR", 6, ABS, this::rorM);
        opcodes[0x6F] = null;

        // 0x7#
        opcodes[0x70] = new Inst("BVS", 2, REL, this::bvs);
        opcodes[0x71] = new Inst("ADC", 5, INDY, this::adc);
        opcodes[0x72] = null;
        opcodes[0x73] = null;
        opcodes[0x74] = null;
        opcodes[0x75] = new Inst("ADC", 4, ZPGX, this::adc);
        opcodes[0x76] = new Inst("ROR", 6, ZPGX, this::rorM);
        opcodes[0x77] = null;
        opcodes[0x78] = new Inst("SEI", 2, IMPL, this::sei);
        opcodes[0x79] = new Inst("ADC", 4, ABSY, this::adc);
        opcodes[0x7A] = null;
        opcodes[0x7B] = null;
        opcodes[0x7C] = null;
        opcodes[0x7D] = new Inst("ADC", 4, ABSX, this::adc);
        opcodes[0x7E] = new Inst("ROR", 7, ABSX_PLUS, this::rorM);
        opcodes[0x7F] = null;

        // 0x8#
        opcodes[0x80] = null;
        opcodes[0x81] = new Inst("STA", 6, INDX, this::sta);
        opcodes[0x82] = null;
        opcodes[0x83] = null;
        opcodes[0x84] = new Inst("STY", 3, ZPG, this::sty);
        opcodes[0x85] = new Inst("STA", 3, ZPG, this::sty);
        opcodes[0x86] = new Inst("STX", 3, ZPG, this::stx);
        opcodes[0x87] = null;
        opcodes[0x88] = new Inst("DEY", 2, IMPL, this::dey);
        opcodes[0x89] = null;
        opcodes[0x8A] = new Inst("TXA", 2, IMPL, this::txa);
        opcodes[0x8B] = null;
        opcodes[0x8C] = new Inst("STY", 4, ABS, this::sty);
        opcodes[0x8D] = new Inst("STA", 4, ABS, this::sta);
        opcodes[0x8E] = new Inst("STX", 4, ABS, this::stx);
        opcodes[0x8F] = null;

        // 0x9#
        opcodes[0x90] = new Inst("BCC", 2, REL, this::bcc);
        opcodes[0x91] = new Inst("STA", 6, INDY, this::sta);
        opcodes[0x92] = null;
        opcodes[0x93] = null;
        opcodes[0x94] = new Inst("STY", 4, ZPGX, this::sty);
        opcodes[0x95] = new Inst("STA", 4, ZPGX, this::sta);
        opcodes[0x96] = new Inst("STX", 4, ZPGY, this::stx);
        opcodes[0x97] = null;
        opcodes[0x98] = new Inst("TYA", 2, IMPL, this::tya);
        opcodes[0x99] = new Inst("STA", 5, ABSY_PLUS, this::sta);
        opcodes[0x9A] = new Inst("TXS", 2, IMPL, this::txs);
        opcodes[0x9B] = null;
        opcodes[0x9C] = null;
        opcodes[0x9D] = new Inst("STA", 5, ABSX_PLUS, this::sta);
        opcodes[0x9E] = null;
        opcodes[0x9F] = null;

        // 0xA#
        opcodes[0xA0] = new Inst("LDY", 2, IMM, this::ldy);
        opcodes[0xA1] = new Inst("LDA", 6, INDX, this::lda);
        opcodes[0xA2] = new Inst("LDX", 2, IMM, this::ldx);
        opcodes[0xA3] = null;
        opcodes[0xA4] = new Inst("LDY", 3, ZPG, this::ldy);
        opcodes[0xA5] = new Inst("LDA", 3, ZPG, this::lda);
        opcodes[0xA6] = new Inst("LDX", 3, ZPG, this::ldx);
        opcodes[0xA7] = null;
        opcodes[0xA8] = new Inst("TAY", 2, IMPL, this::tay);
        opcodes[0xA9] = new Inst("LDA", 2, IMM, this::lda);
        opcodes[0xAA] = new Inst("TAX", 2, IMPL, this::tax);
        opcodes[0xAB] = null;
        opcodes[0xAC] = new Inst("LDY", 4, ABS, this::ldy);
        opcodes[0xAD] = new Inst("LDA", 4, ABS, this::lda);
        opcodes[0xAE] = new Inst("LDX", 4, ABS, this::ldx);
        opcodes[0xAF] = null;

        // 0xB#
        opcodes[0xB0] = new Inst("BCS", 2, REL, this::bcs);
        opcodes[0xB1] = new Inst("LDA", 5, INDY, this::lda);
        opcodes[0xB2] = null;
        opcodes[0xB3] = null;
        opcodes[0xB4] = new Inst("LDY", 4, ZPGX, this::ldy);
        opcodes[0xB5] = new Inst("LDA", 4, ZPGX, this::lda);
        opcodes[0xB6] = new Inst("LDX", 4, ZPGY, this::ldx);
        opcodes[0xB7] = null;
        opcodes[0xB8] = new Inst("CLV", 2, IMPL, this::clv);
        opcodes[0xB9] = new Inst("LDA", 4, ABSY, this::lda);
        opcodes[0xBA] = new Inst("TSX", 2, IMPL, this::tsx);
        opcodes[0xBB] = null;
        opcodes[0xBC] = new Inst("LDY", 4, ABSX, this::ldy);
        opcodes[0xBD] = new Inst("LDA", 4, ABSX, this::lda);
        opcodes[0xBE] = new Inst("LDX", 4, ABSY, this::ldx);
        opcodes[0xBF] = null;

        // 0xC#
        opcodes[0xC0] = new Inst("CPY", 2, IMM, this::cpy);
        opcodes[0xC1] = new Inst("CMP", 6, INDX, this::cmp);
        opcodes[0xC2] = null;
        opcodes[0xC3] = null;
        opcodes[0xC4] = new Inst("CPY", 3, ZPG, this::cpy);
        opcodes[0xC5] = new Inst("CMP", 3, ZPG, this::cmp);
        opcodes[0xC6] = new Inst("DEC", 5, ZPG, this::dec);
        opcodes[0xC7] = null;
        opcodes[0xC8] = new Inst("INY", 2, IMPL, this::iny);
        opcodes[0xC9] = new Inst("CMP", 2, IMM, this::cmp);
        opcodes[0xCA] = new Inst("DEX", 2, IMPL, this::dex);
        opcodes[0xCB] = null;
        opcodes[0xCC] = new Inst("CPY", 4, ABS, this::cpy);
        opcodes[0xCD] = new Inst("CMP", 4, ABS, this::cmp);
        opcodes[0xCE] = new Inst("DEC", 6, ABS, this::dec);
        opcodes[0xCF] = null;
    }

    /**
     * Execute instruction at current program counter
     */
    /*public void execute() {
        int address;
        int instruction = bus.read(PC++);

        switch (instruction) {


            case Inst4.`:
                cycles+=2;
                branch(!P.C);
                break;

            case Inst4.BEQ:
                cycles+=2;
                branch(P.Z);
                break;

            case Inst4.BIT_ABS:
                cycles+=3;
                bit(abs());
                break;
            case Inst4.BNE:
                cycles+=2;
                branch(!P.Z);
                break;
            case Inst4.BVC:
                cycles+=2;
                branch(!P.V);
                break;
            case Inst4.BVS:
                cycles+=2;
                branch(P.V);
                break;
            case Inst4.CLD:
                cycles+=2;
                P.D = false;
                break;
            case Inst4.CLI:
                cycles+=2;
                P.I = false;
                break;`

            // CMP
            case Inst4.CMP_PRE:
                cycles+=6;
                compare(A, indx());
                break;
            case Inst4.CMP_ZPG:
                cycles+=3;
                compare(A, zpg());
                break;
            case Inst4.CMP_IMM:
                cycles+=2;
                compare(A, imm());
                break;
            case Inst4.CMP_ABS:
                cycles+=4;
                compare(A, abs());
                break;
            case Inst4.CMP_POS:
                cycles+=5;
                compare(A, pos(true));
                break;
            case Inst4.CMP_ZPGX:
                cycles+=4;
                compare(A, zpgIndx(X));
                break;
            case Inst4.CMP_ABSY:
                cycles+=4;
                compare(A, indx(Y, true));
                break;
            case Inst4.CMP_ABSX:
                cycles+=4;
                compare(A, indx(X, true));
                break;

            // CPX
            case Inst4.CPX_IMM:
                cycles+=2;
                compare(X, imm());
                break;
            case Inst4.CPX_ZPG:
                cycles+=3;
                compare(X, zpg());
                break;
            case Inst4.CPX_ABS:
                cycles+=4;
                compare(X, abs());
                break;

            // CPY
            case Inst4.CPY_IMM:
                cycles+=2;
                compare(Y, imm());
                break;
            case Inst4.CPY_ZPG:
                cycles+=3;
                compare(Y, zpg());
                break;
            case Inst4.CPY_ABS:
                cycles+=4;
                compare(Y, abs());
                break;

            // DEC
            case Inst4.DEC_ZPG:
                cycles+=5;
                inc(0xFF, zpg());
                break;
            case Inst4.DEC_ABS:
                cycles+=6;
                inc(0xFF, abs());
                break;
            case Inst4.DEC_ZPGX:
                cycles+=6;
                inc(0xFF, zpg());
                break;
            case Inst4.DEC_ABSX:
                cycles+=7;
                inc(0xFF, indx(X, false));
                break;

            case Inst4.DEX:
                cycles+=2;
                inx(0xFF);
                break;

            // INC
            case Inst4.INC_ZPG:
                cycles+=5;
                inc(0x01, zpg());
                break;
            case Inst4.INC_ABS:
                cycles+=6;
                inc(0x01, abs());
                break;
            case Inst4.INC_ZPGX:
                cycles+=6;
                inc(0x01, zpg());
                break;
            case Inst4.INC_ABSX:
                cycles+=7;
                inc(0x01, indx(X, false));
                break;

            case Inst4.INX:
                cycles+=2;
                inx(0x01);
                break;
            case Inst4.INY:
                cycles+=2;
                iny(0x01);
                break;

            // JMP
            case Inst4.JMP_IND:
                cycles+=5;
                jmp(ind());
                break;

            case Inst4.NOP:
                cycles+=2;
                break;

            case Inst4.PLA:
                cycles+=4;
                A = pl();
                P.setZSFlags(A);
                break;
            case Inst4.PLP:
                cycles+=4;
                P.setStatus(pl());
                break;

            case Inst4.RTI:
                cycles+=6;
                rti();
                break;

            // SBC
            case Inst4.SBC_PRE:
                cycles+=6;
                sbc(indx());
                break;
            case Inst4.SBC_ZPG:
                cycles+=3;
                sbc(zpg());
                break;
            case Inst4.SBC_IMM:
                cycles+=2;
                sbc(imm());
                break;
            case Inst4.SBC_ABS:
                cycles+=4;
                sbc(abs());
                break;
            case Inst4.SBC_POS:
                cycles+=5;
                sbc(pos(true));
                break;
            case Inst4.SBC_ZPGX:
                cycles+=4;
                sbc(zpgIndx(X));
                break;
            case Inst4.SBC_ABSY:
                cycles+=4;
                sbc(indx(Y, true));
                break;
            case Inst4.SBC_ABSX:
                cycles+=4;
                sbc(indx(X, true));
                break;

            case Inst4.SED:
                // Decimal mode is disabled in NES
                // ADC and SBC instructions will not be affected as
                // decimal mode is not supported
                cycles+=2;
                P.D = true;
                break;
            case Inst4.SEI:
                cycles+=2;
                P.I = true;
                break;


            case Inst4.TAX:
                cycles+=2;
                X = A;
                P.setZSFlags(X);
                break;
            case Inst4.TAY:
                cycles+=2;
                Y = A;
                P.setZSFlags(Y);
                break;
            case Inst4.TSX:
                cycles+=2;
                X = bus.read(SP);
                P.setZSFlags(X);
                break;
            case Inst4.TXA:
                cycles+=2;
                A = X;
                P.setZSFlags(A);
                break;
            case Inst4.TXS:
                cycles+=2;
                bus.write(X, SP);
                break;
            case Inst4.TYA:
                cycles+=2;
                A = Y;
                P.setZSFlags(A);
                break;

            default:
                throw new RuntimeException("Unrecognized instruction");
        }
    } */

    public String tick() {

        // Read the current PC then increment it
        int currentPC = incPC();

        // Retrieve the operation mnemonic
        int op = bus.read(currentPC);

        /**
         * Retrieve the relavent opcode then:
         * 1. Invoke the address mode then update the following variables
         * if need be:
         *      a. op1
         *      b. op2
         *      c. address
         *      d. value
         *  2. Invoke opcode
         *  3. Update cycles count
         */
        Inst opcode = opcodes[op];
        addressMode[opcode.mode].process();
        opcode.operation.process();
        cycles += opcode.cycles;

        return Deassembler.analyse(currentPC, opcode, op1, op2);
    }

//    public void start() {
//        reset();
//
//        while(true) {
//            // TODO - Implement
//            // increment cycles
//            // execute OP_CODE code
//
//            execute();
//        }
//    }

    /**
     * Reset the CPU to an initial state
     */
    public void reset() {
        cycles = 0;

        // TODO there is difference between power-up and reset status
        // Change it according to
        // http://wiki.nesdev.com/w/index.php/CPU_power_up_state
        A = 0;
        X = 0;
        Y = 0;
        SP = 0x01FF;
        P = new Flags();
        P.setStatus(0x34);

        // Must point to reset vector which is at index
        // 0xFFFD (High) and 0xFFFC (low)
        PC = bus.read(0xFFFC) | (bus.read(0xFFFD) << 8);
    }

    public int getA() {
        return A;
    }

    public void setA(int a) {
        A = a;
    }

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
    }

    /**
     * Increment PC by one
     * @return previous PC value before incremental
     */
    public int incPC() {
        int previous = PC;
        PC = (PC + 1) &  0xFFFF;
        return previous;
    }

    /**
     * Decrement PC by one
     * @return previous PC value before decrement
     */
    public int decPC() {
        int previous = PC;
        PC = (PC - 1) &  0xFFFF;
        return previous;
    }

    /**
     * Increment the stack pointer by one.
     * The stack will always be in range 0x100 to 0x1FF, so any overflow
     * will lead to wrapping around the range.
     * @return SP value after increment
     */
    public int incSP() {
        SP =  (SP + 1) & 0x100;
        return SP;
    }

    /**
     * Decrement the stack pointer by one.
     * The stack will always be in range 0x100 to 0x1FF, so any overflow
     * will lead to wrapping around the range.
     * @return previous SP value before decrement
     */
    public int decSP() {
        int previous = SP;
        SP =  (SP - 1) & 0x100;
        return previous;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public Flags getFlags() {
        return P;
    }

    public int getCycles() {
        return cycles;
    }

    //region opcodes methods

    /**
     * Add memory to accumulator with carry
     */
    public void adc() {
        int result = A + value + (P.C ? 1 : 0);
        result = P.setCFlag(result);
        P.setVFlag(A, value, result);
        A = result;
        P.setZSFlags(A);
    }


    /**
     * Logical And memory with accumulator
     */
    public void and() {
        A = A & value;
        P.setZSFlags(A);
    }
//
    /**
     * Arithmetic Shift left. Shifts accumulator one bit left
      */
    public void aslA() {
        A = asl();
    }

    /**
     * Arithmetic Shift left. Shifts memory content one bit left
     */
    public void aslM() {
        bus.write(asl(), address);
    }

    /**
     * Branch on Plus.
     * Branches if Positive
     */
    public void bpl() {
        branch(!P.S);
    }
//

    /**
     * And accumulator with memory
     */
    public void bit() {
        int result = A & value;
        P.setZFlag(result);
        P.setSFlag(value);
        // Set overflow depending on the 6th bit of the memory content
        P.V = (value & 0x40) == 0x40;
    }

    /**
     * Branch if Minus
     */
    public void bmi() {
        branch(P.S);
    }

    /**
     * Force Break (Software Interrupt)
      */
    public void brk() {
        // Increment PC by one, so that when RTI is called, the returned
        // address will be brk + 2. One already increased when reading the
        // instruction, and one is done below
        incPC();
        P.I = true;
        // Write PC and processor status
        bus.write(PC >> 8, decSP());
        bus.write(PC & 0xFF, decSP());
        bus.write(P.getStatus(), decSP());
        // Set PC to interrupt vector address
        PC = bus.read(0xFFFE) | (bus.read(0xFFFF) << 8);
    }

    /**
     * Branch if Overflow clear
     */
    public void bvc() {
        branch(!P.V);
    }

    /**
     * Branch if overflow Set
     */
    public void bvs() {
        branch(P.V);
    }

    /**
     * Branch if Carry Clear
     */
    public void bcc() {
        branch(!P.C);
    }

    /**
     * Branch if Carry Set
     */
    public void bcs() {
        branch(P.C);
    }

    /**
     * Clear Carry Flag
     */
    public void clc() {
        P.C = false;
    }

    /**
     * Clear Interrupt Disable
     */
    public void cli() {
        P.I = false;
    }

    /**
     * Clear Overflow Flag
     */
    public void clv() {
        P.V = false;
    }

    /**
     * Compare Accumulator
     */
    public void cmp() {
        compare(A);
    }

    /**
     * Compare Y Register
     */
    public void cpy() {
        compare(Y);
    }

    /**
     * Decrement Memory Content by one
     */
    public void dec() {
        addMemory(-1);
    }

    /**
     * Decrement X Register by one
     */
    public void dex() {
        addX(-1);
    }

    /**
     * Decrement Y Register by one
     */
    public void dey() {
        addY(-1);
    }

    /**
     * Exclusive OR accumulator with memory
     */
    public void eor() {
        A = (A ^ value) & 0xFF;
        P.setZSFlags(A);
    }

    /**
     * Increment X Register by one
     */
    public void inx() {
        addX(1);
    }

    /**
     * Increment Y Register by one
     */
    public void iny() {
        addY(1);
    }

    /**
     * Jump to absolute or indirect address
     */
    public void jmp() {
        PC = address;
    }


    /**
     * Jump to Subroutine
     */
    public void jsr() {
        // TODO check if implementation is correct
        // It is expected that the jsr will store the address  (minus one)
        // to the stack then jump to given address
        decPC();
        // High byte
        bus.write(PC >> 8, decSP());
        // Low byte
        bus.write(PC & 0xFF, decSP());
        PC = address;
    }
//

    /**
     * Load Accumulator from memory
     */
    public void lda() {
        A = value;
        P.setZSFlags(A);
    }

    /**
     * Load X Register from Memory
     */
    public void ldx() {
        X = value;
        P.setZSFlags(X);
    }


    /**
     * Load Y Register from memory
     */
    public void ldy() {
        Y = value;
        P.setZSFlags(Y);
    }


    /**
     * Logical Shift Right Accumulator
     */
    public void lsrA() {
        A = lsr();
    }

    /**
     * Logical Shift Right Memory Content
     */
    public void lsrM() {
        bus.write(lsr(), address);
    }

    // Logically OR Memory content with Accumulator
    public void ora() {
        A = A | value;
        P.setZSFlags(A);
    }

    /**
     * Push Accumulator to stack
     */
    public void pha() {
        ph(A);
    }

    /**
     * Push Processor Status.
     * Pushes a copy of status into stack
     */
    public void php() {
        ph(P.getStatus());
    }

    /**
     * Pull Accumulator from stack
     */
    public void pla() {
        A = pl();
        P.setZSFlags(A);
    }

    /**
     * Pull Processor Status
     */
    public void plp() {
        P.setStatus(pl());
    }

    /**
     * Rotate Accumulator Left through Carry
     */
    public void rolA() {
        A = rol();
    }

    /**
     * Rotate Memory content Left through Carry
     */
    public void rolM() {
        bus.write(rol(), address);
    }

    /**
     * Rotate Accumulator Right through Carry
     */
    public void rorA() {
        A = ror();
    }

    /**
     * Rotate Memory Content Right through Carry
     */
    public void rorM() {
        bus.write(ror(), address);
    }

    /**
     * Return from Interrupt
     */
    public void rti() {
        P.setStatus(bus.read(incSP()));
        PC = bus.read(incSP());
        PC += (bus.read(incSP()) << 8);
    }
//

    /**
     * Return from Subroutine
     */
    public void rts() {
        PC = bus.read(incSP());
        PC += (bus.read(incSP()) << 8);
    }

//    // Subtract Bus from Accumulator with Borrow
//    public void sbc(int address) {
//        // Same as ADC. The only difference is value is treated as
//        // complement value. Code repeated to avoid stack call
//        int value = 255 -  bus.read(address);
//        int result = A + value + (P.C ? 1 : 0);
//
//        result = P.setCFlag(result);
//        P.setVFlag(A, value, result);
//        A = result;
//        P.setZSFlags(A);
//    }

    /**
     * Set Carry Flag
     */
    public void sec() {
        P.C = true;
    }

    /**
     * Set Interrupt Disable
     */
    public void sei() {
        P.I = true;
    }

    /**
     * Store Accumulator into memory
     */
    public void sta() {
        st(A);
    }

    /**
     * Store X Register into memory
     */
    public void stx() {
        st(X);
    }

    /**
     * Store Y Register into memory
     */
    public void sty() {
        st(Y);
    }

    /**
     * Transfer X to Accumulator
     */
    public void txa() {
        A = X;
        P.setZSFlags(A);
    }

    /**
     * Transfer X to Stack Pointer
     */
    public void txs() {
        bus.write(X, decSP());
    }

    /**
     * Transfer Accumulator to X Register
     */
    public void tax() {
        X = A;
        P.setZSFlags(X);
    }

    /**
     * Transfer Accumulator to Y Register
     */
    public void tay() {
        Y = A;
        P.setZSFlags(Y);
    }

    /**
     * Transfer Stack Pointer to X
     */
    public void tsx() {
        X = bus.read(SP);
        P.setZSFlags(X);
    }

    /**
     * Transfer Y to Accumulator;
     */
    public void tya() {
        A = Y;
        P.setZSFlags(A);
    }

    //endregion

    //region  Address Modes

    /**
     * Accumulator
     */
    private void acc() {
        value = A;
    }

    /**
     * Immediate
     */
    private void imm() {
        op1 = incPC();
        value = op1;
    }

    /**
     * Zero page address
     * @return
     */
    private void zpg() {
        // Read first byte only after instruction for memory address
        op1 = bus.read(incPC());
        // TODO is address ever being read?
        address = op1;
        value = bus.read(address);
    }

    /**
     * Zero page indexed with X
     */
    private void zpgx() {
        op1 = bus.read(incPC());
        // Wrap around if needed
        address = (op1 + X) & 0xFF;
        value = bus.read(address);
    }

    /**
     * Zero page indexed with Y.
     * Only used by LDX and STX
     */
    private void zpgy() {
        op1 = bus.read(incPC());
        // Wrap around if needed
        address = (op1 + Y) & 0xFF;
        value = bus.read(address);
    }

    /**
     * Relative
     */
    private void rel() {
        op1 = bus.read(incPC());
        value = op1;
    }

    /**
     * Absolute
     */
    private void abs() {
        // Read the first and second byte after instruction for memory address
        op1 = bus.read(incPC());
        op2 = bus.read(incPC());
        address = buildAddress(op1, op2);
        value = bus.read(address);
    }

    /**
     * Absolute indexed with X.
     * Adds a cycle if cross page occurs
     */
    private void absx() {
        abs();

        if ((address & 0xFF00) != ((address + X) & 0xFF00))
            cycles++;

        address = (X + address) & 0xFFFF;
        value = bus.read(address);
    }

    /**
     * Absolute indexed with X.
     * No extra cycles are incremented when cross page happens
     */
    private void absxPlus() {
        abs();
        address = (X + address) & 0xFFFF;
        value = bus.read(address);
    }

    /**
     * Absolute indexed with Y.
     * Adds a cycle if cross page occurs
     */
    private void absy() {
        abs();

        if ((address & 0xFF00) != ((address + Y) & 0xFF00))
            cycles++;

        address = (Y + address) & 0xFFFF;
        value = bus.read(address);
    }

    /**
     * Absolute indexed with Y.
     * No extra cycles are incremented when cross page happens
     */
    private void absyPlus() {
        abs();
        address = (Y + address) & 0xFFFF;
        value = bus.read(address);
    }

    /**
     * Indirect.
     * Only used by JMP
     */
    private void ind() {
        op1 = bus.read(incPC());
        op2 = bus.read(incPC());
        int indirect = buildAddress(op1, op2);
        address = bus.read(indirect);
        // Fixes first operand (low byte) cross page boundary
        indirect = buildAddress((op1 + 1) & 0xFF, op2);
        address += bus.read(indirect) << 8;
    }

    /**
     * Indirect X. Also known as Indexed Indirect
     * @return
     */
    private void indx() {
        // X + first byte after instruction as memory address to access first
        // 256 bytes of memory, which wraparound when addition is overflowed.
        // The Index and next byte (within zero page) is considered the 16 bit
        // address
        op1 = bus.read(incPC());
        int indirect = bus.read(op1);
        address = (X + indirect) & 0xFF;
        value = bus.read(address);
    }

    /**
     * Indirect Y. Also known as Indirect Indexed.
     * Adds a cycle if cross page occurs
     */
    private void indy() {
        op1 = bus.read(incPC());
        address = bus.read(op1);


        // Increment cycle if cross page happens
        if( address != ((address + Y) & 0xFF))
            cycles++;

        address = (address + Y) & 0xFF;
        value = bus.read(address);
    }

    /**
     * Indirect Y. Also known as Indirect Indexed.
     * Used exclusively with STA. No extra cycles are incremented when
     * cross page happens
     */
    private void indyPlus() {
        op1 = bus.read(incPC());
        address = bus.read(op1);
        address = (op1 + Y) & 0xFF;
        value = bus.read(address);
    }

    //endregion

    // region Helper methods

    /**
     * Add value to memory
     * @param cnt  Value content
     */
    public void addMemory(int cnt) {
        int result = (value + cnt) & 0xFF;
        bus.write(result, address);
        P.setZSFlags(result);
    }

    /**
     * Add value to X register
     * @param val
     */
    public void addX(int val) {
        X = (X + val) & 0xFF;
        P.setZSFlags(X);
    }

    /**
     * Add value to Y register
     * @param val
     */
    private void addY(int val) {
        Y = (Y + val) & 0xFF;
        P.setZSFlags(Y);
    }

    /**
     * Arithmetic Shift left. Shift value one bit left
     * @return value shifted
     */
    private int asl() {
        value = value << 1;
        value =  P.setCFlag(value);
        P.setZSFlags(value);

        return value;
    }

    /**
     * Whether to branch execution
     * @param cond condition flag whether to branch or not
     */
    private void branch(boolean cond) {
        if (!cond)
            return;

        // Reaching here meaning branch will happen
        // so increment cycles
        cycles++;

        int oldPC = PC;
        PC += value;
        // Increment cycles if cross page occurs
        if( (oldPC & 0xFF00) != (PC & 0xFF00))
            cycles+=1;
    }

    /**
     * Subtract memory content from given compare value and set C, F and S
     * flags accordingly
     * @param cmp Value to compare
     */
    private void compare(int cmp) {
        // Get 2's complements of value
        int result = cmp + ((value * -1) & 0xFF);
        P.setCFlag(result);
        P.setZFlag(result);
        P.setSFlag(result);
    }




    // Logical Shift Right of Accumulator or Bus
    public int lsr() {
        int lowBit = value & 0x1;
        value = value >> 1;
        P.C = lowBit == 0x1;
        P.setZFlag(value);
        P.S = false;

        return value;
    }

    /**
     * Push value into Stack
     * @param val Value that needs to be pushed into stack
     */
    private void ph(int val) {
        bus.write(val, decSP());
    }

    /**
     * Pull content from Stack
     * @return
     */
    public int pl() {
        return bus.read(incSP());
    }

    /**
     * Rotate Accumulator or Memory content Left through Carry
     */
    private int rol() {
        value = (value << 1) | (P.C ? 1 : 0);
        P.C = (value & 0x100) == 0x100;
        value = value & 0xFF;
        P.setZSFlags(value);

        return value;
    }

    /**
     * Rotate Accumulator or Memory content Right through Carry
     * @return
     */
    public int ror() {
        int lowBit = value & 0x01;
        value = (P.C ? 0x80 : 0) | (value >> 1);
        P.C = (lowBit & 0x01) == 0x01;
        P.setZSFlags(value);

        return value;
    }

    /**
     * Store value in Bus
     * @param val
     */
    private void st(int val) {
        bus.write(val, address);
    }

    /**
     * Build Address from little endian address
     * @param low least significant byte
     * @param high high significant byte
     * @return Integer representing address
     */
    private static int buildAddress(int low, int high) {
        return (high << 8) | low;
    }

    // endregion
}
