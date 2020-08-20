package me.aymen.anes;

import me.aymen.anes.cpu.CPU;
import me.aymen.anes.io.Bus;
import me.aymen.anes.ppu.PPU;
import me.aymen.anes.ppu.Screen;
import me.aymen.anes.util.DeAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        bus.rom.load("test roms/scanline.nes");
        cpu.reset();

    }

    public void clock() {
        bus.clock();

        if (cpu.isComplete()) {
            ticks++;
            String ppuStatus = String.format("\tPPU: %03d, %03d  v: $%04X  " +
                            "t: $%04X", ppu.scanLine, ppu.cycle,
                    ppu.ppu_v.getValue(), ppu.ppu_t.getValue());
            String message = String.format("Tick: %05d %-50s%s", ticks,
                    deAssmb.analyse(cpu.getStatus()),
                    deAssmb.showStatus(cpu.getStatus(), ppuStatus));
            System.out.println(message);
        }


        // TODO remove this hack
        if(ppu.cycle >= 1 && ppu.cycle <= 256 && ppu.scanLine >= 0 && ppu.scanLine <= 239 && ppu.renderEnabled()) {

            for (int y = 0; y < 30; y++) {
                for (int x = 0; x < 32; x++) {
                    int offset = x  + y * 32;
                    int tileAddr = bus.ppuRead(ppu.getBaseNameTable() + offset);
                    int palette =  bus.ppuRead(
                            0x23C0 | ((y >> 2) << 3) | ((x >> 2))
                    );
//                    System.out.printf("%d : %d%n",offset, tileAddr);
//                    System.out.printf("$%04X%n", ppu.getBaseNameTable() + offset);
//                    System.out.println("address");
//                    System.out.println(ppu.getBaseNameTable() + offset);

                    for(int j = 0; j < 8; j++) {
                        int lsByte = bus.ppuRead(ppu.getBGPatternTableAddr() + tileAddr * 16 + j + 0);
                        int msByte = bus.ppuRead(ppu.getBGPatternTableAddr() + tileAddr * 16 + j + 8);

                        for (int i = 0; i < 8; i++) {
                            int lsbit = (lsByte >> 7 - i) & 0x1;
                            int msbit = (msByte >> 7 - i) & 0x1;
                            int pixel = (msbit << 1) | lsbit;

                            if ((y & 0x2) != 0) {
                                palette >>= 4;
                            }
                            if ((x & 0x2) != 0) {
                                palette >>= 2;
                            }
                            palette &= 0x3;
                            int xPos = i + x * 8;
                            int yPos = j + y * 8;
                            screen.setPixel(xPos, yPos, ppu.getPaletteColor(palette, pixel));
//                            System.out.println(String.format("(%d, %d)", xPos, yPos));
//                            try {
//                                Thread.sleep(50);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                            screen.repaint();
                        }
                    }
                }
            }

        }

        // Draw frame when complete
        if (ppu.isComplete()) {
            ppu.setComplete(false);
//            screen.repaint();
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
