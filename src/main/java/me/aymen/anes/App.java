package me.aymen.anes;

import me.aymen.anes.memory.Bus;
import me.aymen.anes.memory.Cartridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main( String[] args ) {
        // TODO Create an NES Board that do this functinalities
        Bus bus = new Bus();
        Cartridge c = new Cartridge(bus);
        c.load("test roms/nestest.nes");
        CPU cpu = new CPU(bus);
        // TODO REMOVE these settings. Only used for testing nestest.nes
        cpu.setPC(0xC000);
        cpu.decSP();
        cpu.decSP();
        CPUStatus status = null;
        int i = 0;
        while (i < 10000) {
            try {


                status = cpu.tick();
                String message = String.format("%-40s%s", Deassembler
                        .analyse(status), Deassembler.showStatus(status));
                System.out.println(message + " TICK: " + i);

            } catch(Exception e){
                //logger.error("Error while executing", e);
            }
            i++;
        }
    }
}
