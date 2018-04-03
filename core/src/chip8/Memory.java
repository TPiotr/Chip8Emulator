package chip8;

/**
 * Class that contains main memory, registers, timers etc
 * Created by RYZEN on 31.03.2018.
 */

public class Memory {

    /** JUST STATIC FONT DATA **/
    public short[] chip8_fontset = new short[] {
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    /**
     * 16 (V0, V1 to VE + carry register(VF)) registers
     */
    public byte[] V;

    /**
     * Index register
     */
    public int I;
    /**
     * Program counter
     */
    public int pc;

    /**
     * offset address where sprites memory part starts, well just 0
     */
    public static short hexadecimalSpritesStartAddress = 0x000;
    /**
     * Byte array, memory, 4096 bytes
     */
    public byte[] memory;

    /**
     * 16 levels of stack
     */
    public int[] stack;
    /**
     * Stack pointer
     */
    public int sp;

    /**
     * Keys state (1 - pressed, 0 - released)
     */
    public byte[] key; //16 keys as input

    /**
     * Draw flag, if true chip requests redraw after drawing process set it manually to false
     * (chip sets it in GFX class)
     */
    public boolean draw_flag;


    /**
     * Delay & sound timer
     */
    public int delay_timer, sound_timer;

    /**
     * Construct new memory instance, just create all needed variables
     */
    public Memory() {
        V = new byte[16];
        memory = new byte[4096];
        stack = new int[16];
        key = new byte[16];
    }

    /**
     * Set memory byte at given address
     * @param address address 0 - 4095
     * @param value new memory value
     */
    public void set(int address, byte value) {
        if(address >= memory.length || address < 0) {
            System.err.println("Invalid memory address! (set " + address + ")");
            return;
        }

        memory[address] = value;
    }

    /**
     * Get memory byte at given address
     * @param address address of byte
     * @return memory byte
     */
    public byte get(int address) {
        if(address >= memory.length || address < 0) {
            System.err.println("Invalid memory address! (get " + address + ")");
            return 0;
        }

        return memory[address];
    }
}
