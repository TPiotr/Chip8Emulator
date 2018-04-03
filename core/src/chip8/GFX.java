package chip8;

/**
 * Class holding pixels state and handling draw sprite opcode
 * Created by RYZEN on 31.03.2018.
 */

public class GFX {

    /**
     * Current pixels state array & buffer used in render process
     */
    private boolean pixels[][], pixels_buffer[][];

    /**
     * Memory instance used to access registers & draw flag
     */
    private Memory mem;

    /**
     * Resolution of screen
     */
    public static final int WIDTH = 64, HEIGHT = 32;

    /**
     * Construct new GFX class instance
     * @param mem memory instance
     */
    public GFX(Memory mem) {
        this.mem = mem;

        pixels = new boolean[WIDTH][HEIGHT];
        pixels_buffer = new boolean[WIDTH][HEIGHT];
    }

    /**
     * Clear screen (just set all pixels to false)
     */
    public void clear() {
        for(int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[0].length; j++) {
                pixels[i][j] = false;
                pixels_buffer[i][j] = false;
            }
        }
    }

    /**
     * Method handling DXYN opcode
     * @param x X
     * @param y Y
     * @param nibble N
     */
    public void draw(int x, int y, int nibble) {
        byte offset = 0;

        byte vf = 0;
        while(offset < nibble) {

            byte curr_byte = mem.get((short) (mem.I + offset)); //read one byte
            for(int i = 0; i <= 7; i++) {

                //transform local into real cords
                int local_x = mem.V[x] & 0xff;
                int local_y = mem.V[y] & 0xff;
                int real_x = (local_x + i) % WIDTH;
                int real_y = (local_y + offset) % HEIGHT;

                boolean previous_pixel = pixels[real_x][real_y]; //previous state of pixel
                boolean new_pixel = previous_pixel ^ isBitSet(curr_byte,7 - i); //new state of pixel

                pixels[real_x][real_y] = new_pixel;

                if(previous_pixel && !new_pixel) {
                    vf = 1; //pixel erased
                }
            }

            mem.V[15] = vf; //update VF register (1 = pixel has been erased)
            offset++;
        }

        mem.draw_flag = true;
    }

    /**
     * Call it before your rendering process, copies current pixels state to buffer and because of that removes flickering effect
     */
    public void update() {
        if(mem.draw_flag) {
            copyPixels();
            mem.draw_flag = false;
        }
    }

    /**
     * After drawing process copy pixels to buffer which is used in rendering process
     */
    private void copyPixels() {
        for(int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                pixels_buffer[x][y] = pixels[x][y];
            }
        }
    }

    /**
     * Check if bit on bit index place in byte b is 1
     * @param b byte
     * @param bit bit index
     * @return true if bit on bit index in byte b is equal to 1
     */
    private boolean isBitSet(byte b, int bit) {
        return (b & (1 << bit)) != 0;
    }

    /**
     * Getter for pixels buffer, use this buffer to grab info which pixel should be rendered
     * @return array holding state about pixels
     */
    public boolean[][] getPixelsBuffer() {
        return pixels_buffer;
    }
}
