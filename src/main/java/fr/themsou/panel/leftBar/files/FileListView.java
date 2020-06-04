package fr.themsou.panel.leftBar.files;

import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;

public class FileListView extends ListView<File>{

    public FileListView(){

        VBox.setVgrow(this, Priority.SOMETIMES);
        setOnMouseClicked((MouseEvent event) -> {
            refresh();
        });

        setCellFactory(param -> new FileListItem());
    }

}