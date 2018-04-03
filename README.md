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
