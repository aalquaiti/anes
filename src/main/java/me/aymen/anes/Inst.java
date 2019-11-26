package me.aymen.anes;

import me.aymen.anes.util.Function;

/**
 * Represent Instruction performed by CPU. This object include the name of the
 * instruction, number of cycles it consumes, related address mode, and
 * a function pointer that represent the operation.
 */
public class Inst {

    /**
     * Operation Code
     */
    public String name;

    /**
     * Number of cycles an operation consume
     */
    public int cycles;

    /**
     * Address mode of the operation
     */
    public int mode;

    /**
     * Operation code to perform
     */
    public Function operation;

    public Inst(String name, int cycles, int mode, Function operation) {
        this.name = name;
        this.cycles = cycles;
        this.mode = mode;
        this.operation = operation;
    }
}
