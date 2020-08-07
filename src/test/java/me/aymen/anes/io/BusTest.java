package me.aymen.anes.io;

import me.aymen.anes.cpu.CPU;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusTest {
    private Bus bus;

    @BeforeEach
    public void setUp() {
        bus = new Bus();
        // Added as rom memory is only initialised when a cartridge is loaded
    }

    @Test
    // Assert that RAM space mirror value after 0x800
    public void testReadRAMSpace() {
        bus.ram.memory[0x6FC] = 10;
        assertEquals(10, bus.cpuRead(0x16FC));
    }

    @Test
    // Assert that RAM writes within confined space and mirror values after 0x800
    public void testWriteRAMSpace() {
        bus.cpuWrite(10, 0x16FC);
        assertEquals(10, bus.cpuRead(0x6FC));
    }

    // TODO add test for PPU_IO APU_IO, ROM and VRAM
}