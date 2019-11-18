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
        //Cartridge c = new Cartridge(bus);
        //c.load("test roms/cpu_dummy_reads.nes");


        CPU cpu = new CPU(bus);
        bus.memory[0] = 0x31;
        bus.memory[1] = 0xFF;
        bus.memory[2] = 0x40;
        cpu.setPC(0);
        cpu.setX(1);
        //cpu.reset();
//        cpu.execute();
        System.out.println(cpu.tick());

        System.out.println(cpu.getCycles());
    }
}
