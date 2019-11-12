package me.aymen.anes;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        // TODO Create an NES Board that do this functinalities
        Cartridge c = new Cartridge();
        c.load("test roms/cpu_dummy_reads.nes");
        Memory memory = new Memory();
        memory.loadROM(c);
        CPU cpu = new CPU(memory);
//        cpu.reset();
//        cpu.execute();
        cpu.start();

        System.out.println(cpu.getCycles());
    }
}
