/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.splitpdf;

import fr.clementgre.pdf4teachers.components.DirSelector;
import fr.clementgre.pdf4teachers.components.SyncColorPicker;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.interfaces.windows.AlternativeWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class SplitWindow extends AlternativeWindow<VBox> {
    
    private final HBox outputPath = new DirSelector();
    private final TextArea names = new TextArea();
    
    private final SyncColorPicker colorPicker = new SyncColorPicker();
    private final Slider slider = new Slider(0, 100, 50);
    
    private final Button ok = new Button(TR.tr("actions.generate"));
    
    private final SplitEngine engine = new SplitEngine(this, -1);
    
    public SplitWindow(){
        super(new VBox(), StageWidth.LARGE, TR.tr("splitPdfWindow.title"), TR.tr("splitPdfWindow.title"), TR.tr("splitPdfWindow.description"));
    }
    @Override
    public void setupSubClass(){
        
        HBox namesBox = new HBox();
        names.setPromptText(TR.tr("splitPdfWindow.filesNames"));
        names.setMinWidth(1);
        names.setPrefRowCount(10);
        HBox.setHgrow(names, Priority.ALWAYS);
        PaneUtils.setHBoxPosition(names, 0, 0, new Insets(0, 5, 0, 2.5));
    
        Button browseNames = new Button(TR.tr("file.browse"));
        PaneUtils.setHBoxPosition(browseNames, 0, 30, new Insets(0, 2.5, 0, 5));
    
        namesBox.getChildren().addAll(names, browseNames);
    
        browseNames.setOnAction(e -> {
            File selected = FilesChooserManager.showFileDialog(FilesChooserManager.SyncVar.LAST_OPEN_DIR, TR.tr("dialog.file.extensionType.txt"), "*.txt");
            if(selected != null) loadNames(selected);
            updateStatus();
        });
        
        HBox signalPage = new HBox();
        HBox.setMargin(colorPicker, new Insets(0, 10, 0, 0));
        slider.setMaxWidth(100);
        signalPage.getChildren().addAll(new Label(TR.tr("string.color")), colorPicker, new Label(TR.tr("string.sensibility")), slider);
        signalPage.setSpacing(10);
        signalPage.setAlignment(Pos.CENTER_LEFT);
        PaneUtils.setVBoxPosition(signalPage, 0, 30, 2.5, 0);
        
        root.setSpacing(5);
        root.getChildren().addAll(generateInfo(TR.tr("file.destinationFolder"), false), outputPath, generateInfo(TR.tr("splitPdfWindow.filesNames"), true), namesBox, generateInfo(TR.tr("splitPdfWindow.signalPage"), true), signalPage);
        
        
        
        Button cancel = new Button(TR.tr("actions.cancel"));
        cancel.setOnAction(event -> close());
        
        setButtons(cancel, ok);
        updateStatus();
        
        ok.setOnAction((e) -> {
            if(MainWindow.mainScreen.hasDocument(true) && MainWindow.mainScreen.document.save(false) && Edition.isSave()){
                /*try{
                    UndoEngine.lock();
                    engine.process();
                    close();
                }catch(IOException ex){
                    new ErrorAlert(null, ex.getMessage(), false).showAndWait();
                    ex.printStackTrace();
                }finally{
                    Platform.runLater(UndoEngine::unlock);
                }*/
            }
        });
    
        engine.updateDetectedPages(this::updateStatus);
    }
    @Override
    public void afterShown(){
        // Since generate button is disabled, select names field
        names.requestFocus();
    }
    
    private void updateStatus(){
        clearInfoBox();
        ok.setDisable(false);
        
        if(engine.detectedPages() != -1){
            int detectedCount = engine.detectedPages();
            int namesCount = getNamesCount();
            
            if(detectedCount < namesCount){
                updateInfoBox(AlertIconType.WARNING, TR.tr("splitPdfWindow.info.detectedPages", detectedCount, namesCount));
            }else if(detectedCount > namesCount){
                updateInfoBox(AlertIconType.ERROR, TR.tr("splitPdfWindow.info.detectedPages", detectedCount, namesCount));
                ok.setDisable(true);
            }else{ // ==
                updateInfoBox(AlertIconType.INFORMATION, TR.tr("splitPdfWindow.info.detectedPages", detectedCount, namesCount));
            }
        }else{
            setInfoBoxLoader();
            ok.setDisable(true);
        }
    }
    
    private void loadNames(File file){
        if(!file.exists()) return;
    
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            
            while(line != null){
                if(!line.isBlank()) names.appendText((names.getText().endsWith("\n") || names.getText().isEmpty() ? "" : "\n") + line);
                line = reader.readLine();
            }
            reader.close();
            
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public int getNamesCount(){
        return StringUtils.cleanArray(names.getText().split(Pattern.quote("\n"))).length;
    }
}
