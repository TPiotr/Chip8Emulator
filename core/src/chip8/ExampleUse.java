package chip8;

/**
 * Created by RYZEN on 14.04.2018.
 */

public class ExampleUse {

    public static void main(String[] args) {
        ExampleUse use = new ExampleUse();
        use.setup();
    }

    public void setup() {
        final CHIP8 chip = new CHIP8(); //create chip instance

        //load program into chip memory
        byte[] program_bytes = new byte[1]; //= file.readBytes(); or whatever what suits your needs
        chip.load(program_bytes);

        //run chip emulation on other thread
        Thread chip_thread = new Thread(new Runnable() {

            @Override
            public void run() {
                final float UPS = 500; //500 steps each second of emulation
                final float sleep_time = 1f / UPS * 1000f; //calculate period from frequency (in milis thats why * 1000) so we know for how long thread must sleep each frame

                while(true) {
                    chip.emulationStep();

                    try {
                        Thread.sleep((long) sleep_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        chip_thread.start();

        //setup keyboard input
        /**
         * pseudo code
         *
         * if(keyPressed('A'))
         *      chip.getKeyboard().justSet(10, 1); //key 10, pressed
         *
         * if(keyReleased('A'))
         *      chip.getKeyboard().justSet(10, 0);
         */

        //somewhere in render method

        /**
         * render method somewhere in your code, just if pixel val if true we have to draw square somewhere f.e. as letter in console or white rect on screen using opengl

           float tile_size_x = 16;
           float tile_size_y = tile_size_x;
           for (int i = 0; i < chip.getGFX().getPixelsBuffer().length; i++) {
                for (int j = 0; j < chip.getGFX().getPixelsBuffer()[0].length; j++) {
                    boolean pixel = chip.getGFX().getPixelsBuffer()[i][(chip.getGFX().getPixelsBuffer()[0].length - 1) - j];

                    if (pixel)
                        drawSquare(i * tile_size_x, j * tile_size_y, tile_size_x, tile_size_y); //drawSquare(x, y, w, h)
                }
           }
         */

        //on app exit
        chip.shutoff();
        chip_thread.interrupt();
    }

}
