package me.aymen.anes.io.controls;

/**
 * Represents no pad attached to NES
 */
public class NoPad implements GamePad {
    @Override
    public int read() {
        return 0;
    }

    @Override
    public void poll() {

    }

    @Override
    public void update() {

    }
}
