package me.aymen.anes.ppu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PPUAddressTest {
    PPUAddress addr;

    @BeforeEach
    void setUp() {
        addr = new PPUAddress();
    }

    @Test
    void getValue() {
        addr.setSecondHalf(0x2A);
        addr.setFirstHalf(0x15);

        assertEquals(0x2A15, addr.getValue());
    }

    @Test
    void setValue() {
        addr.setValue(0x5646);

        assertEquals(0x5646, addr.getValue());
    }

    @Test
    void setFullAddress() {
        addr.setValue(0x5646);

        assertEquals(0x646, addr.getFullAddress());
    }

    @Test
    void setCoarseX() {
        addr.setCoarseX(0x46);

        assertEquals(0x6, addr.getCoarseX());
    }

    @Test
    void setCoarseY() {
        addr.setCoarseY(0x46);

        assertEquals(0x6, addr.getCoarseY());
    }

    @Test
    void setHorizontalNameTable() {
        addr.setHorizontalNameTable(0x5);

        assertEquals(0x1, addr.getHorizontalNameTable());
    }

    @Test
    void setVerticalNameTable() {
        addr.setVerticalNameTable(0x5);

        assertEquals(0x1, addr.getVerticalNameTable());
    }

    @Test
    void setNameTable() {
        addr.setNameTable(0x7);

        assertEquals(0x3, addr.getNameTable());
    }

    @Test
    void setFineY() {
        addr.setFineY(0xF);

        assertEquals(0x7, addr.getFineY());
    }

    @Test
    void setFirstHalf() {
        addr.setCoarseX(0x1F);
        addr.setCoarseY(0x1F);

        addr.setFirstHalf(0xBD);

        assertEquals(0x1D, addr.getCoarseX());
        assertEquals(0x1D, addr.getCoarseY());
    }

    @Test
    void setSecondHalf() {
        addr.setCoarseY(0x1F);
        addr.setNameTable(0x3);
        addr.setFineY(0x7);

        addr.setSecondHalf(0x55);

        assertEquals(0xF, addr.getCoarseY());
        assertEquals(0x1, addr.getNameTable());
        assertEquals(0x5, addr.getFineY());
    }

    @Test
    void flipHorizontalNameTable() {
        addr.setHorizontalNameTable(1);
        addr.flipHorizontalNameTable();
        assertEquals(0, addr.getHorizontalNameTable());

        addr.setHorizontalNameTable(0);
        addr.flipHorizontalNameTable();
        assertEquals(1, addr.getHorizontalNameTable());
    }

    @Test
    void flipVerticalNameTable() {
        addr.setVerticalNameTable(1);
        addr.flipVerticalNameTable();
        assertEquals(0, addr.getVerticalNameTable());

        addr.setVerticalNameTable(0);
        addr.flipVerticalNameTable();
        assertEquals(1, addr.getVerticalNameTable());
    }
}