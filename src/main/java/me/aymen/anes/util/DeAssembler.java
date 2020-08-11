package me.aymen.anes.util;


import me.aymen.anes.cpu.AddressMode;
import me.aymen.anes.cpu.CPUStatus;
import me.aymen.anes.io.Bus;

import java.util.function.Function;

import static me.aymen.anes.cpu.AddressMode.*;

/**
 * Helper Class that takes an opcode and return a String representing
 * it in assembly language
 */
public class DeAssembler {

    private final Bus bus;
    // Used to represent address modes
    private static Function<CPUStatus, String>[] modes;
    // Used to show memory address and content according to address mode
    private static Function<CPUStatus, String>[] mem;


    public DeAssembler(Bus bus) {
        this.bus = bus;
        modes = new Function[AddressMode.SIZE];
        mem = new Function[AddressMode.SIZE];

        modes[IMPL] =  v -> "";
        modes[ACC] = v -> "A";
        modes[IMM] = v -> String.format("#$%02X", v.op1);
        modes[ZPG] = v -> String.format("$%02X = %02X", v.op1, v.value);
        modes[ZPGX] = v -> String.format("$%02X,X = %02X", v.op1, v.value);
        modes[ZPGY] = v -> String.format("$%02X,Y = %02X", v.op1, v.value);
        modes[REL] = v -> {
            // Relative must return the address relative to current pc
            // Add two to PC as branching will execute two steps before
            // branching or not if op1 is positive
            // op1 will include a signed value so must be treated as byte
            int address = v.PC + ((v.op1 & 0xF0) != 0xF0 ? 2 : 0);
            address += (byte) v.op1;
            // Wrap within 0xFFFF
            address &= 0xFFFF;

            return String.format("$%04X", address);
        };
        modes[ABS] = v -> String.format("$%02X%02X = %02X", v.op2, v.op1,
                v.value);
        modes[ABSX_P] = v -> String.format("$%02X%02X,X = %02X", v.op2, v.op1,
                v.value);
        modes[ABSX_O] = modes[ABSX_P];
        modes[ABSY_P] = v -> String.format("$%02X%02X,Y = %02X", v.op2, v.op1,
                v.value);
        modes[ABSY_O] = modes[ABSY_P];
        modes[IND] = v -> String.format("($%02X%02X)  = %02X", v.op2, v.op1,
                v.value);
        modes[INDX] = v -> String.format("($%02X,X) -> $%04X  = %02X", v.op1,
                v.address, v.value);
        modes[INDY_P] = v -> String.format("($%02X),Y -> $%04X  = %02X", v.op1,
                v.address, v.value);
        modes[INDY_O] = modes[INDY_P];
    }

    /**
     * Analyse an instruction and return an assembly language representation.
     * Example:
     * C000  6D FF 00  ADC $00FF
     * A:## X:## Y:## P:## SP:## CYC:##
     * @param c cpu status
     * @return String representation
     */
    public String analyse(CPUStatus c) {
        String op1 = c.op1 == -1? "  " : String.format("%02X", c.op1);
        String op2 = c.op2 == -1? "  " : String.format("%02X", c.op2);
        return String.format("%04X  %02X %s %s%8s  %s", c.PC, c.op, op1,
                op2, c.opcode.name, modes[c.opcode.mode].apply(c));
    }

    /**
     * Shows the CPU and PPU status in String format
     * @param c CPU Status
     * @param p PPU Status as string
     * @return
     */
    public String showStatus(CPUStatus c, String p) {
        // TODO improve this. DeAssembler has access to bus. no need to pass
        // arguments
        return String.format("A:%02X X:%02X Y:%02X P:%02X SP:%02X %s CYC:%d",
                c.A, c.X, c.Y, c.P.getStatus(), c.SP, p, c.clockCounter);
    }
}
