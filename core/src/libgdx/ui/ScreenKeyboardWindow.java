package libgdx.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;

import libgdx.EmulatorMain;

/**
 * Created by RYZEN on 02.04.2018.
 */

public class ScreenKeyboardWindow extends VisWindow {

    private EmulatorMain emu;

    public ScreenKeyboardWindow(EmulatorMain emu) {
        super("Input");

        this.emu = emu;

        setResizable(true);
        addCloseButton();

        Table _123c = new Table();
        _123c.add(createButton("1", 1));
        _123c.add(createButton("2", 2));
        _123c.add(createButton("3", 3));
        _123c.add(createButton("C", 12));

        Table _456d = new Table();
        _456d.add(createButton("4", 4));
        _456d.add(createButton("5", 5));
        _456d.add(createButton("6", 6));
        _456d.add(createButton("D", 13));

        Table _789e = new Table();
        _789e.add(createButton("7", 7));
        _789e.add(createButton("8", 8));
        _789e.add(createButton("9", 9));
        _789e.add(createButton("E", 14));

        Table _A0BF = new Table();
        _A0BF.add(createButton("A", 10));
        _A0BF.add(createButton("0", 0));
        _A0BF.add(createButton("B", 11));
        _A0BF.add(createButton("F", 15));

        Table tab = new Table();
        tab.add(_123c).row();
        tab.add(_456d).row();
        tab.add(_789e).row();
        tab.add(_A0BF).row();

        add(tab);
        pack();
    }

    private Table createButton(String letter, final int index) {
        VisTextButton btn = new VisTextButton(letter);

        btn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(emu.getChip() != null)
                    emu.getChip().getKeyboard().justSet(index, (byte) 1);

                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(emu.getChip() != null)
                    emu.getChip().getKeyboard().justSet(index, (byte) 0);

                super.touchUp(event, x, y, pointer, button);
            }
        });

        return btn.pad(10);
    }

    @Override
    protected void close() {
        setVisible(false);
    }
}
