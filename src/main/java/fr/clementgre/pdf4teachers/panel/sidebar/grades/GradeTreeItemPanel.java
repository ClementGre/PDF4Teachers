/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

public class GradeTreeItemPanel extends HBox {
    
    Region spacer = new Region();
    private final Text name = new Text();
    private final Text value = new Text();
    private final Text slash = new Text("/");
    private final Text total = new Text();
    
    public Button newGrade = new Button();
    
    public TextArea nameField;
    public TextArea gradeField;
    public TextArea totalField;
    
    private GradeTreeItem treeItem;
    boolean outOfPanel;
    public GradeTreeItemPanel(GradeTreeItem treeItem, boolean outOfPanel){
        this.treeItem = treeItem;
        this.outOfPanel = outOfPanel;
        
        setAlignment(Pos.CENTER);
        setPrefHeight(18);
        
        if(!outOfPanel) setStyle("-fx-padding: -6 -6 -6 -5;"); // top - right - bottom - left
        else setStyle("-fx-padding: -6 -6 6 -5;"); // top - right - bottom - left
        
        // TEXTS
        
        HBox.setMargin(name, new Insets(0, 10, 0, 5));
        HBox.setMargin(value, new Insets(0, 0, 0, 5));
        HBox.setMargin(total, new Insets(0, 5, 0, 0));
        
        if(outOfPanel){
            name.setText(TR.tr("gradeTab.putGradeOutOf"));
            value.textProperty().bind(Bindings.createStringBinding(() -> {
                if(treeItem.getCore().getValue() == -1) return "?";
                if(treeItem.getCore().getTotal() <= 0) return "0";
                if(treeItem.getCore().getOutOfTotal() <= 0) return "?";
                return MainWindow.twoDigFormat.format(treeItem.getCore().getValue() / treeItem.getCore().getTotal() * treeItem.getCore().getOutOfTotal());
            }, treeItem.getCore().valueProperty(), treeItem.getCore().totalProperty(), treeItem.getCore().outOfTotalProperty()));
            
            total.textProperty().bind(Bindings.createStringBinding(() -> {
                if(treeItem.getCore().getOutOfTotal() <= 0) return "?";
                return MainWindow.gradesDigFormat.format(treeItem.getCore().getOutOfTotal());
            }, treeItem.getCore().outOfTotalProperty()));
            
            total.setStyle("-fx-text-fill: #585858;");
            name.setStyle("-fx-text-fill: #9a0000 !important; -fx-font-style: italic;");
            value.setStyle("-fx-text-fill: #585858;");
        }else{
            name.textProperty().bind(treeItem.getCore().nameProperty());
            
            value.textProperty().bind(Bindings.createStringBinding(() -> (treeItem.getCore().getValue() == -1 ? "?" :
                    MainWindow.gradesDigFormat.format(treeItem.getCore().getValue())), treeItem.getCore().valueProperty()));
            
            total.textProperty().bind(Bindings.createStringBinding(() -> MainWindow.gradesDigFormat.format(treeItem.getCore().getTotal()), treeItem.getCore().totalProperty()));
        }
        
        // FIELDS
        
        if(!outOfPanel){
            nameField = new GradeTreeItemField(treeItem, this, GradeTreeItem.FieldType.NAME, true);
            gradeField = new GradeTreeItemField(treeItem, this, GradeTreeItem.FieldType.GRADE, true);
            totalField = new GradeTreeItemField(treeItem, this, GradeTreeItem.FieldType.TOTAL, true);
        }else{
            totalField = new GradeTreeItemField(treeItem, this, GradeTreeItem.FieldType.OUT_OF_TOTAL, false);
        }
        
        // OTHER
        
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        PaneUtils.setPosition(newGrade, 0, 0, 30, 30, true);
        newGrade.setVisible(false);
        if(!outOfPanel){
            newGrade.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/more.png") + "", 0, 0, ImageUtils.defaultFullDarkColorAdjust));
            newGrade.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.gradeTab.isLockGradeScaleProperty().get() || GradeTreeView.getElementTier(treeItem.getCore().getParentPath()) >= 4, MainWindow.gradeTab.isLockGradeScaleProperty()));
            newGrade.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("gradeTab.newGradeButton.tooltip")));
            newGrade.setOnAction(event -> {
                treeItem.setExpanded(true);
                GradeElement element = MainWindow.gradeTab.newGradeElementAuto(treeItem);
                element.select();
                
                // Update total (Fix the bug when a total is predefined (with no children))
                treeItem.makeSum(false);
                AutoTipsManager.showByAction("gradecreate");
            });
        }
        
        getChildren().addAll(name, spacer, value, slash, total, newGrade);
        
        
    }
    
    public void onSelected(){
        
        // CUSTOM OUT OF PANEL
        if(outOfPanel){
            if(MainWindow.gradeTab.isLockGradeScaleProperty().get()){
                getChildren().setAll(name, spacer, value, slash, total, newGrade);
            }else{
                getChildren().setAll(name, spacer, value, slash, totalField, newGrade);
                totalField.setText(treeItem.getCore().getOutOfTotal() == -1 ? "" : MainWindow.gradesDigFormat.format(treeItem.getCore().getOutOfTotal()));
                
                if(treeItem.getCore().getOutOfTotal() == -1) // Select only if the real total panel has no important reason to request focus
                    if(treeItem.hasSubGrade() || treeItem.getCore().getTotal() != 0 && treeItem.getCore().getValue() != -1)
                        Platform.runLater(() -> totalField.requestFocus());
            }
            return;
        }
        
        // FIELDS
        newGrade.setVisible(true);
        
        nameField.setText(treeItem.getCore().getName());
        if(!treeItem.isRoot() && treeItem.getParent() != null){
            if(((GradeTreeItem) treeItem.getParent()).doExistTwice(treeItem.getCore().getName()))
                treeItem.getCore().setName(treeItem.getCore().getName() + "(1)");
        }
        
        gradeField.setText(treeItem.getCore().getValue() == -1 ? "" : MainWindow.gradesDigFormat.format(treeItem.getCore().getValue()));
        totalField.setText(MainWindow.gradesDigFormat.format(treeItem.getCore().getTotal()));
        
        // ADD NODES
        if(MainWindow.gradeTab.isLockGradeScaleProperty().get()){
            if(treeItem.hasSubGrade()){
                getChildren().setAll(name, spacer, value, slash, total, newGrade);
            }else{
                getChildren().setAll(name, spacer, gradeField, slash, total, newGrade);
                Platform.runLater(() -> {
                    if(treeItem.isDeleted()) return;
                    gradeField.requestFocus();
                });
            }
        }else{
            if(treeItem.hasSubGrade()){
                getChildren().setAll(nameField, spacer, value, slash, total, newGrade);
                Platform.runLater(() -> {
                    if(treeItem.isDeleted()) return;
                    if(!treeItem.isRoot())
                        nameField.requestFocus(); // If root, the "out of total" field should be focused instead
                });
            }else{
                getChildren().setAll(nameField, spacer, gradeField, slash, totalField, newGrade);
                Platform.runLater(() -> {
                    if(treeItem.isDeleted()) return;
                    if(name.getText().contains(TR.tr("gradeTab.gradeDefaultName"))) nameField.requestFocus();
                    else if(total.getText().equals("0")) totalField.requestFocus();
                    else gradeField.requestFocus();
                });
            }
        }
    }
    public void onDeselected(){
        newGrade.setVisible(false);
        getChildren().setAll(name, spacer, value, slash, total, newGrade);
    }
    
    public void onMouseOver(){
        newGrade.setVisible(true);
    }
    public void onMouseOut(){
        newGrade.setVisible(false);
    }
    
    
    public void delete(){
        
        name.textProperty().unbind();
        value.textProperty().unbind();
        total.textProperty().unbind();
        newGrade.disableProperty().unbind();
        
        getChildren().clear();
        nameField = null;
        totalField = null;
        gradeField = null;
    }
    
}
