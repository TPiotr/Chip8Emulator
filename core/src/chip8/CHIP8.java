package chip8;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main class of this emulator. Fetch, decode, execute instruction by instruction
 * Created by RYZEN on 31.03.2018.
 */

public class CHIP8 {

    /**
     * Chip components
     */
    private Memory mem;
    private Keyboard keyboard;
    private GFX gfx;

    /**
     * Array containing code lines indexes where breakpoint occurs (if current line == breakpoint_index return)
     */
    private ArrayList<Integer> breakpoints;

    /**
     * Flag determining if chip will log about every operation in console
     */
    private final boolean PRINT_DEBUG = false;

    /**
     * loaded - variable informing us if program is fully loaded into memory
     * shutoff - variable used to break while(true) loop when user is destroying this chip instance
     */
    private boolean loaded, shutoff;

    /**
     * Flag determining if current code line is breakpoint one, (so tells if emulator is suspended)
     */
    private boolean breakpoint;
    /**
     * Index of code line where breakpoint occurs (grab info from this only if breakpoint = true
     */
    private int breakpoint_line;

    /**
     * Timers properties, with that we have constant timers decreasing speed (decrease 1 after update_after time passed), change update_after value to new if you want (now set to 200ms)
     */
    private long last_timer_update = 0, update_after = 200;


    /**
    * String that contains whole program nicely formatted
    * ====
    * LINE_NUMBER: OPCODE
    * ====
    */
    private String program_instructions;

    /**
     * Construct new chip instance
     */
    public CHIP8() {
        mem = new Memory();
        keyboard = new Keyboard(mem);
        gfx = new GFX(mem);

        breakpoints = new ArrayList<Integer>();
    }

    /**
     * Load program into memory from byte array
     * @param program_data array contains program data
     */
    public void load(byte[] program_data) {
        mem.pc = 512;
        mem.I = 0;
        mem.sp = 0;
        loaded = false;
        mem.delay_timer = 0;
        mem.sound_timer = 0;

        //clear screen
        gfx.clear();

        //clear registers
        for(int i = 0; i < mem.V.length; i++)
            mem.V[i] = 0;

        //clear memory
        for(int i = 0; i < mem.memory.length; i++)
            mem.memory[i] = 0;

        //load font into memory
        for(int i = 0; i < mem.chip8_fontset.length; i++) {
            byte val = (byte) mem.chip8_fontset[i];
            mem.memory[i] = val;
        }

        //load program into memory
        int program_size = program_data.length;
        for(int i = 0; i < program_size; i++) {
            mem.memory[i + 512] = program_data[i]; //512 bytes offset because at this address program memory section starts
        }

        //save program instructions to string and print to screen
        StringBuilder program_instruction_set = new StringBuilder();
        System.out.println("------\nProgram opcodes list:");
        for(int i = 0; i < program_size; i += 2) {
            int opcode = (short) ((mem.get(i + 512) << 8) & 0xFF00) | (mem.get(i + 512 + 1) & 0x00FF);

            //remove first FFFF if occurs to make printed code just cleaner
            String opcode_string = hex(opcode).toUpperCase();
            if(opcode_string.startsWith("FFFF"))
                opcode_string = opcode_string.replaceFirst("FFFF", "");

            program_instruction_set.append(i / 2).append(": ").append(opcode_string).append("\n");
        }

        program_instructions = program_instruction_set.toString();
        System.out.println(program_instructions);
        System.out.println("------");

        //set load flag to true so we can begin with emulation process
        loaded = true;
    }

    /**
     * Fetch, decode and handle 1 opcode, (In my case calling this method with frequency of 500Hz is fine)
     */
    public void emulationStep() {
        //check if program is loaded to memory
        if(!isLoaded())
            return;

        //check if this line is on breakpoints list if is return until it will be removed by user
        int line_code_index = (mem.pc - 512) / 2;
        if(breakpoints.contains(line_code_index)) {
            breakpoint = true;
            breakpoint_line = line_code_index;

            return;
        }
        breakpoint = false;

        //update timers
        if(System.currentTimeMillis() - last_timer_update > update_after) {
            if (mem.sound_timer > 0)
                mem.sound_timer--;

            if (mem.delay_timer > 0)
                mem.delay_timer--;

            last_timer_update = System.currentTimeMillis();
        }

        //grab current opcode from memory, every opcode on chip8 is 2 bytes so we need to connect two bytes from memory into one opcode
        int opcode = (short) ((mem.get(mem.pc) << 8) & 0xFF00) | (mem.get(mem.pc + 1) & 0x00FF);

        println("========= \n" + String.format("opcode: %01X", opcode)); //debug print what current opcode is

        //just grab every 'piece' of opcode to make code for interpreting opcode cleaner
        int first = (opcode & 0xF000) >> 12; //shift with proper amount of bits (4 per digit) because without this result from f.e. 0x1234 would be 0x1000 instead of 0x1
        int second = (opcode & 0x0F00) >> 8;
        int third = (opcode & 0x00F0) >> 4;
        int fourth = (opcode & 0x000F);

        //debug
        println("first: " + hexDec(first) + " second: " + hexDec(second) + " third: " +hexDec(third) + " fourth: " + hexDec(fourth) + " pc: " + (mem.pc / 2) + "(" + (mem.pc - 512) / 2 + ")" + " sp: " + mem.sp);

        int last3 = opcode & 0x0FFF; //NNN
        int last2 = opcode & 0x00FF; //NN

        //step forward one opcode
        mem.pc += 2;

        //decode first 4 bytes of opcode
        //names of instruction and definitions from wikipedia https://en.wikipedia.org/wiki/CHIP-8 (section Opcode table)
        switch(opcode & 0xF000) {
            //0x00 opcodes
            case 0x00:
                switch(opcode & 0x000F) {
                    case 0x0000: // 0x00E0: Clears the screen
                        println("0x00E0");

                        gfx.clear();
                        break;

                    case 0x000E: // 0x00EE: Returns from subroutine
                        println("0x00EE");

                        mem.sp -= 1;
                        mem.pc = mem.stack[mem.sp];

                        break;

                    default:
                        System.err.println("Unknown 0x00 opcode: " + String.format("%01X", opcode));
                        break;
                }
                break;

            case 0x1000: //1NNN goto
                println("1NNN NNN: " + hexDec(last3));

                mem.pc = last3;
                break;

            case 0x2000: //2NNN call subroutine at NNN
                mem.stack[mem.sp] = mem.pc;
                mem.sp++;
                mem.pc = last3;

                println("2NNN NNN: " + hexDec(last3));

                break;

            case 0x3000: //3XNN Skips the next instruction if VX equals NN
                if(mem.V[second] == last2)
                    mem.pc += 2;

                println("3XNN X: " + second + " NN: " + hexDec(last2));

                break;

            case 0x4000: //4XNN Skips the next instruction if VX doesn't equal NN.
                if(mem.V[second] != last2)
                    mem.pc += 2;

                println("4XNN X: " + second + " NN: " + hexDec(last2));

                break;

            case 0x5000: //5XY0 Skips the next instruction if VX equals VY.
                println("5XY0 X: " + hexDec(second) + " Y: " + hexDec(third));

                if(mem.V[second] == mem.V[third])
                    mem.pc += 2;

                break;

            case 0x6000: //6XNN Sets VX to NN.
                println("6XNN X: " + hexDec(second) + " NN: " + hexDec(last2));

                mem.V[second] = (byte) last2;
                break;

            case 0x7000: //7XNN Adds NN to VX. (Carry flag is not changed)
                println("7XNN X: " + hexDec(second) + " NN: " + hexDec(last2));

                int add_val = (last2) + mem.V[second];
                if(add_val > 255) {
                    add_val -= 256;
                }

                mem.V[second] = (byte) add_val;
                break;

            case 0x8000:
                switch(opcode & 0x000F) {
                    case 0x0000: //8XY0 Sets VX to the value of VY.
                        println("8XY0 X: " + hexDec(second) + " Y: " + hexDec(third));

                        mem.V[second] = mem.V[third];
                    break;

                    case 0x0001: //8XY1 Sets VX to VX or VY. (Bitwise OR operation)
                        println("8XY1 X: " + hexDec(second) + " Y: " + hexDec(third));

                        mem.V[second] = (byte) (mem.V[second] | mem.V[third]);
                        break;

                    case 0x0002: //8XY2 Sets VX to VX and VY. (Bitwise AND operation)
                        println("8XY2 X: " + hexDec(second) + " Y: " + hexDec(third));

                        mem.V[second] = (byte) (mem.V[second] & mem.V[third]);
                        break;

                    case 0x0003: //8XY3 Sets VX to VX xor VY
                        println("8XY3 X: " + hexDec(second) + " Y: " + hexDec(third));

                        mem.V[second] = (byte) (mem.V[second] ^ mem.V[third]);
                        break;

                    case 0x0004: //8XY4 Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't
                        println("8XY4 X: " + hexDec(second) + " Y: " + hexDec(third));

                        byte add_result = (byte) (mem.V[second] + mem.V[third]);

                        //if one of the parameters of add operation is bigger than result there is carry
                        if((add_result & 0xff) < (mem.V[third] & 0xff) || (add_result & 0xff) < (mem.V[second] & 0xff)) { //remember to grab bytes without signs
                            mem.V[15] = 1;
                        } else{
                            mem.V[15] = 0;
                        }

                        mem.V[second] = add_result;
                        break;

                    case 0x0005: //8XY5 VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                        println("8XY5 X: " + hexDec(second) + " Y: " + hexDec(third));

                        byte sub_result = (byte) (mem.V[second] - mem.V[third]); //yes from this could be negative number but when deciding if borrow is 1 or 0 sign is skipped

                        //if first parameter in sub operation is less than second there will be negative number so set borrow to 1
                        if((mem.V[second] & 0xff) > (mem.V[third] & 0xff)) { //remember to grab unsigned byte from signed one
                            mem.V[15] = 1;
                        } else {
                            mem.V[15] = 0;
                        }

                        mem.V[second] = sub_result;
                        break;

                    case 0x0006: //8XY6 Shifts VY right by one and copies the result to VX. VF is set to the value of the least significant bit of VY before the shift.
                        println("8XY6 X: " + hexDec(second) + " Y: " + hexDec(third));

                        byte right_least_significant = (byte)(mem.V[second] & (byte) 0x01);
                        mem.V[0xF] = right_least_significant; //Set VF to the least significant bit of VX before shifting operation

                        int right_vx = (mem.V[second] & 0xff);
                        mem.V[second] = (byte) (right_vx >>> 1); // >>> operator means right shift one bit without care about sign bit
                        break;

                    case 0x0007: //8XY7 Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                        println("8XY7 X: " + hexDec(second) + " Y: " + hexDec(third));

                        //welp almost same thing as in 8XY5
                        byte subn_result = (byte) (mem.V[third] - mem.V[second]);

                        if((mem.V[third] & 0xff) > (mem.V[second] & 0xff)) { //as always grab byte without sign bit
                            mem.V[0xF] = 1;
                        } else {
                            mem.V[0xF] = 0;
                        }

                        mem.V[second] = subn_result;
                        break;

                    case 0x000E: //8XYE Shifts VY left by one and copies the result to VX. VF is set to the value of the most significant bit of VY before the shift.
                        println("8XYE X: " + hexDec(second) + " Y: " + hexDec(third));

                        byte rbit_most_significant = (byte)(mem.V[second] & 0x80); //0x80 == 128
                        if(rbit_most_significant != 0) {
                            rbit_most_significant = 1; //if 0x10000000 -> set to 0x01
                        }
                        mem.V[15] = rbit_most_significant; //Set VF to the most significant bit of VX before shifting operation

                        int rbit_vx = (mem.V[second] & 0xff); //int -> uint just like in 8XY6
                        mem.V[second] = (byte) (rbit_vx << 1);
                        break;

                    default:
                        System.err.println("Unknown 0x8 opcode: " + String.format("%01X", opcode));
                        break;
                }
                break;

            case 0x9000: //9XY0 Skips the next instruction if VX doesn't equal VY. (Usually the next instruction is a jump to skip a code block)
                println("9XY0 X: " + hexDec(second) + " Y: " + hexDec(third));

                if(mem.V[second] != mem.V[third])
                    mem.pc += 2;

                break;

            case 0xA000: //ANNN Sets I to the address NNN.
                println("ANNN NNN: " + hexDec(last3));

                mem.I = last3;
                break;

            case 0xB000: // BNNN Jumps to the address NNN plus V0.
                println("BNNN NNN: " + hexDec(last3));

                mem.pc = mem.V[0] + last3;
                break;

            case 0xC000: //CXNN Sets VX to the result of a bitwise and operation on a random number (Typically: 0 to 255) and NN.
                println("CXNN NN: " + hexDec(last2));

                mem.V[second] = (byte) (MathUtils.random(0, 255) & last2);
                break;

            case 0xD000: //DXYN Draws a sprite at coordinate (VX, VY) that has a width of 8 pixels and a height of N pixels. Each row of 8 pixels is read as bit-coded starting from memory location I;
                        // I value doesn’t change after the execution of this instruction.
                        // As described above, VF is set to 1 if any screen pixels are flipped from set to unset when the sprite is drawn, and to 0 if that doesn’t happen

                println("DXYN X: " + hexDec(second) + " Y: " + hexDec(third) + " N: " + hexDec(fourth));

                gfx.draw(second, third, fourth);
                break;

            case 0xE000:
                switch(opcode & 0x00FF) {
                    case 0x009E: //EX9E Skips the next instruction if the key stored in VX is pressed. (Usually the next instruction is a jump to skip a code block)
                        println("EX9E X: " + hexDec(second));

                        if(mem.key[mem.V[second]] == 1)
                            mem.pc += 2;

                        break;

                    case 0x00A1: //EXA1 Skips the next instruction if the key stored in VX isn't pressed. (Usually the next instruction is a jump to skip a code block)
                        println("EXA1 X: " + hexDec(second));

                        if(mem.key[mem.V[second]] != 1)
                            mem.pc += 2;

                        break;

                    default:
                        System.err.println("Unknown 0xE opcode: " + String.format("%01X", opcode));
                        break;
                }
                break;

            case 0xF000:
                switch(opcode & 0x00FF) {
                    case 0x0007: //FX07 Sets VX to the value of the delay timer.
                        println("FX07 X: " + hexDec(second));

                        mem.V[second] = (byte) mem.delay_timer;
                        break;

                    case 0x000A: //key press is awaited, and then stored in VX. (Blocking Operation. All instruction halted until next key event)
                        println("FX0A X: " + hexDec(second));

                        final AtomicBoolean loop = new AtomicBoolean(true);
                        final AtomicInteger pressed_key = new AtomicInteger(0);

                        Keyboard.KeyCallback callback = new Keyboard.KeyCallback() {
                            @Override
                            public void pressed(int keycode) {
                                pressed_key.set(keycode);
                                loop.set(false);
                            }
                        };
                        keyboard.waitForKeyPress(callback);

                        //wait until some key will be pressed, or break loop if chip is shutdown flag is true
                        while(loop.get() && !shutoff) {
                            if(!keyboard.getPendingCallbacks().contains(callback) || shutoff) {
                                break;
                            }
                        }

                        mem.V[second] = (byte) pressed_key.get();
                        break;

                    case 0x0015: //FX15 Sets the delay timer to VX.
                        println("FX15 X: " + hexDec(second));

                        mem.delay_timer = mem.V[second];
                        break;

                    case 0x0018: //FX18 Sets the sound timer to VX.
                        println("FX18 X: " + hexDec(second));

                        mem.sound_timer = mem.V[second];
                        break;

                    case 0x001E: //FX1E Adds VX to I.
                        println("FX1E X: " + hexDec(second));

                        mem.I += mem.V[second];
                        break;

                    case 0x0029: //FX29 Sets I to the location of the sprite for the character in VX. Characters 0-F (in hexadecimal) are represented by a 4x5 font.
                        println("FX29 X: " + hexDec(second));

                        mem.I = (short) (Memory.hexadecimalSpritesStartAddress + 5 * mem.V[second]);
                        break;

                    case 0x0033: //FX33 Stores the binary-coded decimal representation of VX, with the most significant of three digits at the address in I, the middle digit at I plus 1,
                                    // and the least significant digit at I plus 2.
                                    // (In other words, take the decimal representation of VX, place the hundreds digit in memory at location in I, the tens digit at location I+1, and the ones digit at location I+2.)
                        println("FX33 X: " + hexDec(second));

                        int start_memory_addr = mem.I;
                        int fx33_vx = mem.V[second] & 0xff; //get uint from register VX

                        int fx33_hundreds = fx33_vx / 100; //hundreds
                        fx33_vx = fx33_vx - fx33_hundreds * 100;

                        int fx33_tens = fx33_vx / 10; //tens
                        fx33_vx = fx33_vx - fx33_tens * 10;

                        int fx33_units = fx33_vx; //units

                        mem.set(start_memory_addr, (byte) fx33_hundreds);
                        mem.set((start_memory_addr + 1), (byte) fx33_tens);
                        mem.set((start_memory_addr + 2), (byte) fx33_units);
                        break;

                    case 0x0055: //FX55 Stores V0 to VX (including VX) in memory starting at address I. I is increased by 1 for each value written.
                        println("FX55 X: " + hexDec(second));

                        for(byte reg = 0; reg <= second; reg++){
                            mem.set((short) mem.I + reg, mem.V[reg]);
                        }

                        break;

                    case 0x0065: //FX65 Fills V0 to VX (including VX) with values from memory starting at address I. I is increased by 1 for each value written.
                        println("FX65 X: " + hexDec(second));

                        for(int i = 0; i <= second; i++) {
                            mem.V[i] = mem.get(mem.I + i);
                        }

                        break;

                    default:
                        System.err.println("Unknown 0xF opcode: " + String.format("%01X", opcode));
                        break;
                }

                break;

            default:
                System.err.println(String.format("Unknown opcode: %01X", opcode));
                break;
        }

    }

    /**
     * Print String s to console if print_debug flag is set to true
     * @param s string that will be printed to console if print_debug flag = true
     */
    private void println(String s) {
        if(PRINT_DEBUG)
            System.out.println(s);
    }


    /*
     *
     * Method used to grab value stored and formatted in string for debug purposes (to print in console)
     *
     */

    private String bin(int val) {
        return Integer.toBinaryString(val);
    }

    private String hex(int val) {
        return Integer.toHexString(val);
    }

    private String hexDec(int val) {
        return hex(val) + "(" + val + ")";
    }

    /**
     * Flag determining if loading program into memory is done
     * @return flag determining if loading program into memory is done
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Call it before setting you chip instance to null to avoid blocking chip block because of some non broken while(true) loop which still waits for key input
     * Method that sets shutoff flag to true
     */
    public void shutoff() {
        shutoff = true;
    }

    /**
     * @return Flag determining if emulator is stuck on breakpoint line
     */
    public boolean isBreakpoint() {
        return breakpoint;
    }

    /**
     * Use only if isBreakpoint() = true
     * @return number of line of code were breakpoint occurs
     */
    public int getBreakpointLine() {
        return breakpoint_line;
    }

    /**
     * Array containing all breakpoints.
     * Add, remove breakpoints directly to this exposed array
     * @return array containing all current breakpoints
     */
    public ArrayList<Integer> getBreakpoints() {
        return breakpoints;
    }

    /**
     * @return String which contains whole list of loaded program instruction in hex format and dec format of line number, ready for f.e. showing to user what is loaded or whatever
     */
    public String getProgramInstructions() {
        return program_instructions;
    }

    /**
     * Getter for gfx instance, responsible for storing pixels state and drawing sprites
     * @return gfx instance
     */
    public GFX getGFX() {
        return gfx;
    }

    /**
     * Getter for memory instance, responsible for storing registers, memory, timers etc.
     * @return memory instance
     */
    public Memory getMemory() {
        return mem;
    }

    /**
     * Interface used to send key events to chip memory
     * @return keyboard instance
     */
    public Keyboard getKeyboard() {
        return keyboard;
    }
}
