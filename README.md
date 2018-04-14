# Chip8 Emulator 
Chip8 emulator written in Java, to use it you just need to feed it with input events, program data and display video data somewhere somehow. For writting debugger I've used LibGDX with VisUI library also this gives Android compatibility so you can run this as app on your phone :).
 
## Features
- Debugger
- Breakpoints
- Modyfing register values at program runtime (some breakpoint must be reached to modify)
- Running program with custom frequency (500Hz default value)
- Running program step by step option (one click = parse one opcode)
- On screen virtual keyboard and normal keyboard support
- On screen window with list of current program opcodes with current SP & PC values (usefull with step simulation combined)
- On screen indicator when program is waiting for key press or if some breakpoint is reached

## How to use

```JAVA
//create chip instance
CHIP8 chip = new CHIP8();

//next load program to its memory 
byte[] program_bytes = //load some file as bytes, grab from net whatever
chip.load(program_bytes);

//in main loop
chip.emulationStep();

//keyboard
chip.getKeyboard().justSet(10, 1); //key 10 (A letter on default chip8 keyboard set as pressed (0 for released))

//for rendering loop through all pixels and render them somehow somewhere (in console, on screen using opengl etc.)
for (int i = 0; i < chip.getGFX().getPixelsBuffer().length; i++) {
 for (int j = 0; j < chip.getGFX().getPixelsBuffer()[0].length; j++) {
  boolean pixel = chip.getGFX().getPixelsBuffer()[i][(chip.getGFX().getPixelsBuffer()[0].length - 1) - j];
  
  if (pixel)
   drawSquare(i * tile_size_x, j * tile_size_y, tile_size_x, tile_size_y); //drawSquare(x, y, w, h)
 }
}
```

- More complex example [link](core/src/chip8/ExampleUse.java)

## Screenshots
![Wololo](/../master/screens/full_opts.png?raw=true "Wolololo!")
