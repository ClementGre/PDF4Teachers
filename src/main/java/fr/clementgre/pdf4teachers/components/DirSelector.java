/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.io.File;

public class DirSelector extends HBox {
    
    private final TextField path;
    
    public DirSelector(){
        this(null, null);
    }
    public DirSelector(String defaultPath){
        this(defaultPath, null);
    }
    
    public DirSelector(String defaultPath, CallBackArg<String> changeDefaultPath){
        if(defaultPath == null || defaultPath.isBlank() || !new File(defaultPath).exists()) defaultPath = getDefaultPath();
        
        path = new TextField(defaultPath);
        path.setPromptText(TR.tr("file.destinationFolder"));
        path.setMinWidth(1);
        path.setMinHeight(30);
        HBox.setHgrow(path, Priority.ALWAYS);
        PaneUtils.setHBoxPosition(path, 0, 30, new Insets(0, 5, 0, 2.5));

        Button changePath = new Button(TR.tr("file.browse"));
        PaneUtils.setHBoxPosition(changePath, 0, 30, new Insets(0, 2.5, 0, 5));
        
        getChildren().addAll(path, changePath);
    
        String finalDefaultPath = defaultPath;
        changePath.setOnAction(event -> {
            File file = FilesChooserManager.showDirectoryDialog(changePath.getText(), null, finalDefaultPath);
            if(file != null){
                path.setText(file.getAbsolutePath() + File.separator);
                if(changeDefaultPath != null) changeDefaultPath.call(file.getAbsolutePath() + File.separator);
            }
        });
        
    }
    
    private String getDefaultPath(){
        return MainWindow.filesTab.getCurrentDir() != null ? MainWindow.filesTab.getCurrentDir().getAbsolutePath() : System.getProperty("user.home");
    }
    
    public TextField getTextField(){
        return path;
    }
    public String getPath(){
        return path.getText();
    }
    public File getFile(){
        return new File(path.getText());
    }
}
