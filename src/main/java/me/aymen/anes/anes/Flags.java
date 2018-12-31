package me.aymen.anes.anes;

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
     * Overflow
     */
    public boolean V ;

    /**
     * Sign (Negative)
     */
    public boolean S;


    public void reset() {
        C = false;
        Z = false;
        I = false;
        D = false;
        B = false;
        V = false;
        S = false;
    }
}
