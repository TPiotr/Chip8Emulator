package libgdx.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;

import libgdx.EmulatorMain;

/**
 * Created by RYZEN on 02.04.2018.
 */

public class ToolsWindow extends VisWindow {

    public ToolsWindow(final EmulatorMain main) {
        super("Tools");

        setResizable(false);

        //load program button
        VisTextButton load_another_program_button = new VisTextButton("Load program", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                main.loadAnotherProgram();
            }
        });
        add(load_another_program_button);
    }

    public ToolsWindow createToggleButtonForWindow(final VisWindow window) {
        VisTextButton button = new VisTextButton(window.getTitleLabel().getText().toString(), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                window.setVisible(!window.isVisible());

                if(window.isVisible())
                    window.toFront();
            }
        });
        add(button).padLeft(5);

        window.setVisible(false);
        pack();

        return this;
    }
}
