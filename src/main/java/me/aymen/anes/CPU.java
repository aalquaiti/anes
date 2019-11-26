package me.aymen.anes;

import me.aymen.anes.memory.Bus;
import me.aymen.anes.util.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.aymen.anes.AddressMode.*;

public class CPU {
    private static Logger logger = LoggerFactory.getLogger(CPU.class);

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
        addressMode[ZPGY] = this::zpgy;
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
        opcodes[0x5D] = new Inst("EOR", 4, ABSX, this::eor);
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
        opcodes[0x85] = new Inst("STA", 3, ZPG, this::sta);
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

        // 0xD#
        opcodes[0xD0] = new Inst("BNE", 2, REL, this::bne);
        opcodes[0xD1] = new Inst("CMP", 5, INDY, this::cmp);
        opcodes[0xD2] = null;
        opcodes[0xD3] = null;
        opcodes[0xD4] = null;
        opcodes[0xD5] = new Inst("CMP", 4, ZPGX, this::cmp);
        opcodes[0xD6] = new Inst("DEC", 6, ZPGX, this::dec);
        opcodes[0xD7] = null;
        opcodes[0xD8] = new Inst("CLD", 2, IMPL, this::cld);
        opcodes[0xD9] = new Inst("CMP", 4, ABSY, this::cmp);
        opcodes[0xDA] = null;
        opcodes[0xDB] = null;
        opcodes[0xDC] = null;
        opcodes[0xDD] = new Inst("CMP", 4, ABSX, this::cmp);
        opcodes[0xDE] = new Inst("DEC", 7, ABSX_PLUS, this::dec);
        opcodes[0xDF] = null;

        // 0xE#
        opcodes[0xE0] = new Inst("CPX", 2, IMM, this::cpx);
        opcodes[0xE1] = new Inst("SBC", 6, INDX, this::sbc);
        opcodes[0xE2] = null;
        opcodes[0xE3] = null;
        opcodes[0xE4] = new Inst("CPX", 3, ZPG, this::cpx);
        opcodes[0xE5] = new Inst("SBC", 3, ZPG, this::sbc);
        opcodes[0xE6] = new Inst("INC", 5, ZPG, this::inc);
        opcodes[0xE7] = null;
        opcodes[0xE8] = new Inst("INX", 2, IMPL, this::inx);
        opcodes[0xE9] = new Inst("SBC", 2, IMM, this::sbc);
        opcodes[0xEA] = new Inst("NOP", 2, IMPL, this::nop);
        opcodes[0xEB] = null;
        opcodes[0xEC] = new Inst("CPX", 4, ABS, this::cpx);
        opcodes[0xED] = new Inst("SBC", 4, ABS, this::sbc);
        opcodes[0xEE] = new Inst("INC", 6, ABS, this::inc);
        opcodes[0xEF] = null;

        // 0xF#
        opcodes[0xF0] = new Inst("BEQ", 2, REL, this::beq);
        opcodes[0xF1] = new Inst("SBC", 5, INDY, this::sbc);
        opcodes[0xF2] = null;
        opcodes[0xF3] = null;
        opcodes[0xF4] = null;
        opcodes[0xF5] = new Inst("SBC", 4, ZPGX, this::sbc);
        opcodes[0xF6] = new Inst("INC", 6, ZPGX, this::inc);
        opcodes[0xF7] = null;
        opcodes[0xF8] = new Inst("SED", 2, IMPL, this::sed);
        opcodes[0xF9] = new Inst("SBC", 4, ABSY, this::sbc);
        opcodes[0xFA] = null;
        opcodes[0xFB] = null;
        opcodes[0xFC] = null;
        opcodes[0xFD] = new Inst("SBC", 4, ABSX, this::sbc);
        opcodes[0xFE] = new Inst("INC", 7, ABSX_PLUS, this::inc);
        opcodes[0xFF] = null;

        // Unofficial opcodes
        opcodes[0x04] = new Inst("*NOP", 3, ZPG, ()-> {});
        opcodes[0x0C] = new Inst("*NOP", 4, ABS, ()-> {});
        opcodes[0x14] = new Inst("*NOP", 4, ZPGX, ()-> {});
        opcodes[0x1A] = new Inst("*NOP", 2, IMPL, ()-> {});
        opcodes[0x1C] = new Inst("*NOP", 4, ABSX, ()-> {});
        opcodes[0x34] = new Inst("*NOP", 4, ZPGX, ()-> {});
        opcodes[0x3A] = new Inst("*NOP", 2, IMPL, ()-> {});
        opcodes[0x3C] = new Inst("*NOP", 4, ABSX, ()-> {});
        opcodes[0x44] = new Inst("*NOP", 3, ZPG, ()-> {});
        opcodes[0x54] = new Inst("*NOP", 4, ZPGX, ()-> {});
        opcodes[0x5A] = new Inst("*NOP", 2, IMPL, ()-> {});
        opcodes[0x5C] = new Inst("*NOP", 4, ABSX, ()-> {});
        opcodes[0x64] = new Inst("*NOP", 3, ZPG, ()-> {});
        opcodes[0x74] = new Inst("*NOP", 4, ZPGX, ()-> {});
        opcodes[0x7A] = new Inst("*NOP", 2, IMPL, ()-> {});
        opcodes[0x7C] = new Inst("*NOP", 4, ABSX, ()-> {});
        opcodes[0x80] = new Inst("*NOP", 2, IMM, ()-> {});
        opcodes[0x83] = new Inst("*SAX", 6, INDX, this::_sax);
        opcodes[0x87] = new Inst("*SAX", 3, ZPG, this::_sax);
        opcodes[0x8F] = new Inst("*SAX", 4, ABS, this::_sax);
        opcodes[0x97] = new Inst("*SAX", 4, ZPGY, this::_sax);
        opcodes[0xA3] = new Inst("*LAX", 6, INDX, this::_lax);
        opcodes[0xA7] = new Inst("*LAX", 3, ZPG, this::_lax);
        opcodes[0xAF] = new Inst("*LAX", 4, ABS, this::_lax);
        opcodes[0xB3] = new Inst("*LAX", 5, INDY, this::_lax);
        opcodes[0xB7] = new Inst("*LAX", 4, ZPGY, this::_lax);
        opcodes[0xBF] = new Inst("*LAX", 4, ABSY, this::_lax);
        opcodes[0xD4] = new Inst("*NOP", 4, ZPGX, ()-> {});
        opcodes[0xDA] = new Inst("*NOP", 2, IMPL, ()-> {});
        opcodes[0xDC] = new Inst("*NOP", 4, ABSX, ()-> {});
        opcodes[0xEB] = new Inst("*SBC", 2, IMM, this::sbc);
        opcodes[0xF4] = new Inst("*NOP", 4, ZPGX, ()-> {});
        opcodes[0xFA] = new Inst("*NOP", 2, IMPL, ()-> {});
        opcodes[0xFC] = new Inst("*NOP", 4, ABSX, ()-> {});
    }

    /**
     * Represents execution one opcode statement
     * @return String representation of the operation performed in Assembly
     * Code
     */
    public CPUStatus tick() {
        // Reset values. Set to -1 to indicate it was not set
        op1 = -1;
        op2 = -1;
        address = -1;
        value = 0;

        // Read the current PC then increment it
        int currentPC = incPC();
        int currentCycles = cycles;

        // Retrieve the operation mnemonic
        int op = bus.read(currentPC);

        if (currentPC == 0xE7E6) {
            System.out.println("What up!");
        }

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
        if (opcode == null)
            logger.error(
                    String.format("Detected unsupported opcode: $%02X", op));
        addressMode[opcode.mode].process();
        opcode.operation.process();
        cycles += opcode.cycles;

        // Retrieve status after each tick
        CPUStatus status = new CPUStatus();
        status.PC = currentPC;
        status.op = op;
        status.op1 = op1;
        status.op2 = op2;
        status.opcode = opcode;
        status.A = A;
        status.X = X;
        status.Y = Y;
        status.P = P;
        status.SP = SP;
        status.cycle = cycles - currentCycles;
        status.cycleCount = cycles;

        return status;
    }

    /**
     * Executes ticks indefinitely
     */
    public void start() {
        while(true) {
            tick();
        }
    }

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
        SP = 0xFF;
        P = new Flags();
        P.setStatus(0x24);

        // Must point to reset vector which is at index
        // 0xFFFD (High) and 0xFFFC (low)
        PC = bus.read(0xFFFC) | (bus.read(0xFFFD) << 8);
        // TODO check that cycles is increased due to a fetch operation
        cycles+=7;
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
        SP =  (SP + 1) & 0xFF;
        return SP + 0x100;
    }

    /**
     * Decrement the stack pointer by one.
     * The stack will always be in range 0x100 to 0x1FF, so any overflow
     * will lead to wrapping around the range.
     * @return previous SP value before decrement
     */
    public int decSP() {
        int previous = SP + 0x100;
        SP =  (SP - 1) & 0xFF;
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
        P.setZNFlags(A);
    }

    /**
     * Logical And memory with accumulator
     */
    public void and() {
        A = A & value;
        P.setZNFlags(A);
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
     * Branch if Equal
     */
    public void beq() {
        branch(P.Z);
    }

    /**
     * Branch on Plus.
     * Branches if Positive
     */
    public void bpl() {
        branch(!P.N);
    }
//

    /**
     * And accumulator with memory
     */
    public void bit() {
        int result = A & value;
        P.setZFlag(result);
        P.setNFlag(value);
        // Set overflow depending on the 6th bit of the memory content
        P.V = (value & 0x40) == 0x40;
    }

    /**
     * Branch if Minus
     */
    public void bmi() {
        branch(P.N);
    }

    /**
     * Branch if Not Equal
     */
    public void bne() {
        branch(!P.Z);
    }

    /**
     * Force Break (Software Interrupt)
      */
    public void brk() {
        // Increment PC by one, so that when RTI is called, the returned
        // address will be brk + 2. One already increased when reading the
        // instruction, and one is done below
        incPC();
        P.B = true;
        // Write PC and processor status
        ph(PC >> 8);
        ph(PC & 0xFF);
        ph(P.getStatus());
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
     * Clear Decimal Mode
     */
    public void cld() {
        P.D = false;
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
     * Compare X Register
     */
    public void cpx() {
        compare(X);
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
        P.setZNFlags(A);
    }

    /**
     * Increment Memory Content by one
     */
    public void inc() {
        addMemory(1);
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
//        // Low byte
//        ph(PC & 0xFF);
//        // High byte
//        ph(PC >> 8);

        PC = address;
    }


    /**
     * Jump to Subroutine
     */
    public void jsr() {
        // TODO check if implementation is correct
        // It is expected that the jsr will store the address  (minus one)
        // to the stack then jump to given address
        // High byte
        decPC();
        ph(PC >> 8);
        // Low byte
        ph(PC & 0xFF);

        PC = address;
    }
//

    /**
     * Load Accumulator from memory
     */
    public void lda() {
        A = value;
        P.setZNFlags(A);
    }

    /**
     * Load X Register from Memory
     */
    public void ldx() {
        X = value;
        P.setZNFlags(X);
    }


    /**
     * Load Y Register from memory
     */
    public void ldy() {
        Y = value;
        P.setZNFlags(Y);
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

    /**
     * No Operation.
     */
    public void nop() {
        // I'm walkin' here
    }

    // Logically OR Memory content with Accumulator
    public void ora() {
        A = A | value;
        P.setZNFlags(A);
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
        // PHP pushes a copy of the Process status, setting bit 4 and 5 to 1
        // (i.e Flags B and U which is unused).
        // It does not change Process Status (P)
        // See: PLP and RTI
        int status = P.getStatus() | 0x10;
        ph(status);
    }

    /**
     * Pull Accumulator from stack
     */
    public void pla() {
        A = pl();
        P.setZNFlags(A);
    }

    /**
     * Pull Processor Status
     */
    public void plp() {
        // Ignores bit 4 and 5
        // Bit five is always ignored by default and set to 1
        // See: PHP
        int status = pl() & 0xEF;
        P.setStatus(status);
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
        // Ignore bit 4 and 5
        // See: PHP
        int status = pl() & 0xE7;
        P.setStatus(status);
        PC = pl();
        PC += (pl() << 8);
    }
//

    /**
     * Return from Subroutine
     */
    public void rts() {
        // Program Counter is pulled from stack
        PC = pl();
        PC += (pl() << 8);
        // See: JSR
        incPC();
    }

    /**
     * Subtract Memory content to Accumulator with Carry
     */
    public void sbc() {
        // Same as ADC. The only difference is value is treated as
        // complement value. Code repeated to avoid stack call
        value = 255 -  value;
        int result = A + value + (P.C ? 1 : 0);

        result = P.setCFlag(result);
        P.setVFlag(A, value, result);
        A = result;
        P.setZNFlags(A);
    }

    /**
     * Set Carry Flag
     */
    public void sec() {
        P.C = true;
    }

    /**
     * Set Decimal Flag
     */
    public void sed() {
        // Decimal mode is disabled in NES
        // ADC and SBC instructions will not be affected as
        // decimal mode is not supported
        P.D = true;
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
        P.setZNFlags(A);
    }

    /**
     * Transfer X to Stack Pointer
     */
    public void txs() {
        SP = X;
    }

    /**
     * Transfer Accumulator to X Register
     */
    public void tax() {
        X = A;
        P.setZNFlags(X);
    }

    /**
     * Transfer Accumulator to Y Register
     */
    public void tay() {
        Y = A;
        P.setZNFlags(Y);
    }

    /**
     * Transfer Stack Pointer to X
     */
    public void tsx() {
        X = SP;
        P.setZNFlags(X);
    }

    /**
     * Transfer Y to Accumulator;
     */
    public void tya() {
        A = Y;
        P.setZNFlags(A);
    }

    //endregion

    //region unofficial opcodes methods

    /**
     * Load Accumulator and X Register from Memory
     */
    public void _lax() {
        A = value;
        X = value;
        P.setZNFlags(value);
    }

    /**
     * Stores the logical AND the Accumulator and X Register
     */
    public void _sax() {
        // No flags are affected
        bus.write(A & X, address);
    }

    //end region

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
        address = incPC();
        op1 = bus.read(address);
        value = op1;
    }

    /**
     * Zero page address
     * @return
     */
    private void zpg() {
        // Read first byte only after instruction for memory address
        op1 = bus.read(incPC());
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
        // Read as byte as its a signed value
        value = (byte) op1;
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
        // The least significant address to access is basically stored at value
        // op1 + X (wrapped within zero page)
        // The low and next byte (also wrapped within zero page) 's value
        // is the 16 bit address, which is then used to retrieve value
        op1 = bus.read(incPC());
        int low = (op1 + X) & 0xFF;
        int high = (low + 1) & 0xFF;
        address = buildAddress(bus.read(low), bus.read(high));
        value = bus.read(address);
    }

    /**
     * Indirect Y. Also known as Indirect Indexed.
     * Adds a cycle if cross page occurs
     */
    private void indy() {
        // Indirect is calculated by reading the value of the zero page
        // address, then Y is added to it.
        // The result is the least significant byte of the indirect address.
        // The addition's carry (if any) is added to the next zero page address
        op1 = bus.read(incPC());
        int low = bus.read(op1);
        int high = bus.read((op1 + 1) & 0xFF);
        int index = buildAddress(low, high);

        // Increment cycle if cross page happens
        if( index != ((index + Y) & 0xFF))
            cycles++;

        address = (index + Y) & 0xFFFF;
        value = bus.read(address);
    }

    /**
     * Indirect Y. Also known as Indirect Indexed.
     * Used exclusively with STA. No extra cycles are incremented when
     * cross page happens
     */
    private void indyPlus() {
        op1 = bus.read(incPC());
        int low = bus.read(op1);
        int high = bus.read((op1 + 1) & 0xFF);
        int index = buildAddress(low, high);
        address = (index + Y) & 0xFFFF;
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
        P.setZNFlags(result);
    }

    /**
     * Add value to X register
     * @param val
     */
    public void addX(int val) {
        X = (X + val) & 0xFF;
        P.setZNFlags(X);
    }

    /**
     * Add value to Y register
     * @param val
     */
    private void addY(int val) {
        Y = (Y + val) & 0xFF;
        P.setZNFlags(Y);
    }

    /**
     * Arithmetic Shift left. Shift value one bit left
     * @return value shifted
     */
    private int asl() {
        value = value << 1;
        value =  P.setCFlag(value);
        P.setZNFlags(value);

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
     * Compare value and set C, F and N flags accordingly
     * @param cmp Value to compare
     */
    private void compare(int cmp) {
        int result = cmp - value;
        P.C = cmp >= value;
        P.setZFlag(result);
        P.setNFlag(result);
    }

    // Logical Shift Right of Accumulator or Bus
    public int lsr() {
        int lowBit = value & 0x1;
        value = value >> 1;
        P.C = lowBit == 0x1;
        P.setZFlag(value);
        P.N = false;

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
        P.setZNFlags(value);

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
        P.setZNFlags(value);

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
