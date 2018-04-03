package libgdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

import chip8.CHIP8;
import chip8.GFX;
import libgdx.ui.ProgramExecutionSpeedWindow;
import libgdx.ui.ProgramInstructionsWindow;
import libgdx.ui.RegistersWindow;
import libgdx.ui.ScreenKeyboardWindow;
import libgdx.ui.StepSimulationWindow;
import libgdx.ui.ToolsWindow;

public class EmulatorMain extends ApplicationAdapter {
	//libgdx rendering vars
	private SpriteBatch batch;
	private Texture white_image;
	private OrthographicCamera camera;

	//chip vars
	private boolean emulator_running = true, step_emulation;
	private Thread chip8_thread;

	private CHIP8 chip;

	//input
	private InputMultiplexer multiplexer;

	//ui
	private BitmapFont font;
	private Stage ui;

	private ToolsWindow tools_ui;
	private ProgramInstructionsWindow program_instructions_ui;
	private ProgramExecutionSpeedWindow frequency_ui;
	private ScreenKeyboardWindow keyboard_ui;
	private StepSimulationWindow step_simulation_ui;
	private RegistersWindow registers_ui;

	//colors
	private static final Color BACKGROUND_COLOR = Color.RED;
	private static final Color TILE_COLOR = Color.LIGHT_GRAY;

	@Override
	public void create () {
		VisUI.load();

		font = new BitmapFont();
		batch = new SpriteBatch();
		white_image = new Texture("white_texture.png");

		multiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(multiplexer);

		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		//create emulator instance
		chip = new CHIP8();

		//libgdx keyboard input
		LibGdxKeyboardInput keyboard_input = new LibGdxKeyboardInput(this, multiplexer);

		//init gui
		ui = new Stage(new ScreenViewport());
		multiplexer.addProcessor(ui);

		showFilechooser();

		program_instructions_ui = new ProgramInstructionsWindow(this);
		program_instructions_ui.centerWindow();
		ui.addActor(program_instructions_ui);

		frequency_ui = new ProgramExecutionSpeedWindow();
		frequency_ui.centerWindow();
		ui.addActor(frequency_ui);

		keyboard_ui = new ScreenKeyboardWindow(this);
		keyboard_ui.centerWindow();
		ui.addActor(keyboard_ui);

		registers_ui = new RegistersWindow();
		registers_ui.centerWindow();
		ui.addActor(registers_ui);

		step_simulation_ui = new StepSimulationWindow(this);
		step_simulation_ui.centerWindow();
		ui.addActor(step_simulation_ui);

		tools_ui = new ToolsWindow(this);
		tools_ui.createToggleButtonForWindow(program_instructions_ui).createToggleButtonForWindow(frequency_ui).createToggleButtonForWindow(keyboard_ui).createToggleButtonForWindow(registers_ui);
		tools_ui.createToggleButtonForWindow(step_simulation_ui);
		ui.addActor(tools_ui);

		//start emulator thread
		createChipThread();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);

		camera.setToOrtho(false, width, height);
		ui.getViewport().update(width, height);
	}

	public void loadAnotherProgram() {
		chip.shutoff();
		chip = new CHIP8();
		showFilechooser();
	}

	private void showFilechooser() {
		FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
		fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

		FileTypeFilter filter = new FileTypeFilter(false);
		filter.addRule("CHIP8 program", "ch8");
		fileChooser.setFileTypeFilter(filter);

		fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected (Array<FileHandle> file) {
				chip.load(file.first().readBytes());

				//show program instructions list
				program_instructions_ui.setText(chip.getProgramInstructions());
			}
		});

		fileChooser.fadeIn();
		ui.addActor(fileChooser);

		fileChooser.setDirectory(Gdx.files.internal("roms/"));
	}

	private void createChipThread() {
		chip8_thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(emulator_running) {
					float UPS = frequency_ui.getFrequency();
					float sleep_time = 1f / UPS * 1000f;

					if(chip != null && !step_emulation)
						chip.emulationStep();

					try {
						Thread.sleep((long) sleep_time);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		chip8_thread.start();
	}

	private void updateUI() {
		registers_ui.update(chip);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(BACKGROUND_COLOR.r, BACKGROUND_COLOR.g, BACKGROUND_COLOR.b, BACKGROUND_COLOR.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//update ui
		updateUI();

		//chip logic
		if(chip != null) {
			//update current pc & sp in ui component
			program_instructions_ui.updatePCSP(chip.getMemory());

			//render process
			batch.setProjectionMatrix(camera.combined);
			batch.setColor(TILE_COLOR);
			batch.begin();

			//calculate tiles size to fit whole screen space
			float tile_size_x = (float) Gdx.graphics.getWidth() / (float) GFX.WIDTH;
			float tile_size_y = (float) Gdx.graphics.getHeight() / (float) GFX.HEIGHT;

			//offsets
			int offx = 0;//-Gdx.graphics.getWidth() / 2;
			int offy = 0;//-Gdx.graphics.getHeight() / 2;

			//render loop
			for (int i = 0; i < chip.getGFX().getPixelsBuffer().length; i++) {
				for (int j = 0; j < chip.getGFX().getPixelsBuffer()[0].length; j++) {
					boolean val = chip.getGFX().getPixelsBuffer()[i][(chip.getGFX().getPixelsBuffer()[0].length - 1) - j];

					if (val)
						batch.draw(white_image, i * tile_size_x + offx, j * tile_size_y + offy, tile_size_x, tile_size_y);
				}
			}

			//return to basic color
			batch.setColor(Color.WHITE);

			//render text if chip is waiting for some input
			if (chip.getKeyboard().isWaitingForKey()) {
				font.draw(batch, "waiting for key...", 1150, 700);
			}

			//render text if some breakpoint is reached
			if(chip.isBreakpoint()) {
				font.draw(batch, "breakpoint! (" + chip.getBreakpointLine() + ")", 1150, 680);
			}

			batch.end();
			//call update just before rendering process
			chip.getGFX().update();

		}

		ui.act(Gdx.graphics.getDeltaTime());
		ui.draw();
	}
	
	@Override
	public void dispose () {
		emulator_running = false;

		batch.dispose();
		white_image.dispose();
	}

	public CHIP8 getChip() {
		return chip;
	}

	public void setStepSimulationEnabled(boolean step_emulation) {
		this.step_emulation = step_emulation;
	}
}
