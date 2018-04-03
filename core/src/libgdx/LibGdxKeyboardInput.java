package libgdx;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

import java.util.HashMap;

/**
 * Created by RYZEN on 02.04.2018.
 */

public class LibGdxKeyboardInput {

    private static HashMap<Integer, Integer> key_bindings = new HashMap<Integer, Integer>();
    static {
        int i = 0;
        key_bindings.put(Input.Keys.Q, i++); //0
        key_bindings.put(Input.Keys.W, i++);
        key_bindings.put(Input.Keys.E, i++);
        key_bindings.put(Input.Keys.R, i++);
        key_bindings.put(Input.Keys.T, i++);
        key_bindings.put(Input.Keys.Y, i++);
        key_bindings.put(Input.Keys.U, i++);
        key_bindings.put(Input.Keys.I, i++);
        key_bindings.put(Input.Keys.O, i++);
        key_bindings.put(Input.Keys.P, i++);
        key_bindings.put(Input.Keys.A, i++);
        key_bindings.put(Input.Keys.S, i++);
        key_bindings.put(Input.Keys.D, i++);
        key_bindings.put(Input.Keys.F, i++);
        key_bindings.put(Input.Keys.G, i++);
        key_bindings.put(Input.Keys.H, i); //15
    }

    public LibGdxKeyboardInput(final EmulatorMain main, InputMultiplexer multiplexer) {
        InputProcessor input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if(key_bindings.containsKey(keycode)) {
                    int index = key_bindings.get(keycode);

                    if(main.getChip() != null)
                        main.getChip().getKeyboard().justSet(index, (byte) 1);
                }

                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                if(key_bindings.containsKey(keycode)) {
                    int index = key_bindings.get(keycode);

                    if(main.getChip() != null)
                        main.getChip().getKeyboard().justSet(index, (byte) 0);
                }

                return false;
            }
        };
        multiplexer.addProcessor(input);
    }

}
