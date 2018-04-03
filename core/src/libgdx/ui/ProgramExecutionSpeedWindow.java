package libgdx.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

/**
 * Created by RYZEN on 02.04.2018.
 */

public class ProgramExecutionSpeedWindow extends VisWindow {

    private int current_freq = 500; //500 Hz default frequency

    public ProgramExecutionSpeedWindow() {
        super("Program speed");

        setResizable(false);
        addCloseButton();

        final IntSpinnerModel int_model = new IntSpinnerModel(current_freq, 1, 50000, 1);
        Spinner spinner = new Spinner("Frequency:", int_model);

        spinner.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                current_freq = int_model.getValue();
            }
        });

        add(spinner);
        pack();
    }

    @Override
    protected void close() {
        setVisible(false);
    }

    public int getFrequency() {
        return current_freq;
    }
}
