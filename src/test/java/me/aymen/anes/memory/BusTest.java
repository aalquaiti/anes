package me.aymen.anes.memory;

import me.aymen.anes.CPU;
import me.aymen.anes.memory.Bus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusTest {
    private CPU cpu;
    private Bus bus;

    @BeforeEach
    public void setUp() {
        bus = new Bus();
        cpu = new CPU(bus);
    }

    @Test
    // Assert that RAM space mirror value after 0x800
    public void testReadRAMSpace() {
        bus.memory[0x6FC] = 10;
        assertEquals(10, bus.read(0x16FC));
    }

    @Test
    public void testWriteRAMSpace() {
        bus.write(10, 0x16FC);
        assertEquals(10, bus.memory[0x6FC]);
    }

    @Test
    public void testIOSpace() {
        bus.write(10, 0x3FFF);
        assertEquals(10, bus.memory[0x2007]);
    }

    @Test
    public void testWriteROMSpace() {
        assertThrows(IllegalArgumentException.class, () -> {
        bus.write(10, 0x8000);
        });
    }
}