package me.aymen.anes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class App 
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main( String[] args ) {
            Nes nes = new Nes();

            nes.start();
    }

//        int screenWidth = 1024;
//        int screenHeight = 960;
//        int logicalWidth = 256;
//        int logicalHeight = 240;
//        Screen s = new Screen(screenWidth, screenHeight);
//
//        while (true) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            for(int x =0; x < logicalWidth; x++) {
//                for(int y = 0; y < logicalHeight; y++) {
//                    int R = (int)(Math.random()*256);
//                    int G = (int)(Math.random()*256);
//                    int B= (int)(Math.random()*256);
//                    Color color = new Color(R, G, B);
//                    s.setPixel(x, y, color);
//                }
//            }
//
//            s.repaint();

//        }
}
