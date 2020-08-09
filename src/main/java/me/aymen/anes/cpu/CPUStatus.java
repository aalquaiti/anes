package me.aymen.anes.cpu;

/**
 * Stores CPU Status after each tick.
 */
public class CPUStatus {
    public int PC;
    public int op;      // op code in binary
    public int op1;
    public int op2;
    public Inst opcode; // Instruction object
    public int A;
    public int X;
    public int Y;
    public Flags P;
    public int SP;
    public int cycles;
    public int clockCounter;
    public int address;

    public CPUStatus() {

    }
}
