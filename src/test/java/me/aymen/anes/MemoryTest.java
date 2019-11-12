package me.aymen.anes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {
    private CPU cpu;
    private Memory memory;

    @BeforeEach
    public void setUp() {
        memory = new Memory();
        cpu = new CPU(memory);
    }

    @Test
    // Assert that RAM space mirror value after 0x800
    public void testReadRAMSpace() {
        memory.data[0x6FC] = 10;
        assertEquals(10, memory.read(0x16FC));
    }

    @Test
    public void testWriteRAMSpace() {
        memory.write(10, 0x16FC);
        assertEquals(10, memory.data[0x6FC]);
    }

    @Test
    public void testIOSpace() {
        memory.write(10, 0x3FFF);
        assertEquals(10, memory.data[0x2007]);
    }

    @Test
    public void testWriteROMSpace() {
        assertThrows(IllegalArgumentException.class, () -> {
        memory.write(10, 0x8000);
        });
    }
}