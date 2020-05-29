package fr.themsou.panel.leftBar.files;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.export.ExportWindow;
import fr.themsou.utils.Builders;
import fr.themsou.utils.NodeMenuItem;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.Collections;
import java.util.Optional;

public class FileListView extends ListView<File>{

    public FileListView(){

        VBox.setVgrow(this, Priority.SOMETIMES);
        setOnMouseClicked((MouseEvent event) -> {
            refresh();
        });

        setCellFactory(param -> new FileListItem());
    }

}