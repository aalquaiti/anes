package me.aymen.anes;

import me.aymen.anes.cpu.CPU;
import me.aymen.anes.io.Bus;
import me.aymen.anes.ppu.PPU;
import me.aymen.anes.ppu.Screen;
import me.aymen.anes.util.Deassembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nes {
    private static final Logger logger = LoggerFactory.getLogger(Nes.class);
    private Bus bus;
    private CPU cpu;
    private PPU ppu;
    private Screen screen;
    private int ticks;

    public Nes() {
        bus = new Bus();
        cpu = bus.cpu;
        ppu = bus.ppu;
        screen = new Screen(1024, 960);
        bus.ppu.connect(screen);

        // TODO REMOVE these settings. Only used for testing nestest.nes
        bus.rom.load("test roms/nestest.nes");
        cpu.reset();
        bus.cpu.setPC(0xC000);
        bus.cpu.decSP();
        cpu.decSP();
    }

    public void clock() {
        // PPU clock runs three times faster than cpu due to Johnson counter
        // See https://wiki.nesdev.com/w/index.php/Cycle_reference_chart
        // TODO move this function to be part of bus clock as this helps
        //  to manage bus related functionality such as DMA
        ppu.clock();
        ppu.clock();
        ppu.clock();
        cpu.clock();

        if (cpu.isComplete()) {
            ticks++;
            // TODO add ppu status
            String ppuStatus = String.format("PPU: %03d, %03d", ppu.scanLine,
                    ppu.cycle);
            String message = String.format("Tick: %05d %-50s%s", ticks,
                    Deassembler .analyse(cpu.getStatus()),
                    Deassembler.showStatus(cpu.getStatus()));
            System.out.println(message);
        }

        if (ppu.isComplete()) {
            screen.repaint();
        }
    }

    public void start() {
        int i = 0;
        while (i < 100000) {
            try {
                clock();
            } catch(Exception e){
                logger.error("Error while executing", e);
            }
            i++;
        }
    }
}
