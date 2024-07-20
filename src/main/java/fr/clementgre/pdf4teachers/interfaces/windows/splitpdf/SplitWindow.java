/*
 * Copyright (c) 2022-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.splitpdf;

import fr.clementgre.pdf4teachers.components.DirSelector;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.interfaces.windows.AlternativeWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class SplitWindow extends AlternativeWindow<VBox> {
    
    private final DirSelector outputPath = new DirSelector();
    private final TextArea names = new TextArea();
    
    private final Spinner<Integer> intervalSpinner = new Spinner<>(1, 1000, MainWindow.userData.splitPdfInterval);
    
    private final ColorPicker colorPicker = new ColorPicker(MainWindow.userData.splitPdfMatchColor);
    private final Slider slider = new Slider(0, 100, MainWindow.userData.splitSensibility);
    
    private final CheckBox doKeepSelectedPages = new CheckBox(TR.tr("splitWindow.checkbox.keepSelectedPages"));
    private final CheckBox doPreserveEdition = new CheckBox(TR.tr("splitWindow.checkbox.preserveEdition"));
    
    private final Button ok = new Button(TR.tr("actions.generate"));
    
    private final SplitEngine engine = new SplitEngine(this);
    public final SplitType splitType;
    
    public enum SplitType {
        INTERVAL, COLOR, SELECTION
    }
    
    public SplitWindow(SplitType splitType){
        super(new VBox(), StageWidth.LARGE,
                TR.tr("splitPdfWindow." + splitType.toString().toLowerCase() + ".title"),
                TR.tr("splitPdfWindow." + splitType.toString().toLowerCase() + ".title"),
                TR.tr("splitPdfWindow." + splitType.toString().toLowerCase() + ".description"));
        this.splitType = splitType;
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
        
        names.textProperty().addListener((observable, oldValue, newValue) -> updateStatus());
        namesBox.getChildren().addAll(names, browseNames);
        
        browseNames.setOnAction(e -> {
            File selected = FilesChooserManager.showFileDialog(null, TR.tr("dialog.file.extensionType.txt"), "*.txt");
            if(selected != null) loadNames(selected);
        });
        
        
        root.setSpacing(5);
        root.getChildren().addAll(generateInfo(TR.tr("file.destinationFolder"), false), outputPath, generateInfo(TR.tr("splitPdfWindow.filesNames"), true), namesBox);
        if(splitType == SplitType.COLOR){
            
            HBox signalPage = new HBox();
            HBox.setMargin(colorPicker, new Insets(0, 10, 0, 0));
            slider.setMaxWidth(100);
            signalPage.getChildren().addAll(new Label(TR.tr("string.color")), colorPicker, new Label(TR.tr("string.sensitivity")), slider);
            signalPage.setSpacing(10);
            signalPage.setAlignment(Pos.CENTER_LEFT);
            PaneUtils.setVBoxPosition(signalPage, 0, 30, 2.5, 0);
            colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                updateStatus();
                MainWindow.userData.splitPdfMatchColor = newValue;
            });
            slider.valueProperty().addListener((observable, oldValue, newValue) -> {
                updateStatus();
                MainWindow.userData.splitSensibility = newValue.intValue();
            });
            
            Label info = new Label(TR.tr("splitPdfWindow.signalPage.info"));
            PaneUtils.setVBoxPosition(info, 0, 0, new Insets(7.5, 2.5, 2.5, 2.5));
            
            root.getChildren().addAll(generateInfo(TR.tr("splitPdfWindow.signalPage"), true), signalPage, info);
            
        }else if(splitType == SplitType.INTERVAL){
            
            intervalSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                updateStatus();
                MainWindow.userData.splitPdfInterval = newValue;
            });
            
            Label info = new Label(TR.tr("splitPdfWindow.intervalSelection.info"));
            PaneUtils.setVBoxPosition(info, 0, 0, new Insets(7.5, 2.5, 2.5, 2.5));
            
            root.getChildren().addAll(generateInfo(TR.tr("splitPdfWindow.intervalSelection"), true), intervalSpinner, info);
            
        }
        
        // Options
        
        if(splitType != SplitType.INTERVAL){
            doKeepSelectedPages.setSelected(MainWindow.userData.splitPdfKeepSelectedPages);
            doPreserveEdition.setSelected(MainWindow.userData.splitPdfPreserveEdition);
            
            doKeepSelectedPages.selectedProperty().addListener((observable, oldValue, newValue) -> {
                updateStatus();
                MainWindow.userData.splitPdfKeepSelectedPages = newValue;
            });
            doPreserveEdition.selectedProperty().addListener((observable, oldValue, newValue) -> {
                MainWindow.userData.splitPdfPreserveEdition = newValue;
            });
            root.getChildren().addAll(generateInfo(TR.tr("options.title"), true), doKeepSelectedPages, doPreserveEdition);
        }
        
        // Buttons
        
        Button cancel = new Button(TR.tr("actions.cancel"));
        cancel.setOnAction(event -> close());
        
        setButtons(cancel, ok);
        updateStatus();
        
        ok.setOnAction((e) -> {
            if(MainWindow.mainScreen.hasDocument(true) && MainWindow.mainScreen.document.save(false) && Edition.isSave()){
                try{
                    engine.process();
                }catch(IOException ex){
                    Log.eAlerted(ex);
                }
            }
        });
        
        if(splitType == SplitType.COLOR) engine.updatePagesColors(this::updateStatus);
    }
    @Override
    public void afterShown(){
        // Since generate button is disabled, select names field
        names.requestFocus();
    }
    
    private void updateStatus(){
        clearInfoBox();
        ok.setDisable(false);
        
        int detectedCount = engine.countMatchPages();
        
        if(detectedCount != -1){
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
            BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
            String line = reader.readLine();
            
            while(line != null){
                if(!line.isBlank())
                    names.appendText((names.getText().endsWith("\n") || names.getText().isEmpty() ? "" : "\n") + line);
                line = reader.readLine();
            }
            reader.close();
            
        }catch(IOException e){
            Log.eNotified(e);
        }
    }
    
    public boolean doKeepSelectedPages(){
        return doKeepSelectedPages.isSelected();
    }
    public boolean doPreserveEdition(){
        return doPreserveEdition.isSelected();
    }
    public int getInterval(){
        return intervalSpinner.getValue();
    }
    public int getNamesCount(){
        return StringUtils.cleanArray(names.getText().split(Pattern.quote("\n"))).length;
    }
    public String[] getNames(){
        return StringUtils.cleanArray(names.getText().split(Pattern.quote("\n")));
    }
    public Color getColor(){
        return colorPicker.getValue();
    }
    public double getSensibility(){
        // Range .5 - 0
        return .5 - (slider.getValue() / 100d / 2d);
    }
    public ObservableList<Color> getCustomColors(){
        return colorPicker.getCustomColors();
    }
    public File getOutputDir(){
        return outputPath.getFile();
    }
    
}
