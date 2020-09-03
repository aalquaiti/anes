package me.aymen.anes.io.controls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Joysticks and expansion slot used for other third party controllers.
 * Responsible for read/write for CPU Memory access at 0x4016 and 0x4017
 */
public class Controller {
    private final Logger logger = LoggerFactory.getLogger(Controller.class);

    // Handles a latch strobe being open or closed
    private boolean latch;

    private GamePad pad1;
    private GamePad pad2;

    public void setPad1(GamePad pad1) {
        this.pad1 = pad1;
    }

    public void setPad2(GamePad pad2) {
        this.pad2 = pad2;
    }

    public boolean isLatchOpen() {
        return latch;
    }

    /**
     * Handles read for control IO at address 0x4016 and 0x4017
     */
    public int read(int address) {
        switch (address) {
            case 0x4016:
                return pad1.read();
            case 0x4017:
                return pad2.read();
            default:
                // Should never happen
                logger.error(String.format("Controller read for unknown device" +
                        "at address $%04X not supported", address));
                System.exit(-1);
        }

        return 0;
    }

    /**
     * Handles write for control IO at address 0x4016 and 0x4017.
     * Writes to 0x4017 are ignored
     */
    public void write(int value, int address) {
        if (address!= 0x4016) {
            return;
        }

        pad1.poll();
        pad2.poll();

//        // Only gamepads are supported for now, so only the first bit
//        // is read to open or close the latch
//        value &= 0x1;
//
//        // A poll happens when firstly 1 is written to open the strobe,
//        // and then is closed by writing 0.
//        if(value == 1) {
//            latch = true;
//            pad1.poll();
//            pad2.poll();
//        } else {
//            // if latch is open and about to be closed,
//            // poll the buttons pressed
////            if(latch) {
////                pad1.poll();
////                pad2.poll();
////            }
//            latch = false;
//        }
    }

    /**
     * Update controllers status of buttons pressed/released
     */
    public void update() {
        pad1.update();
        pad2.update();
    }
}
