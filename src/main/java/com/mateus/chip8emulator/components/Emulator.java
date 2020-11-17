package com.mateus.chip8emulator.components;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Stack;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Emulator extends Thread {

    private final int[] memory = new int[4096];
    private final Display display;
    private int opcode;
    private final KeyInput keyInput;
    private final Random random = new Random();
    private final int[] V = new int[16];
    private int pc = 0x200;
    private int I = 0;
    private final Stack<Integer> subroutines = new Stack<>();
    private int delayTimer = 0;
    private int soundTimer = 0;
    private boolean shouldRedrawn = false;
    private boolean running = true;
    private final int CARRY_FLAG = 0xF;

    public Emulator(Display display, KeyInput keyInput) {
        this.display = display;
        this.keyInput = keyInput;
        int[] fontSet = new int[] {
                0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                0x20, 0x60, 0x20, 0x20, 0x70, // 1
                0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        };

        System.arraycopy(fontSet, 0, memory, 0, fontSet.length);
    }

    @Override
    public void run() {
        int cycles = 0;
        running = true;
        while (running) {
            long runtime1 = System.nanoTime();
            fetch();
            decode();
            if (cycles %8==0) {
                cycles = 0;
                if (shouldRedrawn) {
                    display.render();
                    shouldRedrawn = false;
                }
                delayTimer--;
                soundTimer--;
            }
            long runtime2 = System.nanoTime();
            cycles++;
            long wait = (2000000 - (runtime2 - runtime1));
            long target = System.nanoTime() + wait;
            while (System.nanoTime()<target) {
                try {
                    Thread.sleep(0L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void finish() {
        running = false;
        System.exit(0);
    }

    public void loadRom(String filePath) {
        try {
            interrupt();
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            for (int i = 0; i < bytes.length; i++) {
                memory[0x200 + i] = bytes[i] & 0xFF;
            }
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetch() {
        opcode = (memory[pc] << 8 | memory[pc + 1]);
        pc += 2;
    }

    private void decode() {
        switch (opcode) {
            case 0x00E0:
                display.clear();
                shouldRedrawn = true;

                break;
            case 0x00EE:
                pc = subroutines.pop();

                break;
        }
        switch (opcode & 0xF000) {
            case 0x1000:
                pc = opcode & 0x0FFF;
                break;
            case 0x2000:
                subroutines.push(pc);
                pc = opcode & 0x0FFF;
                break;
            case 0x3000:
                if (V[(opcode & 0x0F00) >>> 8] ==  (opcode & 0x00FF)) {
                    pc += 2;
                }
                break;
            case 0x4000:
                if (V[(opcode & 0x0F00) >>> 8] !=  (opcode & 0x00FF)) {
                    pc += 2;
                }
                break;
            case 0x5000:
                if (V[(opcode & 0x0F00) >>> 8] == V[(opcode & 0x00F0) >>> 4])
                    pc += 2;
                break;
            case 0x6000:
                V[(opcode & 0x0F00) >>> 8] = opcode & 0x00FF;
                break;
            case 0x7000:
                int sum = V[(opcode & 0x0F00) >>> 8] + (opcode & 0x00FF);
                if (sum >= 256) {
                    sum -= 256;
                }
                V[(opcode & 0x0F00) >>> 8] = sum;
                break;
            case 0xA000:
                I = opcode & 0x0FFF;
                break;
            case 0xB000:
                pc = (opcode & 0x0FFF) + V[0];
                break;
            case 0xC000:
                int rand = random.nextInt(256);
                V[(opcode & 0x0F00) >>> 8] = rand & (opcode & 0x00FF);
                break;
            case 0xD000:
                int x = V[(opcode & 0x0F00) >> 8] % 64;
                int y = V[(opcode & 0x00F0) >> 4] % 32;
                int height = opcode & 0x000F;
                V[CARRY_FLAG] = 0;

                int read = 0;
                while (read < height) {
                    int pixelLine = memory[I + read];
                    for (int xLine = 0; xLine < 8; xLine++) {
                        int pixel = pixelLine & (0x80 >> xLine);
                        if (pixel != 0) {
                            if (display.getPixelValue(x + xLine, y + read) == 1)
                                V[CARRY_FLAG] = 1;
                            display.setPixel(x + xLine, y + read);
                        }
                    }
                    read++;
                }
                shouldRedrawn = true;
                break;
        }
        switch (opcode & 0xF00F) {
            case 0x8000:
                V[(opcode & 0x0F00) >>> 8] = V[(opcode & 0x00F0) >>> 4];
                break;
            case 0x8001:
                V[(opcode & 0x0F00) >>> 8] |= V[(opcode & 0x00F0) >>> 4];
                break;
            case 0x8002:
                V[(opcode & 0x0F00) >>> 8] &= V[(opcode & 0x00F0) >>> 4];
                break;
            case 0x8003:
                V[(opcode & 0x0F00) >>> 8]^= V[(opcode & 0x00F0) >>> 4];
                break;
            case 0x8004:
                int sum = V[(opcode & 0x0F00) >>> 8] + V[(opcode & 0x00F0) >>> 4];
                if (sum > 255) {
                    V[CARRY_FLAG] = 1;
                } else {
                    V[CARRY_FLAG] = 0;
                }
                V[(opcode & 0x0F00) >>> 8] = sum & 0xFF;
                break;
            case 0x8005:
                int n1 = (opcode & 0x0F00) >>> 8;
                int n2 = (opcode & 0x00F0) >>> 4;


                V[CARRY_FLAG] = V[n1] > V[n2] ? 1 : 0;


                V[n1] = (V[n1] - V[n2]) & 0xFF;
                break;
            case 0x8006:
                V[CARRY_FLAG] = (V[(opcode & 0x0F00) >>> 8] & 0x1) == 1 ? 1 : 0;

                V[(opcode & 0x0F00) >>> 8] = (V[(opcode & 0x0F00) >>> 8] >>> 1);

                break;
            case 0x8007:
                n1 = (opcode & 0x00F0) >>> 4;
                n2 = (opcode & 0x0F00) >>> 8;
                V[CARRY_FLAG] = V[n1] > V[n2] ? 1 : 0;

                V[n1] = (V[n1] - V[n2]) & 0xFF;
                break;
            case 0x800E:
                int msb = (V[(opcode & 0x0F00) >>> 8] >>> 7);
                V[CARRY_FLAG] = msb == 0x1 ? 1 : 0;
                V[(opcode & 0x0F00) >>> 8] = (V[(opcode & 0x0F00) >>> 8] << 1) & 0xFF;
                break;
            case 0x9000:
                if (V[(opcode & 0x0F00) >>> 8] != V[(opcode & 0x00F0) >>> 4])
                    pc += 2;
                break;
        }
        switch (opcode & 0xF0FF) {
            case 0xE09E:
                if (keyInput.getKey(V[(opcode & 0x0F00) >>> 8]))
                    pc += 2;
                break;
            case 0xE0A1:
                if (!keyInput.getKey(V[(opcode & 0x0F00) >>> 8]))
                    pc += 2;
                break;
            case 0xF007:
                V[(opcode & 0x0F00) >>> 8] = delayTimer;
                break;
            case 0xF015:
                delayTimer = V[(opcode & 0x0F00) >>> 8];
                break;
            case 0xF018:
                soundTimer = V[(opcode & 0x0F00) >>> 8];
                break;
            case 0xF01E:
                if (I + V[(opcode & 0x0F00) >>> 8] > 0xFFF) {
                    V[CARRY_FLAG] = 1;
                } else {
                    V[CARRY_FLAG] = 0;
                }

                I = ((I + V[(opcode & 0x0F00) >>> 8]) & 0xFFF);
                break;
            case 0xF00A:
                boolean pressed = false;
                for (int i = 0; i < keyInput.getKeys().length; i++) {
                    if (keyInput.getKey(i)) {
                        V[(opcode & 0x0F00) >>> 8] = i;
                        pressed = true;
                        break;
                    }
                }
                if (!pressed)
                    pc -= 2;
                break;
            case 0xF029:
                I = (V[(opcode & 0x0F00) >>> 8] * 5);
                shouldRedrawn = true;
                break;
            case 0xF033:
                int idx = (opcode & 0x0F00) >>> 8;
                memory[I] = (V[idx] / 100);
                memory[I + 1] = ((V[idx] % 100) / 10);
                memory[I + 2] = ((V[idx] % 100) % 10);
                break;
            case 0xF055:
                int x = (opcode & 0x0F00) >>> 8;
                System.arraycopy(V, 0, memory, I, x + 1);
                break;
            case 0xF065:
                x = (opcode & 0x0F00) >>> 8;
                for (int i = 0; i <= x; i++) {
                    V[i] = memory[I + i] & 0xFF;
                }
                break;
        }

    }
}
