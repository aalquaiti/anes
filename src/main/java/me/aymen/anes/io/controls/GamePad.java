package me.aymen.anes.io.controls;

/**
 * Represents NES game bad
 */
public interface GamePad {
    public static int A = 0x01;
    public static int B = 0x02;
    public static int SELECT = 0x04;
    public static int START = 0x08;
    public static int UP = 0x10;
    public static int DOWN = 0x20;
    public static int LEFT = 0x40;
    public static int RIGHT = 0x80;

    /**
     * Read one bit from a game pad
     */
    int read();

    /**
     * Poll the value of buttons clicked and reserve them in buffer.
     * This is important as each reach will result in shift the value of the
     * buffer. When controller is not polled, the latch is continuously updated
     * by pressed buttons
     */
    void poll();

    /**
     * Update the status of buttons pressed/released
     */
    void update();
}
