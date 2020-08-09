package me.aymen.anes.io;

/**
 * Describes IO devices connected to bus and is accessed by CPU or PPU
 */
public enum IO {
    RAM,
    PPU_IO,
    APU_IO,
    PGR_ROM,
    CHR_ROM,
    VRAM,
    PLTE // PPU Palette Memory,
}
