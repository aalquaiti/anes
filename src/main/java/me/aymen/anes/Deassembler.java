package me.aymen.anes;


import java.util.function.Function;

import static me.aymen.anes.AddressMode.*;

/**
 * Helper Class that takes an opcode and return a String representing
 * it in assembly language
 */
public class Deassembler {
    
    // Used to represent address modes
    private static Function<CPUStatus, String>[] modes =
            new Function[AddressMode.SIZE];
    
    // Used to show memory address and content according to address mode
    private static Function<CPUStatus, String>[] mem =
            new Function[AddressMode.SIZE]; 
    
    static {
        modes[IMPL] =  v -> "";
        modes[ACC] = v -> "A";
        modes[IMM] = v -> String.format("#$%02X", v.op1);
        modes[ZPG] = v -> String.format("$%02X", v.op1);
        modes[ZPGX] = v -> String.format("$%02X,X", v.op1);
        modes[ZPGY] = v -> String.format("$%02X,Y", v.op1);
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
        modes[ABS] = v -> String.format("$%02X%02X", v.op2, v.op1);
        modes[ABSX] = v -> String.format("$%02X%02X,X", v.op2, v.op1);
        modes[ABSX_PLUS] = modes[ABSX];
        modes[ABSY] = v -> String.format("$%02X%02X,Y", v.op2, v.op1);
        modes[ABSY_PLUS] = modes[ABSY];
        modes[IND] = v -> String.format("($%02X%02X)", v.op2, v.op1);
        modes[INDX] = v -> String.format("($%02X,X)", v.op1);
        modes[INDY] = v -> String.format("($%02X),Y", v.op1);
        modes[INDY_PLUS] = modes[INDY];
    }

    /**
     * Analyse an instruction and return an assembly language representation.
     * Example:
     * C000  6D FF 00  ADC $00FF
     * A:## X:## Y:## P:## SP:## CYC:## COUNT:####
     * @param s cpu status
     * @return String representation
     */
    public static String analyse(CPUStatus s) {
        String op1 = s.op1 == -1? "  " : String.format("%02X", s.op1);
        String op2 = s.op2 == -1? "  " : String.format("%02X", s.op2);
        return String.format("%04X  %02X %s %s%8s  %s", s.PC, s.op, op1,
                op2, s.opcode.name, modes[s.opcode.mode].apply(s));
    }

    /**
     * Shows the CPU and PPU status in String format
     * @param s CPU Status
     * @return
     */
    public static String showStatus(CPUStatus s) {
        return String.format("A:%02X X:%02X Y:%02X P:%02X SP:%02X CYC:%d " +
                "SUM: %d", s.A, s.X, s.Y, s.P.getStatus(), s.SP, s.cycle,
                s.cycleCount);
    }
}
