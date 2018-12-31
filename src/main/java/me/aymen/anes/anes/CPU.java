package me.aymen.anes.anes;

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
            case Inst.ADC_ZPGX:
                cycles+=4;
                adc(zpgIndx(X));
            case Inst.ADC_ABSY:
                cycles+=4;
                adc(zpgIndx(Y));
            case Inst.ADC_ABSX:
                cycles+=4;
                adc(zpgIndx(X ));

            // AND

            default:
                throw new RuntimeException("Unrecognized instruction");
        }
        PC++;
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
        SP = 0;
        P = new Flags();
    }

    public int getA() {
        return A;
    }

    public void setRam(RAM ram) {
        this.ram = ram;
    }

    public Flags getFlags() {
        return P;
    }

    // Add memory to accumulator with carry
    public void adc(int addr) {
        int value = ram.read(addr);
        int result = A + value + (P.C ? 1 : 0);

        P.C = (result & 0x100) == 0x100 ? true : false;
        P.S = (result & 0x80) == 0x80 ? true : false;
        P.Z = (result | 0) == 0 ? true : false;

        // If the 8th bit of A and value is 0 and result is 1
        // or if 8th bit of A and value is 1 and result is 0,
        // this will indicate an overflow
        P.V = ((A ^ result) & (value ^ result) & 0x80) != 0x0;

        A = result & 0xFF;
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
     * Absolue address
     * @return
     */
    private int abs() {
        // Read the first and second byte after instruction for ram address
        return ram.memory[PC++] | (ram.memory[PC++] << 8);
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
        int address = ram.memory[PC++] | ( ram.memory[PC++] << 8);

        if( (address >> 8 + Y) > 0xFF)
            cycles++;

        return address + Y;
    }

    /**
     * Absolute Indexed Addressing.
     * @param register X or Y register value to sum address with     *
     * @return
     */
    private int indx(int register) {
        // First and second byte + register as a 16 bit address
        int address = (ram.memory[PC++] | ram.memory[PC++] << 8);

        if ( (address >> 8 + register) > 0xFF )
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
