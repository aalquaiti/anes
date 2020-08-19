package me.aymen.anes;

import me.aymen.anes.cpu.CPU;
import me.aymen.anes.io.Bus;
import me.aymen.anes.ppu.PPU;
import me.aymen.anes.ppu.Screen;
import me.aymen.anes.util.DeAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nes {
    private static final Logger logger = LoggerFactory.getLogger(Nes.class);
    private final Bus bus;
    private final CPU cpu;
    private final PPU ppu;
    private final Screen screen;
    private final DeAssembler deAssmb;
    private int ticks;

    public Nes() {
        bus = new Bus();
        cpu = bus.cpu;
        ppu = bus.ppu;
        screen = new Screen(768, 720);
        deAssmb = new DeAssembler(bus);
        bus.ppu.connect(screen);

        // TODO REMOVE these settings. Only used for testing nestest.nes
        bus.rom.load("test roms/nestest.nes");
        cpu.reset();

    }

    public void clock() {
        bus.clock();

//        if (cpu.isComplete()) {
//            ticks++;
//            String ppuStatus = String.format("PPU: %03d, %03d", ppu.scanLine,
//                    ppu.cycle);
//            String message = String.format("Tick: %05d %-50s%s", ticks,
//                    deAssmb.analyse(cpu.getStatus()),
//                    deAssmb.showStatus(cpu.getStatus(), ppuStatus));
//            System.out.println(message);
//        }

        // Draw frame when complete
        if (ppu.isComplete()) {
            ppu.setComplete(false);
            screen.repaint();
        }
    }

    public void start() {
        int i = 0;
        while (true) {
            try {
                clock();
            } catch(Exception e){
                System.err.println(e.getCause());
                e.printStackTrace();
            }
            i++;
        }
        //System.exit(0);
//        ColorPalette p = new ColorPalette();
//        Color[] colors = {
//                Color.WHITE,
//                p.color[0x08],
//                p.color[0x11],
//                p.color[0x23]
//        };

//        for(int j = 0; j < 16; j++) {
//            for (int i = 0; i < 16; i++) {
//                int index = i + j * 16;
//                int[][] tile = ppu.getTile(index);
//                for (int y = 0; y < 8; y++) {
//                    for (int x = 0; x < 8; x++) {
//                        int xPos = i * 8 + x;
//                        int yPos = j * 8 + y;
//                        screen.setPixel(xPos, yPos, colors[tile[x][y]]);
//                    }
//                }
//            }
//        }

//        int[][] palette = ppu.getPalette(0);
//        for(int y=0; y< 128; y++) {
//            for(int x =0; x < 128; x ++) {
//                screen.setPixel(x, y, colors[palette[x][y]]);
//            }
//        }
//
//        screen.repaint();

    }
}
