package me.aymen.anes.exception;

/**
 * Thrown when a ROM file is corrupted
 */
public class InvalidROMException extends RuntimeException {

    public InvalidROMException() {
        super();
    }

    public InvalidROMException(String message) {
        super(message);
    }
}
