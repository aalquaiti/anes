package me.aymen.anes;

public class RAM {

    public static int SIZE = 0x800;
    public final int[] memory;

    public RAM() {
        memory = new int[SIZE];
    }

    public int read(int index) {

        checkIndex(index);

        return memory[index % SIZE];
    }

    public void write(byte data, int index) {
        checkIndex(index);

        memory[index % SIZE] = data;
    }

    private void checkIndex(int index) {
        if(index < 0 || index > SIZE - 1)
            throw new IllegalArgumentException("Accessing beyond memory boundary");
    }
}
