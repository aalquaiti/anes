package me.aymen.anes;

import me.aymen.anes.memory.Bus;
import me.aymen.anes.memory.Cartridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Hello world!
 *
 */
public class App 
{
    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main( String[] args ) {
//        // TODO Create an NES Board that do this functinalities
//        Bus bus = new Bus();
//        Cartridge c = new Cartridge(bus);
//        c.load("test roms/nestest.nes");
//        CPU cpu = new CPU(bus);
//        // TODO REMOVE these settings. Only used for testing nestest.nes
//        cpu.setPC(0xC000);
//        cpu.decSP();
//        cpu.decSP();
//        CPUStatus status = null;
//        int i = 0;
//        while (i < 10000) {
//            try {
//
//
//                status = cpu.tick();
//                String message = String.format("%-50s%s", Deassembler
//                        .analyse(status), Deassembler.showStatus(status));
//                System.out.println(message + " TICK: " + i);
//
//            } catch(Exception e){
//                //logger.error("Error while executing", e);
//            }
//            i++;
//        }

        int screenWidth = 640;
        int screenHeight = 480;
        int logicalWidth = 320;
        int logicalHeight = 240;
        Screen s = new Screen(screenWidth, screenHeight);

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for(int x =0; x < logicalWidth; x++) {
                for(int y = 0; y < logicalHeight; y++) {
                    int R = (int)(Math.random()*256);
                    int G = (int)(Math.random()*256);
                    int B= (int)(Math.random()*256);
                    Color color = new Color(R, G, B);
                    s.setPixel(x, y, color);
                }
            }

            s.repaint();

        }
    }
}
