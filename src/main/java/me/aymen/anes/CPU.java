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
//        addressMode[ACC] = ::acc;
//        addressMode[IMM] = this::imm;
        addressMode[ZPG] = this::zpg;
//        addressMode[ZPGX] = this::zpgx;
//        addressMode[ZPGY] = this::zpgy;
//        addressMode[REL] = () -> {};
//        addressMode[ABS] = this::abs;
//        addressMode[ABSX] = this::absx;
//        addressMode[ABSY] = this::absy;
//        addressMode[IND] = this::ind;
        addressMode[INDX] = this::indx;
//        addressMode[INDY] = this::indy;


        // 0x0#
        opcodes[0x00] = new Inst("BRK", 7, IMPL , this::brk);
        opcodes[0x01] = new Inst("ORA", 6, INDX, this::ora);
        opcodes[0x02] = null;
        opcodes[0x03] = null;
        opcodes[0x04] = null;
        opcodes[0x05] = new Inst("ORA", 3, ZPG, this::ora);
        opcodes[0x06] = new Inst("ASL", 5, ZPG, this::aslA);
    }

    /**
     * Execute instruction at current program counter
     */
    /*public void execute() {
        int address;
        int instruction = bus.read(PC++);

        switch (instruction) {

            // ADC
            case Inst4.ADC_PRE:
                cycles+=6;
                adc(indx());
                break;
            case Inst4.ADC_ZPG:
                cycles+=3;
                adc(zpg());
                break;
            case Inst4.ADC_IMM:
                cycles+=2;
                adc(imm());
                break;
            case Inst4.ADC_ABS:
                cycles+=4;
                adc(abs());
                break;
            case Inst4.ADC_POS:
                cycles+=5;
                adc(pos(true));
                break;
            case Inst4.ADC_ZPGX:
                cycles+=4;
                adc(zpgIndx(X));
                break;
            case Inst4.ADC_ABSY:
                cycles+=4;
                adc(indx(Y, true));
                break;
            case Inst4.ADC_ABSX:
                cycles+=4;
                adc(indx(X, true));
                break;

            // AND
            case Inst4.AND_PRE:
                cycles+=6;
                and(indx());
                break;
            case Inst4.AND_ZPG:
                cycles+=3;
                and(zpg());
                break;
            case Inst4.AND_IMM:
                cycles+=2;
                and(imm());
                break;
            case Inst4.AND_ABS:
                cycles+=4;
                and(abs());
                break;
            case Inst4.AND_POS:
                cycles+=5;
                and(pos(true));
                break;
            case Inst4.AND_ZPGX:
                cycles+=4;
                and(zpgIndx(X));
                break;
            case Inst4.AND_ABSY:
                cycles+=4;
                and(zpgIndx(Y));
                break;
            case Inst4.AND_ABSX:
                cycles+=4;
                and(zpgIndx(X));
                break;

            // ASL
            case Inst4.ASL_ACC:
                cycles+=2;
                A = asl(A);
                break;
            case Inst4.ASL_ABS:
                cycles+=6;
                address = abs();
                bus.write(asl(bus.read(address)), address);
                break;
            case Inst4.ASL_ZPGX:
                cycles+=6;
                address = zpgIndx(X);
                bus.write(asl(bus.read(address)), address);
                break;
            case Inst4.ASL_ABSX:
                cycles+=7;
                address = zpgIndx(X);
                bus.write(asl(bus.read(address)), address);
                break;

            case Inst4.BCC:
                cycles+=2;
                branch(!P.C);
                break;
            case Inst4.BCS:
                cycles+=2;
                branch(P.C);
                break;
            case Inst4.BEQ:
                cycles+=2;
                branch(P.Z);
                break;
            case Inst4.BIT_ZPG:
                cycles+=3;
                bit(zpg());
                break;
            case Inst4.BIT_ABS:
                cycles+=3;
                bit(abs());
                break;
            case Inst4.BMI:
                cycles+=2;
                branch(P.S);
                break;
            case Inst4.BNE:
                cycles+=2;
                branch(!P.Z);
                break;
            case Inst4.BPL:
                cycles+=2;
                branch(!P.S);
                break;
            case Inst4.BVC:
                cycles+=2;
                branch(!P.V);
                break;
            case Inst4.BVS:
                cycles+=2;
                branch(P.V);
                break;
            case Inst4.CLC:
                cycles+=2;
                P.C = false;
                break;
            case Inst4.CLD:
                cycles+=2;
                P.D = false;
                break;
            case Inst4.CLI:
                cycles+=2;
                P.I = false;
                break;
            case Inst4.CLV:
                cycles+=2;
                P.I = false;
                break;

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
            case Inst4.DEY:
                cycles+=2;
                iny(0xFF);
                break;

            // EOR
            case Inst4.EOR_PRE:
                cycles+=6;
                eor(indx());
                break;
            case Inst4.EOR_ZPG:
                cycles+=3;
                eor(zpg());
                break;
            case Inst4.EOR_IMM:
                cycles+=2;
                eor(imm());
                break;
            case Inst4.EOR_ABS:
                cycles+=4;
                eor(abs());
                break;
            case Inst4.EOR_POS:
                cycles+=5;
                eor(pos(true));
            case Inst4.EOR_ZPGX:
                cycles+=4;
                eor(zpgIndx(X));
                break;
            case Inst4.EOR_ABSY:
                cycles+=4;
                eor(indx(Y, true));
                break;
            case Inst4.EOR_ABSX:
                cycles+=4;
                adc(indx(X, true));
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
            case Inst4.JMP_ABS:
                cycles+=3;
                jmp(abs());
                break;
            case Inst4.JMP_IND:
                cycles+=5;
                jmp(ind());
                break;

            case Inst4.JSR:
                cycles+=6;
                jsr(abs());
                break;

            // LDA
            case Inst4.LDA_PRE:
                cycles+=6;
                lda(indx());
                break;
            case Inst4.LDA_ZPG:
                cycles+=3;
                lda(zpg());
                break;
            case Inst4.LDA_IMM:
                cycles+=2;
                lda(imm());
                break;
            case Inst4.LDA_ABS:
                cycles+=4;
                lda(abs());
                break;
            case Inst4.LDA_POS:
                cycles+=5;
                lda(pos(true));
                break;
            case Inst4.LDA_ZPGX:
                cycles+=4;
                lda(zpgIndx(X));
                break;
            case Inst4.LDA_ABSY:
                cycles+=4;
                lda(indx(Y, true));
                break;
            case Inst4.LDA_ABSX:
                cycles+=4;
                lda(indx(X, true));
                break;

            // LDX
            case Inst4.LDX_IMM:
                cycles+=2;
                ldx(imm());
                break;
            case Inst4.LDX_ZPG:
                cycles+=3;
                ldx(zpg());
                break;
            case Inst4.LDX_ABS:
                cycles+=4;
                ldx(abs());
                break;
            case Inst4.LDX_ZPGY:
                cycles+=4;
                ldx(zpgIndx(Y));
                break;
            case Inst4.LDX_ABSY:
                cycles+=4;
                ldx(indx(Y, true));
                break;

            // LDY
            case Inst4.LDY_IMM:
                cycles+=2;
                ldy(imm());
                break;
            case Inst4.LDY_ZPG:
                cycles+=3;
                ldy(zpg());
                break;
            case Inst4.LDY_ABS:
                cycles+=4;
                ldy(abs());
                break;
            case Inst4.LDY_ZPGX:
                cycles+=4;
                ldy(zpgIndx(X));
                break;
            case Inst4.LDY_ABSX:
                cycles+=4;
                ldy(indx(X, false));
                break;

            // LSR
            case Inst4.LSR_ZPG:
                cycles+=5;
                address = zpg();
                bus.write(lsr(bus.read(address)), address);
                break;
            case Inst4.LSR_ACC:
                cycles+=2;
                A = lsr(A);
                break;
            case Inst4.LSR_ABS:
                cycles+=6;
                address = abs();
                bus.write(lsr(bus.read(address)), address);
                break;
            case Inst4.LSR_ZPGX:
                cycles+=6;
                address = zpgIndx(X);
                bus.write(lsr(bus.read(address)), address);
                break;
            case Inst4.LSR_ABSX:
                cycles+=7;
                address = zpgIndx(X);
                bus.write(lsr(bus.read(address)), address);
                break;

            case Inst4.NOP:
                cycles+=2;
                break;

            // ORA
            case Inst4.ORA_IMM:
                cycles+=2;
                ora(imm());
                break;
            case Inst4.ORA_ABS:
                cycles+=4;
                ora(abs());
                break;
            case Inst4.ORA_POS:
                cycles+=5;
                ora(pos(true));
                break;
            case Inst4.ORA_ZPGX:
                cycles+=4;
                ora(zpgIndx(X));
                break;
            case Inst4.ORA_ABSY:
                cycles+=4;
                ora(indx(Y, true));
                break;
            case Inst4.ORA_ABSX:
                cycles+=4;
                ora(indx(X, true));
                break;

            case Inst4.PHA:
                cycles+=3;
                ph(A);
                break;
            case Inst4.PHP:
                cycles+=3;
                ph(P.getStatus());
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

            // ROL
            case Inst4.ROL_ZPG:
                cycles+=5;
                address = zpg();
                bus.write(rol(bus.read(address)), address);
                break;
            case Inst4.ROL_ACC:
                cycles+=2;
                A = rol(A);
                break;
            case Inst4.ROL_ABS:
                cycles+=6;
                address = abs();
                bus.write(rol(bus.read(address)), address);
                break;
            case Inst4.ROL_ZPGX:
                cycles+=6;
                address = zpgIndx(X);
                bus.write(rol(bus.read(address)), address);
                break;
            case Inst4.ROL_ABSX:
                cycles+=7;
                address = zpgIndx(X);
                bus.write(rol(bus.read(address)), address);
                break;

            // ROR
            case Inst4.ROR_ZPG:
                cycles+=5;
                address = zpg();
                bus.write(ror(bus.read(address)), address);
                break;
            case Inst4.ROR_ACC:
                cycles+=2;
                A = rol(A);
                break;
            case Inst4.ROR_ABS:
                cycles+=6;
                address = abs();
                bus.write(ror(bus.read(address)), address);
                break;
            case Inst4.ROR_ZPGX:
                cycles+=6;
                address = zpgIndx(X);
                bus.write(ror(bus.read(address)), address);
                break;
            case Inst4.ROR_ABSX:
                cycles+=7;
                address = zpgIndx(X);
                bus.write(ror(bus.read(address)), address);
                break;

            case Inst4.RTI:
                cycles+=6;
                rti();
                break;
            case Inst4.RTS:
                cycles+=6;
                rts();
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

            case Inst4.SEC:
                cycles+=2;
                P.C = true;
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

            // STA
            case Inst4.STA_PRE:
                cycles+=6;
                st(indx(), A);
                break;
            case Inst4.STA_ZPG:
                cycles+=3;
                st(zpg(), A);
                break;
            case Inst4.STA_ABS:
                cycles+=4;
                st(abs(), A);
                break;
            case Inst4.STA_POS:
                cycles+=6;
                // Extra cycle is false as its always an extra cycle for STA
                // than other instructions
                st(pos(false), A);
                break;
            case Inst4.STA_ZPGX:
                cycles+=4;
                st(zpgIndx(X), A);
                break;
            case Inst4.STA_ABSY:
                cycles+=5;
                st(indx(Y, false), A);
                break;
            case Inst4.STA_ABSX:
                cycles+=5;
                st(indx(X, false), A);
                break;

            // STX
            case Inst4.STX_ZPG:
                cycles+=3;
                st(zpg(), X);
                break;
            case Inst4.STX_ABS:
                cycles+=4;
                st(abs(), X);
                break;
            case Inst4.STX_ZPGY:
                cycles+=4;
                st(zpgIndx(Y), X);
                break;

            // STY
            case Inst4.STY_ZPG:
                cycles+=3;
                st(zpg(), Y);
                break;
            case Inst4.STY_ABS:
                cycles+=4;
                st(abs(), Y);
                break;
            case Inst4.STY_ZPGX:
                cycles+=4;
                st(zpgIndx(X), Y);
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
        int currentPC = PC++;

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
         *  2. Invoke
         */
        Inst opcode = opcodes[op];
        addressMode[opcode.mode].process();
        opcode.operation.process();

        return String.format("%04X    %s", currentPC,
                Deassembler.analyse(opcode, op1, op2));
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

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }


    /**
     * Retrieve SP to be within range 0x100 to 0x1FF
     * @return
     */
    public int getSP() {
        return SP + 0x100;
    }

    /**
     * Decrement the stack pointer by one.
     * The stack will always be in range 0x100 to 0x1FF, so any overflow
     * will lead to wrapping around the range.
     */
    public void decSP() {
        SP--;
        SP &= 0x100;
    }

    /**
     * Increment the stack pointer by one.
     * The stack will always be in range 0x100 to 0x1FF, so any overflow
     * will lead to wrapping around the range.
     */
    public void incSP() {
        SP++;
        SP &= 0x100;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public Flags getFlags() {
        return P;
    }

    //region opcodes methods

//    // Add memory to accumulator with carry
//    public void adc(int address) {
//        int value = bus.read(address);
//        int result = A + value + (P.C ? 1 : 0);
//
//        result = P.setCFlag(result);
//        P.setVFlag(A, value, result);
//        A = result;
//        P.setZSFlags(A);
//    }
//
//    // And memory with accumulator
//    public void and(int address) {
//        int value = bus.read(address);
//        A = A & value;
//        P.setZSFlags(A);
//    }
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
     * Arithmetic Shift left. Shift value one bit left
     * @return value shifted
     */
    private int asl() {
        value = value << 1;
        value =  P.setCFlag(value);
        P.setZSFlags(value);

        return value;
    }
//
//    // And accumulator with memory
//    public void bit(int address) {
//        int value = bus.read(address);
//        int result = A & value;
//
//        P.setZFlag(result);
//        P.setSFlag(value);
//        // Set overflow depending on the 6th bit of the memory content
//        P.V = (value & 0x40) == 0x40 ? true : false;
//    }

    // Force Break (Software Interrupt)
    public void brk() {
        PC+=2;
        bus.write(PC >> 8, getSP());
        decSP();
        bus.write(PC & 0xFF, getSP());
        decSP();
        bus.write(P.getStatus(), getSP());
        decSP();
        PC = bus.read(0xFFFE) | (bus.read(0xFFFF) << 8);
        P.I = true;
    }

//    // increment memory (by value)
//    public void inc(int value, int address) {
//        int result = (bus.read(address) + value) & 0xFF;
//        bus.write(result, address);
//        P.setZSFlags(result);
//    }
//
//    // increment X register (by value)
//    public void inx(int value) {
//        X = (X + value) & 0xFF;
//        P.setZSFlags(X);
//    }
//
//    // decrement Y register (by 1)
//    public void iny(int value) {
//        Y = (Y + value) & 0xFF;
//        P.setZSFlags(Y);
//    }
//
//    // Xor accumulator with memory
//    public void eor(int address) {
//        int value = bus.read(address);
//        A = (A ^ value) & 0xFF;
//        P.setZSFlags(A);
//    }
//
//    // Jump to absolute or indirect address
//    public void jmp(int address) {
//        PC = address;
//    }
//
//    // Jump to Subroutine
//    public void jsr(int address) {
//        // It is expected that the jsr will store the address of the third byte
//        PC--;
//        bus.write(PC >> 8, getSP());
//        decSP();
//        bus.write(PC & 0xFF, getSP());
//        decSP();
//        PC = address;
//    }
//
//    // Load Accumulator from Bus
//    public void lda(int address) {
//        int value = bus.read(address);
//        A = value;
//        P.setZSFlags(A);
//    }
//
//    // Load Index Register X from Bus
//    public void ldx(int address) {
//        int value = bus.read(address);
//        X = value;
//        P.setZSFlags(X);
//    }
//
//    // Load Index Register Y from Bus
//    public void ldy(int address) {
//        int value = bus.read(address);
//        Y = value;
//        P.setZSFlags(Y);
//    }
//
//    // Logical Shift Right of Accumulator or Bus
//    public int lsr(int value) {
//        int lowBit = value & 0x1;
//        value = value >> 1;
//        P.C = lowBit == 0x1;
//        P.setZFlag(value);
//        P.S = false;
//
//        return value;
//    }
//
    // Logically OR Bus with Accumulator
    public void ora() {
        A = A | value;
        P.setZSFlags(A);
    }
//
//    // Push value into Stack
//    public void ph(int value) {
//        bus.write(value, getSP());
//        decSP();
//    }
//
//    // Pull content from Stack
//    public int pl() {
//        incSP();
//        return bus.read(getSP());
//    }
//
//    // Rotate Accumulator or Bus Left through Carry
//    public int rol(int value) {
//        value = (value << 1) | (P.C ? 1 : 0);
//        P.C = (value & 0x100) == 0x100;
//        value = value & 0xFF;
//        P.setZSFlags(value);
//
//        return value;
//    }
//
//    // Rotate Accumulator or Bus Right through Carry
//    public int ror(int value) {
//        int lowBit = value & 0x01;
//        value = (P.C ? 0x80 : 0) | (value >> 1);
//        P.C = (lowBit & 0x01) == 0x01;
//        P.setZSFlags(value);
//
//        return value;
//    }
//
//    // Return from Interrupt
//    public void rti() {
//        incSP();
//        P.setStatus(bus.read(getSP()));
//        incSP();
//        PC = bus.read(getSP());
//        incSP();
//        PC += (bus.read(getSP()) << 8);
//    }
//
//    // Return from Subroutine
//    public void rts() {
//        incSP();
//        PC = bus.read(getSP());
//        incSP();
//        PC += (bus.read(getSP()) << 8);
//
//        // The jsr stored the third byte as an address, so pc is
//        // incremented to point to next next instruction after return
//        PC++;
//    }
//
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
//
//    // Store value in Bus
//    public void st(int address, int value) {
//        bus.write(value, address);
//    }

    /**
     * Subtract memory content from given compare value and set C, F and S
     * flags accordingly
     * @param cmp Value to compare
     * @param address memory address of content to compare with
     */
    public void compare(int cmp, int address) {
        int value = bus.read(address);

        // Get 2's complements of value
        int result = cmp + ((value * -1) & 0xFF);
        P.setCFlag(result);
        P.setZFlag(result);
        P.setSFlag(result);
    }

//    /**
//     * Whether to branch execution
//     * @param cond True if branching condition is met
//     */
//    public void branch(boolean cond) {
//        PC++;
//        if (!cond)
//            return;
//
//        byte value = (byte) bus.read(PC);
//
//        int oldPC = PC;
//        PC += value;
//        cycles++;
//
//        // When cross page occurs
//        if( (oldPC & 0xFF00) != (PC & 0xFF00))
//            cycles+=1;
//    }

    //endregion

    //region  Address Modes

    /**
     * Immediate address
     * @return
     */
    private int imm() {
        return PC++;
    }

    /**
     * Absolute (direct) address
     * @return
     */
    private int abs() {
        // Read the first and second byte after instruction for memory address
        return bus.read(PC++) | (bus.read(PC++) << 8);
    }

    /**
     * Zero page address
     * @return
     */
    private void zpg() {
        // Read first byte only after instruction for memory address
        op1 = bus.read(PC++);
        // TODO is address ever being read?
        address = op1;
        value = bus.read(address);
    }

    /**
     * Pre-Index Indirect Address
     * @return
     */
    private void indx() {
        // X + first byte after instruction as memory address to access first
        // 256 bytes of memory, which wraparound when addition is overflowed.
        // The Index and next byte (within zero page) is considered the 16 bit
        // address
        op1 = bus.read(PC++);
        address = (X + op1) & 0xFF;
        value = bus.read(address);
    }

    /**
     * Post-Index Indirect Address (a.k.a. Indirect indexed addressing)
     * @param extra Whether to consider to add extra cycle of page boundary is crossed.
     *              Used when cycle if dependent on page boundary
     * @return
     */
    private int pos(boolean extra) {
        // First byte after instruction the 16 bit address.
        // Index + Y is returned
        int address = bus.read(PC++) | (bus.read(PC++) << 8);

        if( extra &&
                ((address & 0xFF00) != ((address + Y) & 0xFF00)))
            cycles++;

        return address + Y;
    }

    /**
     * Zero page indexed Addressing.
     * @param register X or Y register value to sum address with
     * @return
     */
    private int zpgIndx(int register) {
        // Crossing page boundary is not to be handled
        return (register + bus.read(PC++)) & 0xFF;
    }

    /**
     * Absolute Indexed Addressing.
     * @param register X or Y register value to sum address with
     * @param extra Whether to consider to add extra cycle of page boundary is crossed.
     *              Used when cycle if dependent on page boundary
     * @return
     */
    private int indx(int register, boolean extra) {
        // First and second byte + register as a 16 bit address
        int address = bus.read(PC++) | (bus.read(PC++) << 8);

        if ( extra && (address & 0xFF00) != ((address + Y) & 0xFF00) )
            cycles++;

        return (register + address) & 0xFFFF;
    }

    /**
     * indirect address
     * @return
     */
    private int ind() {
        int low = bus.read(PC++);
        int high = bus.read(PC++);
        int address = low | (high << 8);
        int value = bus.read(address);
        // Fixes first operand (low byte) cross page boundary
        address = ( (low + 1) & 0xFF) | (high << 8);
        value+= bus.read(address) << 8;

        return value;
    }

    //endregion

    public int getCycles() {
        return cycles;
    }
}
