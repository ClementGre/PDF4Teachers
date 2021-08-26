/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.language;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.AlternativeWindow;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.Objects;

public class LanguageWindow extends AlternativeWindow<ListView<LanguagePane>> {
    
    public static void checkUpdatesAndShow(CallBackArg<String> callBack){
        new LanguagesUpdater().update((downloaded) -> {
            new LanguageWindow(callBack);
        }, false, false);
    }
    
    CallBackArg<String> callBack;
    public LanguageWindow(CallBackArg<String> callBack){
        super(new ListView<>(), StageWidth.NORMAL, TR.tr("language.chooseLanguageWindow.title"), TR.tr("language.chooseLanguageWindow.title"));
        this.callBack = callBack;
        
        if(Main.settings.language.getValue().isEmpty()) Main.settings.language.setValue("en_us");
    }
    
    @Override
    public void setupSubClass(){
        setSubHeaderText(TR.tr("language.chooseLanguageWindow.header"));
        
        VBox.setVgrow(root, Priority.ALWAYS);
        root.setPrefHeight(500);
        root.setBorder(null);
        
        if(Main.settings.darkTheme.getValue())
            root.setStyle("-fx-background-color: #262626;"); // Default background color defined in css
        
        // Force the root pane to fit all the height
        // (Therefore, there is no scroll with the Alternative Window scrollPane.)
        scrollPane.setFitToHeight(true);
        
        setupLanguages();
        
        setupButtons();
    }
    private void setupButtons(){
        Button contribute = new Button(TR.tr("language.chooseLanguageWindow.contributeButton"));
        Button cancel = new Button(TR.tr("actions.cancel"));
        Button apply = new Button(Main.window == null ? TR.tr("actions.apply") : TR.tr("actions.applyAndRestart"));
        
        contribute.setOnAction((ActionEvent event) -> Main.hostServices.showDocument("https://pdf4teachers.org/Contribute/"));
        cancel.setOnAction((e) -> close());
        apply.setOnAction((ActionEvent event) -> {
            TR.updateLocale();
            close();
            callBack.call(root.getSelectionModel().getSelectedItem().getShortName());
        });
        
        setButtons(cancel, apply);
        setLeftButtons(contribute);
    }
    @Override
    public void afterShown(){
    
    }
    
    public void setupLanguages(){
        try{
            File dir = new File(Main.dataFolder + "translations" + File.separator);
            
            for(File file : Objects.requireNonNull(dir.listFiles())){
                try{
                    if(FilesUtils.getExtension(file.getName()).equals("properties")){
                        LanguagePane languagePane = new LanguagePane(file);
                        root.getItems().add(languagePane);
                        
                        if(Main.settings.language.getValue().equals(languagePane.getShortName()))
                            root.getSelectionModel().select(languagePane);
                    }
                }catch(Exception e){e.printStackTrace();}
            }
        }catch(Exception e){e.printStackTrace();}
    }
    
}
