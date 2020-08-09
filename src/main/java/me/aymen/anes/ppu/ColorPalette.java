package me.aymen.anes.ppu;

import java.awt.*;

/**
 * Give access to all supported NES colors
 */
public class ColorPalette {
    Color[] color;

    public ColorPalette() {
        color = new Color[0x40];
        color[0x00] = new Color(0x75,0x75,0x75);
        color[0x01] = new Color(0x27,0x1B,0x8F);
        color[0x02] = new Color(0x00,0x00,0xAB);
        color[0x03] = new Color(0x47,0x00,0x9F);
        color[0x04] = new Color(0x8F,0x00,0x77);
        color[0x05] = new Color(0xAB,0x00,0x13);
        color[0x06] = new Color(0xA7,0x00,0x00);
        color[0x07] = new Color(0x7F,0x0B,0x00);
        color[0x08] = new Color(0x43,0x2F,0x00);
        color[0x09] = new Color(0x00,0x47,0x00);
        color[0x0A] = new Color(0x00,0x51,0x00);
        color[0x0B] = new Color(0x00,0x3F,0x17);
        color[0x0C] = new Color(0x1B,0x3F,0x5F);
        color[0x0D] = new Color(0x00,0x00,0x00);
        color[0x0E] = new Color(0x00,0x00,0x00);
        color[0x0F] = new Color(0x00,0x00,0x00);
        color[0x10] = new Color(0xBC,0xBC,0xBC);
        color[0x11] = new Color(0x00,0x73,0xEF);
        color[0x12] = new Color(0x23,0x3B,0xEF);
        color[0x13] = new Color(0x83,0x00,0xF3);
        color[0x14] = new Color(0xBF,0x00,0xBF);
        color[0x15] = new Color(0xE7,0x00,0x5B);
        color[0x16] = new Color(0xDB,0x2B,0x00);
        color[0x17] = new Color(0xCB,0x4F,0x0F);
        color[0x18] = new Color(0x8B,0x73,0x00);
        color[0x19] = new Color(0x00,0x97,0x00);
        color[0x1A] = new Color(0x00,0xAB,0x00);
        color[0x1B] = new Color(0x00,0x93,0x3B);
        color[0x1C] = new Color(0x00,0x83,0x8B);
        color[0x1D] = new Color(0x00,0x00,0x00);
        color[0x1E] = new Color(0x00,0x00,0x00);
        color[0x1F] = new Color(0x00,0x00,0x00);
        color[0x20] = new Color(0xFF,0xFF,0xFF);
        color[0x21] = new Color(0x3F,0xBF,0xFF);
        color[0x22] = new Color(0x5F,0x97,0xFF);
        color[0x23] = new Color(0xA7,0x8B,0xFD);
        color[0x24] = new Color(0xF7,0x7B,0xFF);
        color[0x25] = new Color(0xFF,0x77,0xB7);
        color[0x26] = new Color(0xFF,0x77,0x63);
        color[0x27] = new Color(0xFF,0x9B,0x3B);
        color[0x28] = new Color(0xF3,0xBF,0x3F);
        color[0x29] = new Color(0x83,0xD3,0x13);
        color[0x2A] = new Color(0x4F,0xDF,0x4B);
        color[0x2B] = new Color(0x58,0xF8,0x98);
        color[0x2C] = new Color(0x00,0xEB,0xDB);
        color[0x2D] = new Color(0x00,0x00,0x00);
        color[0x2E] = new Color(0x00,0x00,0x00);
        color[0x2F] = new Color(0x00,0x00,0x00);
        color[0x30] = new Color(0xFF,0xFF,0xFF);
        color[0x31] = new Color(0xAB,0xF7,0xFF);
        color[0x32] = new Color(0xC7,0xD7,0xFF);
        color[0x33] = new Color(0xD7,0xCB,0xFF);
        color[0x34] = new Color(0xFF,0xC7,0xFF);
        color[0x35] = new Color(0xFF,0xC7,0xDB);
        color[0x36] = new Color(0xFF,0xBF,0xB3);
        color[0x37] = new Color(0xFF,0xDB,0xAB);
        color[0x38] = new Color(0xFF,0xE7,0xA3);
        color[0x39] = new Color(0xE3,0xFF,0xA3);
        color[0x3A] = new Color(0xAB,0xF3,0xBF);
        color[0x3B] = new Color(0xB3,0xFF,0xCF);
        color[0x3C] = new Color(0x9F,0xFF,0xF3);
        color[0x3D] = new Color(0x00,0x00,0x00);
        color[0x3E] = new Color(0x00,0x00,0x00);
        color[0x3F] = new Color(0x00,0x00,0x00);
    }

    public Color get(int index) {
        return color[index];
    }
}
