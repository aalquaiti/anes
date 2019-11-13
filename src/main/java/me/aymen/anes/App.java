package me.aymen.anes;

import me.aymen.anes.memory.Bus;
import me.aymen.anes.memory.Cartridge;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        // TODO Create an NES Board that do this functinalities
        Bus bus = new Bus();
        Cartridge c = new Cartridge(bus);
        c.load("test roms/cpu_dummy_reads.nes");

        CPU cpu = new CPU(bus);
//        cpu.reset();
//        cpu.execute();
        cpu.start();

        System.out.println(cpu.getCycles());
    }
}
