package chip8;

import java.util.ArrayList;

/**
 * Class responsible for sending input state to chip
 * With this class you can use any library for handling input, just call justSet() method when you need to change chip input state
 * Created by RYZEN on 01.04.2018.
 */

public class Keyboard {

    /**
     * Memory instance, place where input state is stored
     */
    private Memory memory;

    /**
     * Interface used by chip8 class with FX0A opcode
     */
    public interface KeyCallback {
        void pressed(int keycode);
    }
    private ArrayList<KeyCallback> callbacks;

    /**
     * Construct new keyboard class instance
     * @param memory memory instance used to modify keys state stored there
     */
    public Keyboard(Memory memory) {
        this.memory = memory;

        callbacks = new ArrayList<KeyCallback>();
    }

    /**
     * Call this function in your own handling input methods (f.e ifKeyPressed('A') { chip.getKeyboard().justSet(10, 1); } ifKeyReleased...)
     * @param index index of key that state was changed (0 - 15)
     * @param value new state of that key (1 - pressed, 0 - nope)
     */
    public synchronized void justSet(final int index, final byte value) {
        //System.out.println("I: " + index + " V: " + value);

        synchronized (callbacks) {
            if (value == 1) {
                for (KeyCallback callback : callbacks) {
                    callback.pressed(index);
                }
            }
            callbacks.clear();
        }

        memory.key[index] = value;
    }

    /**
     * Submit new callback which will return first pressed key from now
     * @param callback callback instance
     */
    public void waitForKeyPress(KeyCallback callback) {
        callbacks.add(callback);
    }

    /**
     * @return Flag determining if there are any pending key callbacks waiting for some key to be pressed
     */
    public boolean isWaitingForKey() {
        return callbacks.size() > 0;
    }

    /**
     * Array that contains callbacks that are waiting for some key to be pressed by user
     * @return callbacks array
     */
    public ArrayList<KeyCallback> getPendingCallbacks() {
        return callbacks;
    }
}
