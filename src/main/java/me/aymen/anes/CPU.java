package me.aymen.anes;

public class CPU {

    // CPU Components
    private RAM ram;
    private int cycles;

    // Registers
    private int A;          // Accumulator
    private int X;          // X Index
    private int Y;          // Y Index
    private int PC;         // Program Counter
    private int SP;         // Stack Pointer
    private Flags P;        // Process Status

    public CPU() {
        init();
    }

    /**
     * Execute instruction at current program counter
     */
    public void execute() {

        int address = 0;

        switch (ram.memory[PC]) {

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
                adc(pos());
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
                and(pos());
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
                ram.memory[address] = asl(ram.memory[zpg()]);
                break;
            case Inst.ASL_ACC:
                cycles+=2;
                A = asl(A);
                break;
            case Inst.ASL_ABS:
                cycles+=6;
                address = abs();
                ram.memory[address] = asl(ram.memory[address]);
                break;
            case Inst.ASL_ZPGX:
                cycles+=6;
                address = zpgIndx(X);
                ram.memory[address] = asl(ram.memory[address]);
                break;
            case Inst.ASL_ABSX:
                cycles+=7;
                address = zpgIndx(X);
                ram.memory[address] = asl(ram.memory[address]);
                break;

            case Inst.BCC:
                cycles+=2;
                _branch(!P.C);
                break;
            case Inst.BCS:
                cycles+=2;
                _branch(P.C);
                break;
            case Inst.BEQ:
                cycles+=2;
                _branch(P.Z);
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
                _branch(P.S);
                break;
            case Inst.BNE:
                cycles+=2;
                _branch(!P.Z);
                break;
            case Inst.BPL:
                cycles+=2;
                _branch(!P.S);
                break;
            case Inst.BRK:
                cycles+=7;
                brk();
                break;
            case Inst.BVC:
                cycles+=2;
                _branch(!P.V);
                break;
            case Inst.BVS:
                cycles+=2;
                _branch(P.V);
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
                _compare(A, pre());
                break;
            case Inst.CMP_ZPG:
                cycles+=3;
                _compare(A, zpg());
                break;
            case Inst.CMP_IMM:
                cycles+=2;
                _compare(A, imm());
                break;
            case Inst.CMP_ABS:
                cycles+=4;
                _compare(A, abs());
                break;
            case Inst.CMP_POS:
                cycles+=5;
                _compare(A, pos());
                break;
            case Inst.CMP_ZPGX:
                cycles+=4;
                _compare(A, zpgIndx(X));
                break;
            case Inst.CMP_ABSY:
                cycles+=4;
                _compare(A, indx(Y, true));
                break;
            case Inst.CMP_ABSX:
                cycles+=4;
                _compare(A, indx(X, true));
                break;

            // CPX
            case Inst.CPX_IMM:
                cycles+=2;
                _compare(X, imm());
                break;
            case Inst.CPX_ZPG:
                cycles+=3;
                _compare(X, zpg());
                break;
            case Inst.CPX_ABS:
                cycles+=4;
                _compare(X, abs());
                break;

            // CPY
            case Inst.CPY_IMM:
                cycles+=2;
                _compare(Y, imm());
                break;
            case Inst.CPY_ZPG:
                cycles+=3;
                _compare(Y, zpg());
                break;
            case Inst.CPY_ABS:
                cycles+=4;
                _compare(Y, abs());
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
                eor(pos());
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
                lda(pos());
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

            default:
                throw new RuntimeException("Unrecognized instruction");
        }
    }

    public void start() {

        while(true) {
            // TODO - Implement
            // increment cycles
            // execute OP_CODE code
        }
    }

    public void init() {
        ram = new RAM();
        cycles = 0;

        A = 0;
        X = 0;
        Y = 0;
        PC = 0;
        SP = 0x01FF;
        P = new Flags();
    }

    public int getA() {
        return A;
    }

    public int getPC() {
        return PC;
    }

    public void setRam(RAM ram) {
        this.ram = ram;
    }

    public Flags getFlags() {
        return P;
    }

    // Add memory to accumulator with carry
    public void adc(int address) {
        int value = ram.read(address);
        int result = A + value + (P.C ? 1 : 0);

        result = P.setCFlag(result);
        P.setVFlag(A, value, result);
        A = result;
        P.setZSFlags(A);
    }

    // And memory with accumulator
    public void and(int address) {
        int value = ram.read(address);
        A = A & value;
        P.setZSFlags(A);
    }

    // Shift accumulator or memory byte left
    public int asl(int value) {
        value = value << 1;

        value =  P.setCFlag(value);
        P.setZSFlags(value);

        return value;
    }

    // Branch when zero flag is set
    public void beq() {
        PC++;
        _branch(P.Z);
    }

    // And accumulator with memory
    public void bit(int address) {
        int value = ram.memory[address];
        int result = A & value;

        P.setZFlag(result);
        P.setSFlag(value);
        // Set overflow depending on the 6th bit of the memory content
        P.V = (value & 0x40) == 0x40 ? true : false;
    }

    // Force Break (Software Interrupt)
    public void brk() {
        PC+=2;
        ram.memory[SP] = PC >> 8;
        ram.memory[SP - 1] = PC & 0xFF;
        ram.memory[SP - 2] = P.getStatus();
        SP = SP - 3;
        PC = ram.memory[0xFFFE] | (ram.memory[0xFFFF] << 8);
        P.I = true;
    }

    // increment memory (by value)
    public void inc(int value, int address) {
        int result = (ram.memory[address] + value) & 0xFF;
        ram.memory[address] = result;
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

    // Xor accumulator with memory
    public void eor(int address) {
        int value = ram.memory[address];
        A = (A ^ value) & 0xFF;
        P.setZSFlags(A);
    }

    // Jump to absolute or indirect address
    public void jmp(int address) {
        PC = address;
    }

    // Jump to Subroutine
    public void jsr(int address) {
        ram.memory[SP] = address >> 8;
        ram.memory[SP - 1] = address & 0xFF;
        SP = SP - 2;
        PC = address;
    }

    // Load Accumulator from Memory
    public void lda(int address) {
        int value = ram.read(address);
        A = value;
        P.setZSFlags(A);
    }

    // Load Index Register X from Memory
    public void ldx(int address) {
        int value = ram.read(address);
        X = value;
        P.setZSFlags(X);
    }

    // Load Index Register Y from Memory
    public void ldy(int address) {
        int value = ram.read(address);
        Y = value;
        P.setZSFlags(Y);
    }

    /**
     * Subtract memory content from given compare value and set C, F and S
     * flags accordingly
     * @param cmp Value to compare
     * @param address memory address of content to compare with
     */
    public void _compare(int cmp, int address) {
        int value = ram.memory[address];

        // Get 2's complements of value
        int result = cmp + ((value * -1) & 0xFF);
        P.setCFlag(result);
        P.setZFlag(result);
        P.setSFlag(result);
    }

    /**
     * Whether to _branch execution
     * @param cond True if branching condition is met
     */
    public void _branch(boolean cond) {
        PC++;
        if (!cond)
            return;

        byte value = (byte) ram.memory[PC];
        PC++;
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
        // Read the first and second byte after instruction for ram address
        return ram.memory[PC++] | (ram.memory[PC++] << 8);
    }

    /**
     * indirect address
     * @return
     */
    private int ind() {
        int low = ram.memory[PC++];
        int high = ram.memory[PC++];
        int address = low | (high << 8);
        int value = ram.memory[address];
        // Fixes first operand (low byte) cross page boundary
        address = ( (low + 1) & 0xFF) | (high << 8);
        value+= ram.memory[address] << 8;

        return value;
    }

    /**
     * Zero page address
     * @return
     */
    private int zpg() {
        // Read first byte only after instruction for ram address
        return ram.memory[PC++];
    }

    /**
     * Pre-Index Indirect Address
     * @return
     */
    private int pre() {
        // X + first byte after instruction as ram address to access first 256 bytes of memory, which
        // wraparound when addition is overflowed. The Index and next byte (within zero page) is considered
        // the 16 bit address
        int index = (X + ram.memory[PC++]) & 0xFF;

        return index | ( ( (index + 1) & 0xFF) << 8) ;
    }

    /**
     * Post-Index Indirect Address (a.k.a. Indirect indexed addressing)
     * @return
     */
    private int pos() {
        // First byte after instruction the 16 bit address.
        // Index + Y is returned
        int address = ram.memory[PC++] | (ram.memory[PC++] << 8);

        if( (address & 0xFF00) != ((address + Y) & 0xFF00))
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
        int address = ram.memory[PC++] | (ram.memory[PC++] << 8);

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
        return (register + ram.memory[PC++]) & 0xFF;
    }
}
