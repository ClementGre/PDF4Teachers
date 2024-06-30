/*
 * Copyright (c) 2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.margin;

import fr.clementgre.pdf4teachers.interfaces.windows.AlternativeWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToDoubleConverter;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class MarginWindow extends AlternativeWindow<VBox> {
    
    private final HBox marginKind = new HBox();
    private final ToggleButton marginKindRelative = new ToggleButton(TR.tr("marginWindow.marginKindRelative"));
    private final ToggleButton marginKindAbsolute = new ToggleButton(TR.tr("marginWindow.marginKindAbsolute"));
    
    private final BorderPane marginPane = new BorderPane();
    private final Spinner<Double> marginTop = new Spinner<>(-1000000d, 1000000d, MainWindow.userData.marginTop, 5);
    private final Spinner<Double> marginRight = new Spinner<>(-1000000d, 1000000d, MainWindow.userData.marginRight, 5);
    private final Spinner<Double> marginBottom = new Spinner<>(-1000000d, 1000000d, MainWindow.userData.marginBottom, 5);
    private final Spinner<Double> marginLeft = new Spinner<>(-1000000d, 1000000d, MainWindow.userData.marginLeft, 5);
    
    private final CheckBox isMarginOnSelectedPages = new CheckBox(TR.tr("marginWindow.isMarginOnSelectedPages"));
    
    VBox marginInfo = new VBox();
    
    public MarginWindow(){
        super(new VBox(), StageWidth.LARGE, TR.tr("marginWindow.title"), TR.tr("marginWindow.title"), TR.tr("marginWindow.description"));
    }
    @Override
    public void setupSubClass(){
        
        // Root Pane
        root.setSpacing(5);
        root.getChildren().addAll(marginKind, marginInfo, marginPane, generateInfo(TR.tr("options.title"), true), isMarginOnSelectedPages);
        
        // Kind
        
        marginKind.getChildren().addAll(marginKindRelative, marginKindAbsolute);
        ToggleGroup marginKindGroup = new ToggleGroup();
        marginKindGroup.getToggles().addAll(marginKindRelative, marginKindAbsolute);
        
        marginKindGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(MainWindow.userData.marginKindAbsolute){
                PDRectangle cropBox = getFirstSelectedPageCropBox();
                if(MainWindow.mainScreen.document.getSelectedPages().isEmpty())
                    marginInfo = generateInfo(TR.tr("marginWindow.marginAbsolute", Integer.toString((int) cropBox.getWidth()), Integer.toString((int) cropBox.getHeight())), false);
                else
                    marginInfo = generateInfo(TR.tr("marginWindow.marginAbsoluteSelected", Integer.toString((int) cropBox.getWidth()), Integer.toString((int) cropBox.getHeight())), false);
            }else{
                marginInfo = generateInfo(TR.tr("marginWindow.marginRelative"), false);
            }
            root.getChildren().set(1, marginInfo);
        });
        marginKindGroup.selectToggle(MainWindow.userData.marginKindAbsolute ? marginKindAbsolute : marginKindRelative);
        marginKindGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null){
                // Select a toggle that is not the one disabled
                if(marginKindGroup.getToggles().get(0) != oldValue)
                    marginKindGroup.getToggles().get(0).setSelected(true);
                else marginKindGroup.getToggles().get(1).setSelected(true);
            }
            MainWindow.userData.marginKindAbsolute = newValue == marginKindAbsolute;
            convertSpinnerValue(MainWindow.userData.marginKindAbsolute, marginTop);
            convertSpinnerValue(MainWindow.userData.marginKindAbsolute, marginRight);
            convertSpinnerValue(MainWindow.userData.marginKindAbsolute, marginBottom);
            convertSpinnerValue(MainWindow.userData.marginKindAbsolute, marginLeft);
        });
        
        // Margin
        marginPane.setPadding(new Insets(2.5));
        marginPane.setMaxWidth(400);
        
        marginPane.setRight(generateMarginInput(TR.tr("marginWindow.margin.right"), marginRight));
        marginPane.setLeft(generateMarginInput(TR.tr("marginWindow.margin.left"), marginLeft));
        marginPane.setTop(generateMarginInput(TR.tr("marginWindow.margin.top"), marginTop));
        marginPane.setBottom(generateMarginInput(TR.tr("marginWindow.margin.bottom"), marginBottom));
        
        marginTop.valueProperty().addListener(o -> MainWindow.userData.marginTop = marginTop.getValue());
        marginRight.valueProperty().addListener(o -> MainWindow.userData.marginRight = marginRight.getValue());
        marginBottom.valueProperty().addListener(o -> MainWindow.userData.marginBottom = marginBottom.getValue());
        marginLeft.valueProperty().addListener(o -> MainWindow.userData.marginLeft = marginLeft.getValue());
        
        // Options
        if(MainWindow.mainScreen.document.getSelectedPages().isEmpty()){
            isMarginOnSelectedPages.setDisable(true);
        }else{
            isMarginOnSelectedPages.setSelected(MainWindow.userData.marginIsMarginOnSelectedPages);
            isMarginOnSelectedPages.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.marginIsMarginOnSelectedPages = newValue);
        }
        PaneUtils.setVBoxPosition(isMarginOnSelectedPages, 0, 0, 2.5, 0);
        
        // Buttons
        Button apply = new Button(TR.tr("actions.apply"));
        apply.setOnAction(this::apply);
        Button cancel = new Button(TR.tr("actions.cancel"));
        cancel.setOnAction(event -> close());
        setButtons(cancel, apply);
    }
    @Override
    public void afterShown(){
        updateInfoBox(AlertIconType.INFORMATION, TR.tr("marginWindow.info"));
    }
    
    private Pane generateMarginInput(String title, Spinner<Double> spinner){
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        Label label = new Label(title);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-font-size: 12px;");
        
        // Spinner config
        spinner.setMaxWidth(100);
        spinner.setEditable(true);
        spinner.getValueFactory().setConverter(new StringToDoubleConverter(spinner.getValue(), true));
        
        box.getChildren().addAll(label, spinner);
        return box;
    }
    
    private void apply(ActionEvent e){
        if(MainWindow.mainScreen.hasDocument(true)){
            new MarginEngine(marginTop.getValue(), marginRight.getValue(), marginBottom.getValue(), marginLeft.getValue(),
                    isMarginOnSelectedPages.isSelected(), marginKindAbsolute.isSelected()).apply();
        }
        close();
    }
    
    private void convertSpinnerValue(boolean toAbsolute, Spinner<Double> spinner){
        if(toAbsolute){
            spinner.getValueFactory().setValue(spinner.getValue() * getFirstSelectedPageWidth() / 100);
        }else{
            spinner.getValueFactory().setValue(spinner.getValue() / getFirstSelectedPageWidth() * 100);
        }
    }
    
    
    private double getFirstSelectedPageWidth(){
        return getFirstSelectedPageCropBox().getWidth();
    }
    private PDRectangle getFirstSelectedPageCropBox(){
        if(MainWindow.mainScreen.hasDocument(true)){
            int firstIndex = 0;
            if(!MainWindow.mainScreen.document.getSelectedPages().isEmpty()){
                firstIndex = MainWindow.mainScreen.document.getSelectedPages().stream().sorted().findFirst().orElse(0);
            }
            return MainWindow.mainScreen.document.pdfPagesRender.getPageRotatedCropBox(firstIndex);
        }
        return new PDRectangle();
    }
    
}
