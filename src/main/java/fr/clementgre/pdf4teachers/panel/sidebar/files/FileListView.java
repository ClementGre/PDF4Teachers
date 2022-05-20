/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.files;

import javafx.geometry.Insets;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;

public class FileListView extends ListView<File>{
    
    public FileListView(){
        
        setBorder(null);
        setPadding(new Insets(0));
        
        VBox.setVgrow(this, Priority.SOMETIMES);
        setOnMouseClicked((MouseEvent event) -> {
            refresh();
        });
        
        setCellFactory(param -> new FileListCell());
    }
    
}