package libgdx.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisWindow;

import chip8.Memory;
import libgdx.EmulatorMain;

/**
 * Created by RYZEN on 02.04.2018.
 */

public class ProgramInstructionsWindow extends VisWindow {

    private VisLabel label_pc_sp;

    private EmulatorMain emu;

    public ProgramInstructionsWindow(EmulatorMain emu) {
        super("Program instructions");

        this.emu = emu;
        setResizable(true);

        label_pc_sp = new VisLabel();

        Table table = createCodeTable("");
        VisScrollPane scroll = new VisScrollPane(table);
        add(scroll);
        add(label_pc_sp);
    }

    private Table createCodeTable(String code) {
        Table main = new Table();
        TableUtils.setSpacingDefaults(this);

        EventListener listener = new EventListener() {
            @Override
            public boolean handle(Event event) {
                if(event instanceof ChangeListener.ChangeEvent) {
                    VisCheckBox box = (VisCheckBox) event.getListenerActor();

                    String[] name_parts = box.getLabel().getText().toString().split(":");
                    int line_num = Integer.parseInt(name_parts[0]);
                    //int opcode = Integer.decode("0x" + name_parts[1].replaceAll(" ", ""));

                    if(emu.getChip() != null) {
                        if(box.isChecked()) {
                            if(!emu.getChip().getBreakpoints().contains(line_num))
                                emu.getChip().getBreakpoints().add(line_num);
                        } else {
                            if(emu.getChip().getBreakpoints().contains(line_num))
                                emu.getChip().getBreakpoints().remove((Integer) line_num);
                        }
                    }
                }

                return false;
            }
        };

        //read line by line
        String[] lines_data = code.split("\n");
        for (String line : lines_data) {
            Table sub_table = new Table();

            VisCheckBox is_breakpoint_checkbox = new VisCheckBox(line);
            sub_table.add(is_breakpoint_checkbox);

            is_breakpoint_checkbox.addListener(listener);
            main.add(sub_table).left().row();
        }

        return main;
    }

    public void setText(String text) {
        //update all components = remove all components and all them again brand new
        clearChildren();

        Table table = new Table();
        table.left().add(createCodeTable(text));

        VisScrollPane scroll = new VisScrollPane(table);
        scroll.setFlickScroll(false);
        scroll.setFadeScrollBars(false);

        left().add(scroll).growX().row();
        left().add(label_pc_sp);
    }

    public void updatePCSP(Memory memory) {
        label_pc_sp.setText("PC:" + (memory.pc - 512) / 2 + " SP: " + memory.sp);
    }
}
