package com.mateus.chip8emulator;

import com.mateus.chip8emulator.components.Display;
import com.mateus.chip8emulator.components.Emulator;
import com.mateus.chip8emulator.components.KeyInput;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Chip8Emulator extends Application {

    private Stage emulatorStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.emulatorStage = primaryStage;
        initiation();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void initiation() {

        KeyInput keyInput = new KeyInput();
        Display display = new Display();

        Emulator emulator = new Emulator(display, keyInput);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        MenuItem loadRom = new MenuItem("Load ROM");
        loadRom.setOnAction(l -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open ROM File");

            File rom = fileChooser.showOpenDialog(emulatorStage);
            if (rom != null)
                emulator.loadRom(rom.getPath());
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(l -> emulator.finish());

        fileMenu.getItems().add(loadRom);
        fileMenu.getItems().add(exitItem);
        menuBar.getMenus().add(fileMenu);

        VBox root = new VBox();
        root.getChildren().add(menuBar);
        root.getChildren().add(display);

        Scene scene = new Scene(root);

        scene.setOnKeyPressed(e -> keyInput.setKeyValue(e.getCode(), true));
        scene.setOnKeyReleased(e -> keyInput.setKeyValue(e.getCode(), false));

        emulatorStage.setScene(scene);

        emulatorStage.setTitle("Chip-8 Emulator");
        emulatorStage.setResizable(false);
        emulatorStage.setMaxWidth(640);
        emulatorStage.setMaxHeight(370);
        emulatorStage.setMinWidth(640);
        emulatorStage.setMinHeight(370);
        emulatorStage.setOnCloseRequest(e -> emulator.finish());
        emulatorStage.show();
    }
}
