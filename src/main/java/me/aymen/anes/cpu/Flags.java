package me.aymen.anes.cpu;

/**
 * Represents CPU Processor Status
 */
public class Flags {

    public Flags()
    {
        reset();
    }
    /**
     *  Carry status
     */
    public boolean C;

    /**
     * Zero
     */
    public boolean Z;

    /**
     * Interrupt
     */
    public boolean I;

    /**
     * Decimal Mode
     */
    public boolean D;

    /**
     * Break
     */
    public boolean B;

    /**
     * Unused flag
     */
    public boolean U;

    /**
     * Overflow
     */
    public boolean V ;

    /**
     * Sign (Negative)
     */
    public boolean N;


    private void reset() {
        C = false;
        Z = false;
        I = false;
        D = false;
        B = false;
        U = true; // Unused. Always set
        V = false;
        N = false;
    }

    /**
     * Set the carry flag
     * @param value Value to base flag on
     * @return Value supplied adjusted to reflect one byte without carry bit
     */
    public int setCFlag(int value) {
        C = (value & 0x100) == 0x100;

        return value & 0xFF;
    }

    /**
     * Sets the Zero flag
     * @param value The value to set flags upon
     */
    public void setZFlag(int value) {
        Z = value == 0;
    }

    /**
     * Sets the Negative flag
     * @param value The value to set flags upon
     */
    public void setNFlag(int value) {
        N = (value & 0x80) == 0x80;
    }

    /**
     * Sets the Zero and Negative flag
     * @param value The value to set flags upon
     */
    public void setZNFlags(int value) {
        // Repeated as to avoid another method call stack
        Z = value == 0;
        N = (value & 0x80) == 0x80;
    }

    /**
     * Set the overflow flag.
     * @param A current accumulator register value before operation
     * @param value value of the operand (first byte after instruction)
     * @param result the result of the operation
     */
    public void setVFlag(int A, int value, int result) {
        // If the 8th bit of A and value is 0 and result is 1
        // or if 8th bit of A and value is 1 and result is 0,
        // this will indicate an overflow
        V = ((A ^ result) & (value ^ result) & 0x80) != 0x0;
    }

    /**
     * Retrieve all flags as 8 bit value
     * @return
     */
    public int getStatus() {
        int value[] = {
                C ? 1 : 0,
                Z ? 1 : 0,
                I ? 1 : 0,
                D ? 1 : 0,
                B ? 1 : 0,
                1,          // For unused flag
                V ? 1 : 0,
                N ? 1 : 0};

        int result = 0;
        int counter = 1;
        for (int item : value) {
            result += counter * item;
            counter *= 2;
        }

        return result;
    }

    /**
     * Set flags by given value.
     * @param value
     */
    public void setStatus(int value) {
        C = (value & 0x01) == 0x01;
        Z = (value & 0x02) == 0x02;
        I = (value & 0x04) == 0x04;
        D = (value & 0x08) == 0x08;
        B = (value & 0x010) == 0x10;
        U = true;
        V = (value & 0x40) == 0x40;
        N = (value & 0x80) == 0x80;
    }

    public Flags clone() {
        Flags P = new Flags();
        P.setStatus(this.getStatus());

        return P;
    }
}
