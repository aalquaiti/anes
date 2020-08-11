package me.aymen.anes.io;

/**
 * Describes IO devices connected to bus and is accessed by CPU or PPU
 */
public enum IO {

    // Accessible through CPU
    RAM,
    PPU_IO,
    APU_IO,
    OAM_DMA,
    PGR_ROM,

    // Accessible through PPU
    CHR_ROM,
    VRAM,
    PLTE // PPU Palette Memory,
}
