/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.components.ScratchText;
import fr.clementgre.pdf4teachers.components.ShortcutsTextArea;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.fonts.AppFontsLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

import java.util.Random;
import java.util.regex.Pattern;

public class GradeTreeItemField extends ShortcutsTextArea {
    
    private static final int GRADE_NAME_MAX_CHARS = 40;
    
    public GradeTreeItemField(GradeTreeItem treeItem, GradeTreeItemPanel panel, GradeTreeItem.FieldType type, boolean contextMenu){
        super("😉😉😉");
        setId("no-scroll-bar");
        
        setStyle("-fx-font-size: 13;");
        setMinHeight(29);
        setMaxHeight(29);
        setMinWidth(29);
        
        if(type == GradeTreeItem.FieldType.GRADE) HBox.setMargin(this, new Insets(0, 0, 0, 5));
        if(type == GradeTreeItem.FieldType.TOTAL || type == GradeTreeItem.FieldType.OUT_OF_TOTAL)
            HBox.setMargin(this, new Insets(0, 5, 0, 0));
        
        if(contextMenu) setContextMenu(treeItem.getCore().menu);
        else setContextMenu(null);
        
        
        // Select & deselect
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if(newValue){
                    if(getCaretPosition() == getText().length() || getCaretPosition() == 0 || type != GradeTreeItem.FieldType.NAME){
                        positionCaret(getText().length());
                        selectAll();
                    }
                }else deselect();
            });
        });
        
        // Text listener
        ScratchText meter = new ScratchText();
        meter.setFont(AppFontsLoader.getFontPath(AppFontsLoader.OPEN_SANS, 13));
        
        textProperty().addListener((observable, oldValue, newValue) -> {
            
            // ENTER PRESSED
            
            if(newValue.contains("\n")){ // Enter : Switch to the next grade
                if(treeItem.getPageContextmenu() != null) treeItem.getPageContextmenu().hide();
                
                GradeTreeItem afterItem = treeItem.getAfterChildItem();
                MainWindow.gradeTab.treeView.getSelectionModel().select(afterItem);
                if(afterItem != null) Platform.runLater(() -> {
                    switch(type){
                        case NAME -> afterItem.getPanel().nameField.requestFocus();
                        case GRADE -> afterItem.getPanel().gradeField.requestFocus();
                        case TOTAL, OUT_OF_TOTAL -> afterItem.getPanel().totalField.requestFocus();
                    }
                });
                setText(oldValue);
                return;
            }
            
            // TAB PRESSED
            
            if(newValue.contains("\t")){
                if(type == GradeTreeItem.FieldType.OUT_OF_TOTAL){
                    GradeTreeItem afterItem = treeItem.getAfterChildItem();
                    MainWindow.gradeTab.treeView.getSelectionModel().select(afterItem);
                    if(afterItem != null)
                        Platform.runLater(() -> afterItem.getPanel().totalField.requestFocus());
                }
                
                if(treeItem.getCore().getTotal() == 0){
                    switch(type){
                        case NAME, GRADE -> panel.totalField.requestFocus();
                        case TOTAL -> panel.gradeField.requestFocus();
                    }
                }else{
                    switch(type){
                        case NAME, TOTAL -> panel.gradeField.requestFocus();
                        case GRADE -> panel.totalField.requestFocus();
                    }
                }
                setText(oldValue);
                return;
            }
            
            // FORMAT NEW VALUE
            
            String newText;
            if(type == GradeTreeItem.FieldType.NAME){
                newText = newValue.replaceAll("[^ -\\[\\]-~À-ÿ]", "");
                if(newText.length() >= GRADE_NAME_MAX_CHARS) newText = newText.substring(0, GRADE_NAME_MAX_CHARS);
            }else{
                newText = newValue.replaceAll("[^0123456789.,]", "");
                String[] splitted = newText.split("[.,]");
                String integers = splitted.length >= 1 ? splitted[0] : "0";
                String decimals = splitted.length >= 2 ? splitted[1] : "";
                
                if(integers.length() > 4){
                    if(splitted.length >= 2)
                        newText = integers.substring(0, 4) + MainWindow.gradesDigFormat.getDecimalFormatSymbols().getDecimalSeparator() + decimals;
                    else newText = integers.substring(0, 4);
                }
                if(decimals.length() > 3){
                    newText = integers + MainWindow.gradesDigFormat.getDecimalFormatSymbols().getDecimalSeparator() + splitted[1].substring(0, 3);
                }
            }
            
            // SET NEW VALUE
            
            setText(newText);
            meter.setText(newText);
            setMaxWidth(Math.max(meter.getLayoutBounds().getWidth() + 20, 29));
            // Name field will never push others nodes
            if(type != GradeTreeItem.FieldType.NAME) setMinWidth(Math.max(meter.getLayoutBounds().getWidth() + 20, 29));
            
            // In case this is not the real grade field (in the right click context menu field)
            if(type == GradeTreeItem.FieldType.GRADE && this != panel.gradeField){
                panel.gradeField.setText(newText);
            }
            
            // EDIT CORE VALUES
            
            switch(type){
                case NAME -> {
                    treeItem.getCore().setName(newText);
                    if(new Random().nextInt(10) == 0) AutoTipsManager.showByAction("graderename");
                }
                case GRADE -> {
                    // dont accept a value higher than the total
                    try{
                        double value = Double.parseDouble(newText.replaceAll(Pattern.quote(","), "."));
                        if(value > treeItem.getCore().getTotal() && !treeItem.hasSubGrade()){
                            setText(MainWindow.gradesDigFormat.format(treeItem.getCore().getTotal()));
                            panel.gradeField.setText(MainWindow.gradesDigFormat.format(treeItem.getCore().getTotal()));
                        }else treeItem.getCore().setValue(value);
                    }catch(NumberFormatException e){
                        treeItem.getCore().setValue(-1);
                    }
                }
                case TOTAL -> {
                    try{
                        treeItem.getCore().setTotal(Double.parseDouble(newText.replaceAll(Pattern.quote(","), ".")));
                    }catch(NumberFormatException e){
                        treeItem.getCore().setTotal(0);
                    }
                }
                case OUT_OF_TOTAL -> {
                    try{
                        treeItem.getCore().setOutOfTotal(Double.parseDouble(newText.replaceAll(Pattern.quote(","), ".")));
                    }catch(NumberFormatException e){
                        treeItem.getCore().setOutOfTotal(-1);
                    }
                }
            }
            
        });
    }
    
}
