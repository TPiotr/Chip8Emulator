package libgdx.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

import libgdx.EmulatorMain;

/**
 * Created by RYZEN on 03.04.2018.
 */

public class StepSimulationWindow extends VisWindow {

    public StepSimulationWindow(final EmulatorMain emu) {
        super("Step simulation");

        setResizable(true);
        addCloseButton();

        Table table = new Table();
        final VisCheckBox step_simulation_enabled = new VisCheckBox("Active", false);
        step_simulation_enabled.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                emu.setStepSimulationEnabled(step_simulation_enabled.isChecked());
            }
        });

        final IntSpinnerModel step_size_model = new IntSpinnerModel(1, 0, 256, 1);
        Spinner step_size_spinner = new Spinner("Step size", step_size_model);
        step_size_spinner.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

            }
        });

        VisTextButton step_button = new VisTextButton("Step");
        step_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(step_simulation_enabled.isChecked()) {
                    if (emu.getChip() != null) {
                        if (!emu.getChip().getKeyboard().isWaitingForKey()) {
                            Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = 0; i < step_size_model.getValue(); i++) {
                                        emu.getChip().emulationStep();
                                    }
                                }
                            };
                            new Thread(r).start(); //run on another thread to now block whole program when chip will wait for some key pressed
                        }
                    }
                }
            }
        });

        table.add(step_simulation_enabled).padLeft(10);
        table.add(step_button).padLeft(10);
        table.add(step_size_spinner).padLeft(10);

        add(table);
        pack();
    }

    @Override
    protected void close() {
        setVisible(false);
    }
}
