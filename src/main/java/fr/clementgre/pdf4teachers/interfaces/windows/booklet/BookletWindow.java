/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.booklet;

import fr.clementgre.pdf4teachers.interfaces.windows.AlternativeWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BookletWindow extends AlternativeWindow<VBox> {
    
    private final ToggleButton convertKindMake = new ToggleButton(TR.tr("bookletWindow.convertKindMake"));
    private final ToggleButton convertKindDisassemble = new ToggleButton(TR.tr("bookletWindow.convertKindDisassemble"));
    
    private final CheckBox doNotReorderPages = new CheckBox(TR.tr("bookletWindow.doNotReorderPages"));
    
    public BookletWindow(){
        super(new VBox(), StageWidth.LARGE, TR.tr("bookletWindow.title"), TR.tr("bookletWindow.title"), TR.tr("bookletWindow.description"));
    }
    @Override
    public void setupSubClass(){
    
        HBox convertKind = new HBox();
        ToggleGroup convertKindGroup = new ToggleGroup();
        convertKindGroup.getToggles().addAll(convertKindMake, convertKindDisassemble);
        
        convertKindGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null){
                // Select a toggle that is not the one disabled
                if(convertKindGroup.getToggles().get(0) != oldValue) convertKindGroup.getToggles().get(0).setSelected(true);
                else convertKindGroup.getToggles().get(1).setSelected(true);
            }
        });
        convertKindGroup.selectToggle(MainWindow.userData.bookletDoMakeBooklet ? convertKindMake : convertKindDisassemble);
        convertKindGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.bookletDoMakeBooklet = newValue == convertKindMake);
        
        convertKind.getChildren().addAll(convertKindMake, convertKindDisassemble);
        
        doNotReorderPages.setSelected(false);
        
        root.getChildren().addAll(convertKind, doNotReorderPages);
    }
    @Override
    public void afterShown(){
        Button convert = new Button(TR.tr("actions.convert"));
        Button cancel = new Button(TR.tr("actions.cancel"));
        cancel.setOnAction(event -> close());
    
        setButtons(cancel, convert);
    
        convert.setOnAction((e) -> {
            if(MainWindow.mainScreen.hasDocument(true)){
                new BookletEngine(convertKindMake.isSelected(), !doNotReorderPages.isSelected(), true).convert(MainWindow.mainScreen.document);
            }
        });
    }
}
