package libgdx.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

import java.awt.Frame;
import java.awt.Window;

import chip8.CHIP8;

/**
 * Created by RYZEN on 03.04.2018.
 */

public class RegistersWindow extends VisWindow {

    private IntSpinnerModel[] registers_view;
    private CHIP8 last_chip;

    public RegistersWindow() {
        super("Registers");

        setResizable(true);
        addCloseButton();

        Table table = new Table();
        registers_view = new IntSpinnerModel[16];

        for(int i = 0; i < 16; i++) {
            final int index = i;
            final IntSpinnerModel int_model = new IntSpinnerModel(0, 0, 256, 1);
            Spinner spinner = new Spinner("V" + i, int_model);
            table.add(spinner).row();

            //add listener
            spinner.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    int val = int_model.getValue();

                    if(last_chip != null) {
                        //you can modify registers only on break point !!
                        if(last_chip.isBreakpoint()) {
                            last_chip.getMemory().V[index] = (byte) val;
                        }
                    }
                }
            });

            registers_view[i] = int_model;
        }

        Table tab = new Table();
        tab.left().add(table);

        VisScrollPane scroll = new VisScrollPane(tab);
        scroll.setFlickScroll(false);
        scroll.setFadeScrollBars(false);

        left().add(scroll).growX().row();

        pack();
    }

    @Override
    protected void close() {
        this.setVisible(false);
    }

    public void update(CHIP8 chip) {
        if(chip == null)
            return;

        this.last_chip = chip;

        if(chip.isBreakpoint())
            return;

        for(int i = 0; i < chip.getMemory().V.length; i++) {
            registers_view[i].setValue(chip.getMemory().V[i]);
        }
    }
}
