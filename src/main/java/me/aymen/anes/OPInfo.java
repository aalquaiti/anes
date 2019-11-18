package me.aymen.anes;

/**
 * Used to store info used for de-assembling
 */
public class OPInfo {
    int op1;
    int op2;
    int pc;

    public OPInfo(int op1, int op2, int pc) {
        this.op1 = op1;
        this.op2 = op2;
        this.pc = pc;
    }
}
