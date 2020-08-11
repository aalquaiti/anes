package me.aymen.anes.io;

import me.aymen.anes.util.Pair;

/**
 * Represents PPU IO Registers
 */
public class PPURegister {
    /**
     * PPU Registers starting from memory address of 0x2000 to 0x2007.
     * Internally, they are viewed as 8 byte array
     */
    public final static int REG_SIZE = 8;



    // IO Components
    private final Bus bus;
    public int[] reg;


    // This variable is used to emulate the address latch effect that happens
    // when a write happens at PPU_ADDR. For CPU to write to VRAM,
    // it needs two writes at PPU_ADDR, to represent at 16 byte address.
    // The first right set the most significant byte, while the second sets
    // the least significant byte. Example to Write the address 0x1234
    // First Write: 0x12
    // Second Write: 0x34
    // This latch is reset when PPU_STATUS is read
    private boolean latchUsed;

    // Used to store the current address to write/read data to vram
    // when PPU_ADDR and PPU_DATA are used
    private int ppu_address;

    // Used to store previously read data. When reading data from PPU, it takes
    // another cycle to get the new value. The fetched value on first cycle is
    // just the previusly buffered data. On the second read, new data is
    // buffered and fetched
    private int ppu_buffer;

    // Registers
    // For details:
    // https://wiki.nesdev.com/w/index.php/PPU_programmer_reference
    public static final int PPU_CTRL = 0;
    public static final int PPU_MASK = 1;
    public static final int PPU_STATUS = 2;
    public static final int OAM_ADDR = 3;
    public static final int OAM_DATA = 4;
    public static final int PPU_SCROLL = 5;
    public static final int PPU_ADDR = 6;
    public static final int PPU_DATA = 7;

    public PPURegister(Bus bus) {
        this.bus = bus;
        reg = new int[REG_SIZE];
    }

    /**
     * Read value of registers at PPU_STATUS or PPU_DATA.
     * PPU_Data gives access to ppu memory
     * @param address
     * @return
     */
    public int read(int address) {
        int value = 0;

        // When reading from write only register
        if (address != PPU_STATUS || address != PPU_DATA) {
            throw new IllegalArgumentException(String.format(
                    "PPU Register at address $%4X is write only", address));
        }

        if (address == PPU_STATUS) {
            // TODO this is a hack and should be removed
            setVerticalBlank(true);

            // Reset address latch when status is read
            latchUsed = false;

            // TODO Check if this addition is important
            // Reading status set internal value to be as in the line below,
            // as being explained by youtuber javidx9 in his video name:
            // NES Emulator Part #4
            // 0xE0 returns bits 7,6 5, while 0x1F handles the others from
            // the stored buffer value
            value = reg[PPU_STATUS] & 0xE0 | (ppu_buffer & 0x1F);
        }
        else {
            // Return the buffered data
            value = ppu_buffer;
            ppu_buffer = bus.ppuRead(ppu_address);

            // PPU Read from CPU will take extra cycle
            // unless it is a palette address
            Pair<IO, Integer> pair = bus.ppuAccess(ppu_address);
            if (pair.first == IO.PLTE) {
                return bus.ppuRead(ppu_address);
            }

            return value;
        }

        return value;
    }

    public void write(int value, int address) {
        if (address == 2) {
            throw new IllegalArgumentException(String.format(
                    "PPU Register at address %n is read only", address));
        }

        // Set the address latch if not set
        // the ppu_address will be updated accordingly
        // If latched is not used, then value will effect the ppu_address most
        // significant byte. If used, the address will effect the ppu_address
        // least significant byte
        if (address == PPU_ADDR ) {
            if (!latchUsed) {
                latchUsed = true;
                ppu_address = (ppu_address & 0x00FF) + (value << 8);
            } else {
                latchUsed = false;
                ppu_address = (ppu_address & 0xFF00) + value;
            }
            // TODO is the storage of the value necessary?
//            reg[PPU_ADDR] = value;
        }

        // VRAM address to write to should be ppu_address, which by now
        // should have been affected by two writes to PPU_ADDR
        else if (address == PPU_DATA) {
            bus.ppuWrite(value, ppu_address);

            // TODO should we write the value PPU_DATA as well?
            reg[PPU_DATA] = value;

        } else {
            reg[address] = value;
        }

    }

    /**
     * Retrieve name table address that is stored in bits 0 and 1 of PPUCTRL.
     * Addresses of name table
     * 0: 0x2000
     * 1: 0x2400
     * 2: 0x2800
     * 3: 0x2C00
     * @return
     */
    public int getNameTableAddress() {
        return reg[PPU_CTRL] & 0b11;
    }

    /**
     * Set name table address by storing it in bits 0 and 1 of PPUCTRL.
     * Only first two bits are address provided are used.
     * @param address
     */
    public void setNameTableAddress(int address) {
        address &= 0b11;
        reg[PPU_CTRL] |= address;
    }

    /**
     * Retrieve address increment in VRAM address dependending of bit 2 of
     * PPUCTRL:
     * 0: adds 1 (Go horizontal)
     * 1: adds 32 (Go vertical)
     * @return either 1 or 32 depend on bit 2 value
     */
    public int getIncrementModeValue() {
        if ( (reg[PPU_CTRL] & 0b100) == 0) {
            return 1;
        }

        return 32;
    }

    /**
     * Set bit 2 of PPUCTRL that indicates the increment of VRAM address.
     * 0: adds 1 (Go horizontal)
     * 1: adds 32 (Go vertical)
     * False is treated as zero, True as 1
     * @param vertical
     */
    public void setIncrementMode(boolean vertical) {
        int value = vertical ? 0x4 : 0x0;
        reg[PPU_CTRL] |= value;
    }

    public void setVerticalBlank(boolean started) {
        int value = started ? 0x80 : 0x00;
        reg[PPU_STATUS] |= value;
    }
}
