package me.aymen.anes;


import java.util.function.BiFunction;
import static me.aymen.anes.AddressMode.*;

/**
 * Helper Class that takes an opcode and return a String representing
 * it in assembly language
 */
public class Deassembler {
    private static BiFunction<Integer, Integer, String>[] modes =
            new BiFunction[AddressMode.SIZE];
    
    static {
        modes[IMPL] =  (x,y) -> "";
//        modes[ACC] = ::acc;
//        modes[IMM] = this::imm;
        modes[ZPG] = (x,y) -> String.format("$%02X", x);
//        modes[ZPGX] = this::zpgx;
//        modes[ZPGY] = this::zpgy;
//        modes[REL] = () -> {};
//        modes[ABS] = this::abs;
//        modes[ABSX] = this::absx;
//        modes[ABSY] = this::absy;
//        modes[IND] = this::ind;
        modes[INDX] = (x,y) -> String.format("($%02X,X)", x);
//        modes[INDY] = this::indy;
    }

    /**
     * Analyse an instruction and return an assembly language representation
     * in the format:
     * ####     OP OP OP
     * Example
     * C000     ADC $00FF
     * @param opcode Instruction to analyse
     * @param op1 Second byte of the operation
     * @param op2 Third byte of the operation
     * @return String representation
     */
    public static String analyse(Inst opcode, int op1, int op2) {
        return String.format("%s %s", opcode.name,
                modes[opcode.mode].apply(op1, op2));
    }
}
