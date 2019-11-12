package me.aymen.anes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test CPU instructions. Immediate addressing data is assumed if direct data is needed
 */
public class CPUTest {
    CPU cpu;
    Memory memory;

    @BeforeEach
    public void setUp() {
        memory = new Memory();
        cpu = new CPU(memory);
        cpu.reset();
    }

    /**
     * Test initial value of A is what is set without any effect on flags
     */
    @Test
    public void testADCInit() {
        memory.data[0x0] = 0x3A;
        memory.data[0x1] = 0x7C;

        cpu.adc(0x0);

        assertEquals(0x3A,cpu.getA());
        assertEquals(false, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(false, cpu.getFlags().V);
        assertEquals(false, cpu.getFlags().S);
    }

    /**
     * Test addition of A that contain initial value with value in data, triggering overflow and
     * sign flag
     */
    @Test
    public void testADCValue() {
        memory.data[0x0] = 0x3A;
        memory.data[0x1] = 0x7C;

        cpu.adc(0x0);
        cpu.adc(0x1);

        assertEquals(0xB6,cpu.getA());
        assertEquals(false, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(true, cpu.getFlags().V);
        assertEquals(true, cpu.getFlags().S);
    }

    /**
     * Test addition of initial A with Zero, triggering Zero flag
     */
    @Test
    public void testADCZero() {
        cpu.adc(0x0);

        assertEquals(0, cpu.getA());
        assertEquals(true, cpu.getFlags().Z);
    }

    /**
     * Test addition that triggers overflow. By default, the sign flag is expected to
     * be triggered as well in this test.
     */
    @Test
    public void testADCOverFlow() {
        memory.data[0x0] = 0x50;
        memory.data[0x1] = 0x50;

        cpu.adc(0x0);
        cpu.adc(0x1);

        assertEquals(0xA0,cpu.getA());
        assertEquals(false, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(true, cpu.getFlags().V);
        assertEquals(true, cpu.getFlags().S);
    }

    /**
     * Test sign flag is triggered without overflow
     */
    @Test
    public void testADCNegative() {
        memory.data[0x0] = 0xFF;

        cpu.adc(0x0);

        assertEquals(0xFF,cpu.getA());
        assertEquals(false, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(false, cpu.getFlags().V);
        assertEquals(true, cpu.getFlags().S);
    }

    /**
     * Test AND of a zero page address
     */
    @Test
    public void testANDZpg() {
        memory.data[0x0] = 0xFC;
        memory.data[0x1] = 0x13;

        cpu.adc(0x0);
        cpu.and(0x1);

        assertEquals(0x10,cpu.getA());
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(false, cpu.getFlags().S);
    }

    @Test
    public void testASL() {
        assertEquals(0x96, cpu.asl(0xCB));
        assertEquals(true, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(true, cpu.getFlags().S);

    }

    @Test
    public void testBIT() {
        memory.data[0x0] = 0xA6;
        memory.data[0x1] = 0xE0;

        cpu.adc(0x0);
        cpu.bit(0x1);

        assertEquals(0xA6,cpu.getA());
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(true, cpu.getFlags().V);
        assertEquals(true, cpu.getFlags().S);
    }

    /**
     * Test branching takes place. No actual branching operation is tested
     */
    @Test
    public void testBranch() {
        memory.data[0x01] = 0x03; // Jump to 0x05

        cpu.branch(true);

        assertEquals(0x04,cpu.getPC());
    }

    @Test
    public void testCMP() {
        memory.data[0x0] = 0xF6;
        memory.data[0x1] = 0x18;

        cpu.adc(0x0);
        cpu.compare(cpu.getA(), 0x1);

        assertEquals(true, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(true, cpu.getFlags().S);
    }


    @Test
    public void testDEC() {
        memory.data[0x0] = 0xA5;

        cpu.inc(0xFF, 0x0);

        assertEquals(0xA4, memory.data[0x0]);
        assertEquals(false, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(true, cpu.getFlags().S);
    }

    @Test
    public void testEOR() {
        memory.data[0x0] = 0xE3;
        memory.data[0x1] = 0xA0;

        cpu.adc(0x0);
        cpu.eor(0x1);

        assertEquals(0x43,cpu.getA());
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(false, cpu.getFlags().S);
    }

    @Test
    public void testINC() {
        memory.data[0x0] = 0xC0;

        cpu.inc(0x01, 0x0);

        assertEquals(0xC1, memory.data[0x0]);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(true, cpu.getFlags().S);
    }

    @Test
    public void testLDA() {
        memory.data[0x0] = 0xAA;

        cpu.lda(0x0);

        assertEquals(0xAA, cpu.getA());
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(true, cpu.getFlags().S);
    }

    @Test
    public void testLSR() {
        memory.data[0x0] =  cpu.lsr(0x0D);
        cpu.lda(0x0);

        assertEquals(0x06, cpu.getA());
        assertEquals(true, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(false, cpu.getFlags().S);
    }


    @Test
    public void testORA() {
        memory.data[0x0] = 0xE3;
        memory.data[0x1] = 0xAB;

        cpu.lda(0x1);
        cpu.ora(0x0);

        assertEquals(0xEB, cpu.getA());
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(true, cpu.getFlags().S);
    }

    @Test
    public void testROL() {
        int value = cpu.rol(0x2E);

        assertEquals(0x5C, value);
        assertEquals(false, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(false, cpu.getFlags().S);
    }

    @Test
    public void testROR() {
        cpu.getFlags().C = true;
        int value = cpu.ror(0xED);

        assertEquals(0xF6, value);
        assertEquals(true, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(true, cpu.getFlags().S);
    }

    @Test
    public void testSBC() {
        memory.data[0x00] = 0x34;
        memory.data[0x01] = 0x14;

        cpu.lda(0x01);
        cpu.sbc(0x00);

        assertEquals(0xDF, cpu.getA());
        assertEquals(false, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(false, cpu.getFlags().V);
        assertEquals(true, cpu.getFlags().S);
    }

    @Test
    public void testSTA() {
        memory.data[0x00] = 0x63;
        cpu.lda(0x00);
        cpu.st(0x01, cpu.getA());

        assertEquals(0x63, memory.data[0x01]);
        assertEquals(false, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(false, cpu.getFlags().V);
        assertEquals(false, cpu.getFlags().S);
    }
}
