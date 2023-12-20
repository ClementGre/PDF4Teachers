/*
 * Copyright (c) 2020-2023. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.language;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.AlternativeWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
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
    
    private final boolean firstStartBehaviour;
    
    private final CallBackArg<String> callBack;
    public LanguageWindow(boolean firstStartBehaviour, CallBackArg<String> callBack){
        super(new ListView<>(), StageWidth.NORMAL, TR.tr("language.chooseLanguageWindow.title"), TR.tr("language.chooseLanguageWindow.title"));
        this.firstStartBehaviour = firstStartBehaviour;
        this.callBack = callBack;
        
        if(Main.settings.language.getValue().isEmpty()) Main.settings.language.setValue("en_us");
    }
    public static void showLanguageWindow(boolean firstStartBehaviour){
    
        new LanguagesUpdater().update((downloaded) -> {
            new LanguageWindow(firstStartBehaviour, selectedLanguage -> {
                if(!selectedLanguage.isEmpty() && !selectedLanguage.equals(Main.settings.language.getValue())){
                    String oldDocPath = TR.getDocFile().getAbsolutePath();
        
                    Main.settings.language.setValue(selectedLanguage);
                    Main.settings.saveSettings();
        
                    if(!firstStartBehaviour){
                        Main.window.restart(true, oldDocPath);
                        return;
                    }
                }
                if(firstStartBehaviour){
                    TR.updateLocale();
                    Main.startMainWindowAuto();
                }
            }).requestFocus();
        }, false, false);
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
        
        setOnCloseRequest((e) -> {
            // User requested close without clicking on a scene button: save selected language only if first start.
            if(firstStartBehaviour) callBack.call(root.getSelectionModel().getSelectedItem().getShortName());
        });
        
        setupLanguages();
        setupButtons();
    }
    private void setupButtons(){
        Button contribute = new Button(TR.tr("language.chooseLanguageWindow.contributeButton"));
        Button apply = new Button(Main.window == null ? TR.tr("actions.apply") : TR.tr("actions.applyAndRestart"));
        
        contribute.setOnAction((ActionEvent event) -> Main.hostServices.showDocument("https://pdf4teachers.org/Contribute/"));
        apply.setOnAction((ActionEvent event) -> {
            TR.updateLocale();
            close();
            callBack.call(root.getSelectionModel().getSelectedItem().getShortName());
        });
        
        if(!firstStartBehaviour){
            Button cancel = new Button(TR.tr("actions.cancel"));
            cancel.setOnAction((e) -> close());
            setButtons(cancel, apply);
        }else setButtons(apply);
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
                }catch(Exception e){Log.eNotified(e);}
            }
        }catch(Exception e){Log.eNotified(e);}
    }
    
}
