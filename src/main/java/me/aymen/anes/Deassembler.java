package me.aymen.anes;


import java.util.function.Function;

import static me.aymen.anes.AddressMode.*;

/**
 * Helper Class that takes an opcode and return a String representing
 * it in assembly language
 */
public class Deassembler {
    private static Function<OPInfo, String>[] modes =
            new Function[AddressMode.SIZE];
    
    static {
        modes[IMPL] =  v -> "";
        modes[ACC] = v -> "A";
        modes[IMM] = v -> String.format("#$%02X", v.op1);
        modes[ZPG] = v -> String.format("$%02X", v.op1);
        modes[ZPGX] = v -> String.format("$%02X,X", v.op1);
//        modes[ZPGY] = this::zpgy;
        modes[REL] = v -> {
            // Relative must return the address relative to current pc
            // Add two to PC as branching will execute two steps before
            // branching or not if op1 is positive
            // op1 will include a signed value so must be treated as byte
            int address = v.pc + ((v.op1 & 0xF0) != 0xF0 ? 2 : 0);
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
//        modes[IND] = this::ind;
        modes[INDX] = v -> String.format("($%02X,X)", v.op1);
        modes[INDY] = v -> String.format("($%02X),Y", v.op1);
        modes[INDY_PLUS] = modes[INDY];
    }

    /**
     * Analyse an instruction and return an assembly language representation
     * in the format:
     * ####     OP OP OP
     * Example
     * C000     ADC $00FF
     * @param pc Program Counter
     * @param opcode Instruction to analyse
     * @param op1 Second byte of the operation
     * @param op2 Third byte of the operation
     * @return String representation
     */
    public static String analyse(int pc, Inst opcode, int op1, int op2) {
        return String.format("%04X    %s %s", pc, opcode.name,
                modes[opcode.mode].apply(new OPInfo(op1, op2, pc)));
    }
}
