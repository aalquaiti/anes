package me.aymen.anes;

import me.aymen.anes.RAM;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test CPU instructions. Immediate addressing memory is assumed if direct memory is needed
 */
public class InstructionTest {
    CPU cpu;
    RAM ram;

    @Before
    public void setUp() {
        cpu = new CPU();
        ram = new RAM();
        cpu.setRam(ram);
    }

    /**
     * Test initial value of A is what is set without any effect on flags
     */
    @Test
    public void testADCInit() {
        ram.memory[0x0] = 0x3A;
        ram.memory[0x1] = 0x7C;

        cpu.adc(0x0);

        assertEquals(0x3A,cpu.getA());
        assertEquals(false, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(false, cpu.getFlags().V);
        assertEquals(false, cpu.getFlags().S);
    }

    /**
     * Test addition of A that contain initial value with value in memory, triggering overflow and
     * sign flag
     */
    @Test
    public void testADCValue() {
        ram.memory[0x0] = 0x3A;
        ram.memory[0x1] = 0x7C;

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
        ram.memory[0x0] = 0x50;
        ram.memory[0x1] = 0x50;

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
        ram.memory[0x0] = 0xFF;

        cpu.adc(0x0);

        assertEquals(0xFF,cpu.getA());
        assertEquals(false, cpu.getFlags().C);
        assertEquals(false, cpu.getFlags().Z);
        assertEquals(false, cpu.getFlags().V);
        assertEquals(true, cpu.getFlags().S);
    }
}
