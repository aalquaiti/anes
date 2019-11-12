package me.aymen.anes;

public class CPU {

    // CPU Components
    private Memory memory;
    private int cycles;

    // Registers
    private int A;          // Accumulator
    private int X;          // X Index
    private int Y;          // Y Index
    private int PC;         // Program Counter
    private int SP;         // Stack Pointer
    private Flags P;        // Process Status

    public CPU(Memory memory) {
        this.memory = memory;
    }

    /**
     * Execute instruction at current program counter
     */
    public void execute() {
        int address;
        int instruction = memory.read(PC++);

        switch (instruction) {

            // ADC
            case Inst.ADC_PRE:
                cycles+=6;
                adc(pre());
                break;
            case Inst.ADC_ZPG:
                cycles+=3;
                adc(zpg());
                break;
            case Inst.ADC_IMM:
                cycles+=2;
                adc(imm());
                break;
            case Inst.ADC_ABS:
                cycles+=4;
                adc(abs());
                break;
            case Inst.ADC_POS:
                cycles+=5;
                adc(pos(true));
                break;
            case Inst.ADC_ZPGX:
                cycles+=4;
                adc(zpgIndx(X));
                break;
            case Inst.ADC_ABSY:
                cycles+=4;
                adc(indx(Y, true));
                break;
            case Inst.ADC_ABSX:
                cycles+=4;
                adc(indx(X, true));
                break;

            // AND
            case Inst.AND_PRE:
                cycles+=6;
                and(pre());
                break;
            case Inst.AND_ZPG:
                cycles+=3;
                and(zpg());
                break;
            case Inst.AND_IMM:
                cycles+=2;
                and(imm());
                break;
            case Inst.AND_ABS:
                cycles+=4;
                and(abs());
                break;
            case Inst.AND_POS:
                cycles+=5;
                and(pos(true));
                break;
            case Inst.AND_ZPGX:
                cycles+=4;
                and(zpgIndx(X));
                break;
            case Inst.AND_ABSY:
                cycles+=4;
                and(zpgIndx(Y));
                break;
            case Inst.AND_ABSX:
                cycles+=4;
                and(zpgIndx(X));
                break;

            // ASL
            case Inst.ASL_ZPG:
                cycles+=5;
                address = zpg();
                memory.write(asl(memory.read(address)), address);
                break;
            case Inst.ASL_ACC:
                cycles+=2;
                A = asl(A);
                break;
            case Inst.ASL_ABS:
                cycles+=6;
                address = abs();
                memory.write(asl(memory.read(address)), address);
                break;
            case Inst.ASL_ZPGX:
                cycles+=6;
                address = zpgIndx(X);
                memory.write(asl(memory.read(address)), address);
                break;
            case Inst.ASL_ABSX:
                cycles+=7;
                address = zpgIndx(X);
                memory.write(asl(memory.read(address)), address);
                break;

            case Inst.BCC:
                cycles+=2;
                branch(!P.C);
                break;
            case Inst.BCS:
                cycles+=2;
                branch(P.C);
                break;
            case Inst.BEQ:
                cycles+=2;
                branch(P.Z);
                break;
            case Inst.BIT_ZPG:
                cycles+=3;
                bit(zpg());
                break;
            case Inst.BIT_ABS:
                cycles+=3;
                bit(abs());
                break;
            case Inst.BMI:
                cycles+=2;
                branch(P.S);
                break;
            case Inst.BNE:
                cycles+=2;
                branch(!P.Z);
                break;
            case Inst.BPL:
                cycles+=2;
                branch(!P.S);
                break;
            case Inst.BRK:
                cycles+=7;
                brk();
                break;
            case Inst.BVC:
                cycles+=2;
                branch(!P.V);
                break;
            case Inst.BVS:
                cycles+=2;
                branch(P.V);
                break;
            case Inst.CLC:
                cycles+=2;
                P.C = false;
                break;
            case Inst.CLD:
                cycles+=2;
                P.D = false;
                break;
            case Inst.CLI:
                cycles+=2;
                P.I = false;
                break;
            case Inst.CLV:
                cycles+=2;
                P.I = false;
                break;

            // CMP
            case Inst.CMP_PRE:
                cycles+=6;
                compare(A, pre());
                break;
            case Inst.CMP_ZPG:
                cycles+=3;
                compare(A, zpg());
                break;
            case Inst.CMP_IMM:
                cycles+=2;
                compare(A, imm());
                break;
            case Inst.CMP_ABS:
                cycles+=4;
                compare(A, abs());
                break;
            case Inst.CMP_POS:
                cycles+=5;
                compare(A, pos(true));
                break;
            case Inst.CMP_ZPGX:
                cycles+=4;
                compare(A, zpgIndx(X));
                break;
            case Inst.CMP_ABSY:
                cycles+=4;
                compare(A, indx(Y, true));
                break;
            case Inst.CMP_ABSX:
                cycles+=4;
                compare(A, indx(X, true));
                break;

            // CPX
            case Inst.CPX_IMM:
                cycles+=2;
                compare(X, imm());
                break;
            case Inst.CPX_ZPG:
                cycles+=3;
                compare(X, zpg());
                break;
            case Inst.CPX_ABS:
                cycles+=4;
                compare(X, abs());
                break;

            // CPY
            case Inst.CPY_IMM:
                cycles+=2;
                compare(Y, imm());
                break;
            case Inst.CPY_ZPG:
                cycles+=3;
                compare(Y, zpg());
                break;
            case Inst.CPY_ABS:
                cycles+=4;
                compare(Y, abs());
                break;

            // DEC
            case Inst.DEC_ZPG:
                cycles+=5;
                inc(0xFF, zpg());
                break;
            case Inst.DEC_ABS:
                cycles+=6;
                inc(0xFF, abs());
                break;
            case Inst.DEC_ZPGX:
                cycles+=6;
                inc(0xFF, zpg());
                break;
            case Inst.DEC_ABSX:
                cycles+=7;
                inc(0xFF, indx(X, false));
                break;

            case Inst.DEX:
                cycles+=2;
                inx(0xFF);
                break;
            case Inst.DEY:
                cycles+=2;
                iny(0xFF);
                break;

            // EOR
            case Inst.EOR_PRE:
                cycles+=6;
                eor(pre());
                break;
            case Inst.EOR_ZPG:
                cycles+=3;
                eor(zpg());
                break;
            case Inst.EOR_IMM:
                cycles+=2;
                eor(imm());
                break;
            case Inst.EOR_ABS:
                cycles+=4;
                eor(abs());
                break;
            case Inst.EOR_POS:
                cycles+=5;
                eor(pos(true));
            case Inst.EOR_ZPGX:
                cycles+=4;
                eor(zpgIndx(X));
                break;
            case Inst.EOR_ABSY:
                cycles+=4;
                eor(indx(Y, true));
                break;
            case Inst.EOR_ABSX:
                cycles+=4;
                adc(indx(X, true));
                break;

            // INC
            case Inst.INC_ZPG:
                cycles+=5;
                inc(0x01, zpg());
                break;
            case Inst.INC_ABS:
                cycles+=6;
                inc(0x01, abs());
                break;
            case Inst.INC_ZPGX:
                cycles+=6;
                inc(0x01, zpg());
                break;
            case Inst.INC_ABSX:
                cycles+=7;
                inc(0x01, indx(X, false));
                break;

            case Inst.INX:
                cycles+=2;
                inx(0x01);
                break;
            case Inst.INY:
                cycles+=2;
                iny(0x01);
                break;

            // JMP
            case Inst.JMP_ABS:
                cycles+=3;
                jmp(abs());
                break;
            case Inst.JMP_IND:
                cycles+=5;
                jmp(ind());
                break;

            case Inst.JSR:
                cycles+=6;
                jsr(abs());
                break;

            // LDA
            case Inst.LDA_PRE:
                cycles+=6;
                lda(pre());
                break;
            case Inst.LDA_ZPG:
                cycles+=3;
                lda(zpg());
                break;
            case Inst.LDA_IMM:
                cycles+=2;
                lda(imm());
                break;
            case Inst.LDA_ABS:
                cycles+=4;
                lda(abs());
                break;
            case Inst.LDA_POS:
                cycles+=5;
                lda(pos(true));
                break;
            case Inst.LDA_ZPGX:
                cycles+=4;
                lda(zpgIndx(X));
                break;
            case Inst.LDA_ABSY:
                cycles+=4;
                lda(indx(Y, true));
                break;
            case Inst.LDA_ABSX:
                cycles+=4;
                lda(indx(X, true));
                break;

            // LDX
            case Inst.LDX_IMM:
                cycles+=2;
                ldx(imm());
                break;
            case Inst.LDX_ZPG:
                cycles+=3;
                ldx(zpg());
                break;
            case Inst.LDX_ABS:
                cycles+=4;
                ldx(abs());
                break;
            case Inst.LDX_ZPGY:
                cycles+=4;
                ldx(zpgIndx(Y));
                break;
            case Inst.LDX_ABSY:
                cycles+=4;
                ldx(indx(Y, true));
                break;

            // LDY
            case Inst.LDY_IMM:
                cycles+=2;
                ldy(imm());
                break;
            case Inst.LDY_ZPG:
                cycles+=3;
                ldy(zpg());
                break;
            case Inst.LDY_ABS:
                cycles+=4;
                ldy(abs());
                break;
            case Inst.LDY_ZPGX:
                cycles+=4;
                ldy(zpgIndx(X));
                break;
            case Inst.LDY_ABSX:
                cycles+=4;
                ldy(indx(X, false));
                break;

            // LSR
            case Inst.LSR_ZPG:
                cycles+=5;
                address = zpg();
                memory.write(lsr(memory.read(address)), address);
                break;
            case Inst.LSR_ACC:
                cycles+=2;
                A = lsr(A);
                break;
            case Inst.LSR_ABS:
                cycles+=6;
                address = abs();
                memory.write(lsr(memory.read(address)), address);
                break;
            case Inst.LSR_ZPGX:
                cycles+=6;
                address = zpgIndx(X);
                memory.write(lsr(memory.read(address)), address);
                break;
            case Inst.LSR_ABSX:
                cycles+=7;
                address = zpgIndx(X);
                memory.write(lsr(memory.read(address)), address);
                break;

            case Inst.NOP:
                cycles+=2;
                break;

            // ORA
            case Inst.ORA_PRE:
                cycles+=6;
                ora(pre());
                break;
            case Inst.ORA_ZPG:
                cycles+=3;
                ora(zpg());
                break;
            case Inst.ORA_IMM:
                cycles+=2;
                ora(imm());
                break;
            case Inst.ORA_ABS:
                cycles+=4;
                ora(abs());
                break;
            case Inst.ORA_POS:
                cycles+=5;
                ora(pos(true));
                break;
            case Inst.ORA_ZPGX:
                cycles+=4;
                ora(zpgIndx(X));
                break;
            case Inst.ORA_ABSY:
                cycles+=4;
                ora(indx(Y, true));
                break;
            case Inst.ORA_ABSX:
                cycles+=4;
                ora(indx(X, true));
                break;

            case Inst.PHA:
                cycles+=3;
                ph(A);
                break;
            case Inst.PHP:
                cycles+=3;
                ph(P.getStatus());
                break;
            case Inst.PLA:
                cycles+=4;
                A = pl();
                P.setZSFlags(A);
                break;
            case Inst.PLP:
                cycles+=4;
                P.setStatus(pl());
                break;

            // ROL
            case Inst.ROL_ZPG:
                cycles+=5;
                address = zpg();
                memory.write(rol(memory.read(address)), address);
                break;
            case Inst.ROL_ACC:
                cycles+=2;
                A = rol(A);
                break;
            case Inst.ROL_ABS:
                cycles+=6;
                address = abs();
                memory.write(rol(memory.read(address)), address);
                break;
            case Inst.ROL_ZPGX:
                cycles+=6;
                address = zpgIndx(X);
                memory.write(rol(memory.read(address)), address);
                break;
            case Inst.ROL_ABSX:
                cycles+=7;
                address = zpgIndx(X);
                memory.write(rol(memory.read(address)), address);
                break;

            // ROR
            case Inst.ROR_ZPG:
                cycles+=5;
                address = zpg();
                memory.write(ror(memory.read(address)), address);
                break;
            case Inst.ROR_ACC:
                cycles+=2;
                A = rol(A);
                break;
            case Inst.ROR_ABS:
                cycles+=6;
                address = abs();
                memory.write(ror(memory.read(address)), address);
                break;
            case Inst.ROR_ZPGX:
                cycles+=6;
                address = zpgIndx(X);
                memory.write(ror(memory.read(address)), address);
                break;
            case Inst.ROR_ABSX:
                cycles+=7;
                address = zpgIndx(X);
                memory.write(ror(memory.read(address)), address);
                break;

            case Inst.RTI:
                cycles+=6;
                rti();
                break;
            case Inst.RTS:
                cycles+=6;
                rts();
                break;

            // SBC
            case Inst.SBC_PRE:
                cycles+=6;
                sbc(pre());
                break;
            case Inst.SBC_ZPG:
                cycles+=3;
                sbc(zpg());
                break;
            case Inst.SBC_IMM:
                cycles+=2;
                sbc(imm());
                break;
            case Inst.SBC_ABS:
                cycles+=4;
                sbc(abs());
                break;
            case Inst.SBC_POS:
                cycles+=5;
                sbc(pos(true));
                break;
            case Inst.SBC_ZPGX:
                cycles+=4;
                sbc(zpgIndx(X));
                break;
            case Inst.SBC_ABSY:
                cycles+=4;
                sbc(indx(Y, true));
                break;
            case Inst.SBC_ABSX:
                cycles+=4;
                sbc(indx(X, true));
                break;

            case Inst.SEC:
                cycles+=2;
                P.C = true;
                break;
            case Inst.SED:
                // Decimal mode is disabled in NES
                // ADC and SBC instructions will not be affected as
                // decimal mode is not supported
                cycles+=2;
                P.D = true;
                break;
            case Inst.SEI:
                cycles+=2;
                P.I = true;
                break;

            // STA
            case Inst.STA_PRE:
                cycles+=6;
                st(pre(), A);
                break;
            case Inst.STA_ZPG:
                cycles+=3;
                st(zpg(), A);
                break;
            case Inst.STA_ABS:
                cycles+=4;
                st(abs(), A);
                break;
            case Inst.STA_POS:
                cycles+=6;
                // Extra cycle is false as its always an extra cycle for STA
                // than other instructions
                st(pos(false), A);
                break;
            case Inst.STA_ZPGX:
                cycles+=4;
                st(zpgIndx(X), A);
                break;
            case Inst.STA_ABSY:
                cycles+=5;
                st(indx(Y, false), A);
                break;
            case Inst.STA_ABSX:
                cycles+=5;
                st(indx(X, false), A);
                break;

            // STX
            case Inst.STX_ZPG:
                cycles+=3;
                st(zpg(), X);
                break;
            case Inst.STX_ABS:
                cycles+=4;
                st(abs(), X);
                break;
            case Inst.STX_ZPGY:
                cycles+=4;
                st(zpgIndx(Y), X);
                break;

            // STY
            case Inst.STY_ZPG:
                cycles+=3;
                st(zpg(), Y);
                break;
            case Inst.STY_ABS:
                cycles+=4;
                st(abs(), Y);
                break;
            case Inst.STY_ZPGX:
                cycles+=4;
                st(zpgIndx(X), Y);
                break;

            case Inst.TAX:
                cycles+=2;
                X = A;
                P.setZSFlags(X);
                break;
            case Inst.TAY:
                cycles+=2;
                Y = A;
                P.setZSFlags(Y);
                break;
            case Inst.TSX:
                cycles+=2;
                X = memory.read(SP);
                P.setZSFlags(X);
                break;
            case Inst.TXA:
                cycles+=2;
                A = X;
                P.setZSFlags(A);
                break;
            case Inst.TXS:
                cycles+=2;
                memory.write(X, SP);
                break;
            case Inst.TYA:
                cycles+=2;
                A = Y;
                P.setZSFlags(A);
                break;

            default:
                throw new RuntimeException("Unrecognized instruction");
        }
    }

    public void start() {
        reset();

        while(true) {
            // TODO - Implement
            // increment cycles
            // execute OP_CODE code

            execute();
        }
    }

    /**
     * Reset the CPU to an initial state
     */
    public void reset() {
        cycles = 0;

        A = 0;
        X = 0;
        Y = 0;
        SP = 0x01FF;
        P = new Flags();

        // Must point to reset vector which is at index
        // 0xFFFD (High) and 0xFFFC (low)
        PC = memory.read(0xFFFC) | (memory.read(0xFFFD) << 8);
    }

    public int getA() {
        return A;
    }

    public int getPC() {
        return PC;
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

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public Flags getFlags() {
        return P;
    }

    //region op code related methods
    // Add data to accumulator with carry
    public void adc(int address) {
        int value = memory.read(address);
        int result = A + value + (P.C ? 1 : 0);

        result = P.setCFlag(result);
        P.setVFlag(A, value, result);
        A = result;
        P.setZSFlags(A);
    }

    // And data with accumulator
    public void and(int address) {
        int value = memory.read(address);
        A = A & value;
        P.setZSFlags(A);
    }

    // Shift accumulator or data byte left
    public int asl(int value) {
        value = value << 1;

        value =  P.setCFlag(value);
        P.setZSFlags(value);

        return value;
    }

    // And accumulator with data
    public void bit(int address) {
        int value = memory.read(address);
        int result = A & value;

        P.setZFlag(result);
        P.setSFlag(value);
        // Set overflow depending on the 6th bit of the data content
        P.V = (value & 0x40) == 0x40 ? true : false;
    }

    // Force Break (Software Interrupt)
    public void brk() {
        PC+=2;
        memory.write(PC >> 8, getSP());
        decSP();
        memory.write(PC & 0xFF, getSP());
        decSP();
        memory.write(P.getStatus(), getSP());
        decSP();
        PC = memory.read(0xFFFE) | (memory.read(0xFFFF) << 8);
        P.I = true;
    }

    // increment data (by value)
    public void inc(int value, int address) {
        int result = (memory.read(address) + value) & 0xFF;
        memory.write(result, address);
        P.setZSFlags(result);
    }

    // increment X register (by value)
    public void inx(int value) {
        X = (X + value) & 0xFF;
        P.setZSFlags(X);
    }

    // decrement Y register (by 1)
    public void iny(int value) {
        Y = (Y + value) & 0xFF;
        P.setZSFlags(Y);
    }

    // Xor accumulator with data
    public void eor(int address) {
        int value = memory.read(address);
        A = (A ^ value) & 0xFF;
        P.setZSFlags(A);
    }

    // Jump to absolute or indirect address
    public void jmp(int address) {
        PC = address;
    }

    // Jump to Subroutine
    public void jsr(int address) {
        // It is expected that the jsr will store the address of the third byte
        PC--;
        memory.write(PC >> 8, getSP());
        decSP();
        memory.write(PC & 0xFF, getSP());
        decSP();
        PC = address;
    }

    // Load Accumulator from Memory
    public void lda(int address) {
        int value = memory.read(address);
        A = value;
        P.setZSFlags(A);
    }

    // Load Index Register X from Memory
    public void ldx(int address) {
        int value = memory.read(address);
        X = value;
        P.setZSFlags(X);
    }

    // Load Index Register Y from Memory
    public void ldy(int address) {
        int value = memory.read(address);
        Y = value;
        P.setZSFlags(Y);
    }

    // Logical Shift Right of Accumulator or Memory
    public int lsr(int value) {
        int lowBit = value & 0x1;
        value = value >> 1;
        P.C = lowBit == 0x1;
        P.setZFlag(value);
        P.S = false;

        return value;
    }

    // Logically OR Memory with Accumulator
    public void ora(int address) {
        int value = memory.read(address);
        A = A | value;
        P.setZSFlags(A);
    }

    // Push value into Stack
    public void ph(int value) {
        memory.write(value, getSP());
        decSP();
    }

    // Pull content from Stack
    public int pl() {
        incSP();
        return memory.read(getSP());
    }

    // Rotate Accumulator or Memory Left through Carry
    public int rol(int value) {
        value = (value << 1) | (P.C ? 1 : 0);
        P.C = (value & 0x100) == 0x100;
        value = value & 0xFF;
        P.setZSFlags(value);

        return value;
    }

    // Rotate Accumulator or Memory Right through Carry
    public int ror(int value) {
        int lowBit = value & 0x01;
        value = (P.C ? 0x80 : 0) | (value >> 1);
        P.C = (lowBit & 0x01) == 0x01;
        P.setZSFlags(value);

        return value;
    }

    // Return from Interrupt
    public void rti() {
        incSP();
        P.setStatus(memory.read(getSP()));
        incSP();
        PC = memory.read(getSP());
        incSP();
        PC += (memory.read(getSP()) << 8);
    }

    // Return from Subroutine
    public void rts() {
        incSP();
        PC = memory.read(getSP());
        incSP();
        PC += (memory.read(getSP()) << 8);

        // The jsr stored the third byte as an address, so pc is
        // incremented to point to next next instruction after return
        PC++;
    }

    // Subtract Memory from Accumulator with Borrow
    public void sbc(int address) {
        // Same as ADC. The only difference is value is treated as
        // complement value. Code repeated to avoid stack call
        int value = 255 -  memory.read(address);
        int result = A + value + (P.C ? 1 : 0);

        result = P.setCFlag(result);
        P.setVFlag(A, value, result);
        A = result;
        P.setZSFlags(A);
    }

    // Store value in Memory
    public void st(int address, int value) {
        memory.write(value, address);
    }

    /**
     * Subtract data content from given compare value and set C, F and S
     * flags accordingly
     * @param cmp Value to compare
     * @param address data address of content to compare with
     */
    public void compare(int cmp, int address) {
        int value = memory.read(address);

        // Get 2's complements of value
        int result = cmp + ((value * -1) & 0xFF);
        P.setCFlag(result);
        P.setZFlag(result);
        P.setSFlag(result);
    }

    /**
     * Whether to branch execution
     * @param cond True if branching condition is met
     */
    public void branch(boolean cond) {
        PC++;
        if (!cond)
            return;

        byte value = (byte) memory.read(PC);

        int oldPC = PC;
        PC += value;
        cycles++;

        // When cross page occurs
        if( (oldPC & 0xFF00) != (PC & 0xFF00))
            cycles+=1;
    }

    // Address Modes

    /**
     * Immediate address
     * @return
     */
    private int imm() {
        return PC++;
    }

    /**
     * Absolue (direct) address
     * @return
     */
    private int abs() {
        // Read the first and second byte after instruction for data address
        return memory.read(PC++) | (memory.read(PC++) << 8);
    }

    /**
     * indirect address
     * @return
     */
    private int ind() {
        int low = memory.read(PC++);
        int high = memory.read(PC++);
        int address = low | (high << 8);
        int value = memory.read(address);
        // Fixes first operand (low byte) cross page boundary
        address = ( (low + 1) & 0xFF) | (high << 8);
        value+= memory.read(address) << 8;

        return value;
    }

    /**
     * Zero page address
     * @return
     */
    private int zpg() {
        // Read first byte only after instruction for data address
        return memory.read(PC++);
    }

    /**
     * Pre-Index Indirect Address
     * @return
     */
    private int pre() {
        // X + first byte after instruction as data address to access first 256 bytes of data, which
        // wraparound when addition is overflowed. The Index and next byte (within zero page) is considered
        // the 16 bit address
        int index = (X + memory.read(PC++)) & 0xFF;

        return index | ( ( (index + 1) & 0xFF) << 8) ;
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
        int address = memory.read(PC++) | (memory.read(PC++) << 8);

        if( extra &&
                ((address & 0xFF00) != ((address + Y) & 0xFF00)))
            cycles++;

        return address + Y;
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
        int address = memory.read(PC++) | (memory.read(PC++) << 8);

        if ( extra && (address & 0xFF00) != ((address + Y) & 0xFF00) )
            cycles++;

        return (register + address) & 0xFFFF;
    }

    /**
     * Zero page indexed Addressing.
     * @param register X or Y register value to sum address with
     * @return
     */
    private int zpgIndx(int register) {
        // Crossing page boundary is not to be handled
        return (register + memory.read(PC++)) & 0xFF;
    }

    //endregion

    public int getCycles() {
        return cycles;
    }
}
