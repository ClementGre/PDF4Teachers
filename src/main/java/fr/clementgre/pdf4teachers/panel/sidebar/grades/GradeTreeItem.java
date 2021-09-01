/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.components.menus.NodeMenu;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class GradeTreeItem extends TreeItem<String> {
    
    public enum FieldType {
        NAME,
        GRADE,
        TOTAL,
        OUT_OF_TOTAL
    }
    
    private GradeElement core;
    private ContextMenu pageContextMenu = null;
    private boolean deleted = false;
    
    // UI
    private TreeCell<String> cell;
    private VBox root = new VBox();
    private GradeTreeItemPanel outOfPanel;
    private GradeTreeItemPanel panel;
    
    // EVENTS
    private EventHandler<MouseEvent> mouseEnteredEvent;
    private ChangeListener<Boolean> selectedListener;
    
    public GradeTreeItem(GradeElement core){
        this.core = core;
        
        if(isRoot()){
            outOfPanel = new GradeTreeItemPanel(this, true);
            root.getChildren().add(outOfPanel);
        }
        panel = new GradeTreeItemPanel(this, false);
        root.getChildren().add(panel);
        
        selectedListener = (observable, oldValue, newValue) -> {
            if(isDeleted()) return;
            
            if(newValue){ // Est selectionné
                panel.onSelected();
                if(outOfPanel != null) outOfPanel.onSelected();
                
            }else if(oldValue){ // n'est plus selectionné
                panel.onDeselected();
                if(outOfPanel != null) outOfPanel.onDeselected();
            }
        };
        
        mouseEnteredEvent = event -> {
            if(!cell.isFocused()) panel.onMouseOver();
            if(MainWindow.gradeTab.isLockGradeScaleProperty().get()){
                if(cell.getTooltip() == null)
                    cell.setTooltip(PaneUtils.genToolTip(TR.tr("gradeTab.lockGradeScale.unableToEditTooltip")));
            }else if(cell.getTooltip() != null){
                cell.setTooltip(null);
            }
        };
        
    }
    
    public void updateCell(TreeCell<String> cell){
        if(cell == null) return;
        if(core == null){
            System.err.println("Error: trying to update a GradeTreeItem which should be deleted (core == null).");
            return;
        }
        
        // Remove listener on old cell
        if(this.cell != null){
            this.cell.selectedProperty().removeListener(selectedListener);
            this.cell.setOnMouseExited(null);
            this.cell.setOnMouseEntered(null);
            this.cell.setContextMenu(null);
        }
        
        this.cell = cell;
        cell.setGraphic(root);
        cell.setStyle(null);
        cell.setStyle("-fx-padding: 6 6 6 2;");
        cell.setContextMenu(core.menu);
        cell.setOnMouseEntered(mouseEnteredEvent);
        cell.setOnMouseExited(e -> {
            if(!cell.isFocused()) panel.onMouseOut();
        });
        
        cell.selectedProperty().addListener(selectedListener);
        
        if(MainWindow.gradeTab.isLockGradeScaleProperty().get()){
            if(cell.getTooltip() == null)
                cell.setTooltip(PaneUtils.genToolTip(TR.tr("gradeTab.lockGradeScale.unableToEditTooltip")));
        }else if(cell.getTooltip() != null){
            cell.setTooltip(null);
        }
        
        // DEBUG
        if(Main.DEBUG)
            cell.setTooltip(PaneUtils.genToolTip(core.getParentPath() + " - n°" + (core.getIndex() + 1) + "\nPage n°" + core.getPageNumber()));
        
    }
    
    /////////////////////////////////////
    /////////////// MENUS ///////////////
    /////////////////////////////////////
    
    public MenuItem getEditMenuItem(ContextMenu menu){
        
        HBox pane = new HBox();
        NodeMenuItem menuItem = new NodeMenuItem(pane, false);
        
        Text name = new Text();
        
        Text value = new Text();
        Text slash = new Text("/");
        Text total = new Text();
        
        pane.setAlignment(Pos.CENTER);
        pane.setPrefHeight(18);
        name.textProperty().bind(core.nameProperty());
        HBox.setMargin(name, new Insets(0, 10, 0, 0));
        
        HBox.setMargin(value, new Insets(0, 0, 0, 5));
        value.textProperty().bind(Bindings.createStringBinding(() -> (core.getValue() == -1 ? "?" : MainWindow.gradesDigFormat.format(core.getValue())), core.valueProperty()));
        
        HBox.setMargin(total, new Insets(0, 5, 0, 0));
        total.textProperty().bind(Bindings.createStringBinding(() -> MainWindow.gradesDigFormat.format(core.getTotal()), core.totalProperty()));
        
        // SETUP
        
        TextArea gradeField = new GradeTreeItemField(this, panel, FieldType.GRADE, false);
        HBox.setMargin(gradeField, new Insets(-7, 0, -7, 0));
        
        gradeField.setText(core.getValue() == -1 ? "" : MainWindow.gradesDigFormat.format(core.getValue()));
        if(!isRoot() && getParent() != null){
            if(((GradeTreeItem) getParent()).doExistTwice(core.getName())) core.setName(core.getName() + "(1)");
        }
        
        if(hasSubGrade()){
            pane.getChildren().setAll(name, new HBoxSpacer(), value, slash, total);
        }else{
            pane.getChildren().setAll(name, new HBoxSpacer(), gradeField, slash, total);
            Platform.runLater(gradeField::requestFocus);
        }
        
        pageContextMenu = menu;
        
        pane.setMaxWidth(Double.MAX_VALUE);
        pane.setOnMouseEntered(e -> {
            gradeField.requestFocus();
            MainWindow.gradeTab.treeView.getSelectionModel().select(this);
        });
        
        if(!hasSubGrade()){
            pane.setOnMouseClicked((e) -> {
                if(e.getButton() == MouseButton.PRIMARY){
                    panel.gradeField.setText(MainWindow.gradesDigFormat.format(core.getTotal()));
                    setChildrenValuesToMax();
                }else{
                    ContextMenu contextMenu = new ContextMenu();
                    contextMenu.getItems().addAll(getChooseValueMenuItemsAuto());
                    NodeMenuItem.setupMenu(contextMenu);
                    
                    contextMenu.show(Main.window, e.getScreenX(), e.getScreenY());
                }
            });
        }
        
        menu.setOnHiding((e) -> {
            name.textProperty().unbind();
            value.textProperty().unbind();
            total.textProperty().unbind();
            pane.setOnMouseEntered(null);
            pane.setOnMouseClicked(null);
        });
        
        return menuItem;
    }
    public ArrayList<MenuItem> getChooseValueMenuItemsAuto(){
        return getChooseValueMenuItems(0, getCore().getTotal(), 0, true);
    }
    public ArrayList<MenuItem> getChooseValueMenuItems(double min, double max, int deep, boolean includeEdges){
        ArrayList<MenuItem> items = new ArrayList<>();
        
        int maxDivisions = 11;
        double[] intervalsToTest = {.25, .5, 1, 5, 10, 25, 50};
        double finalInterval = 100;
        
        for(double interval : intervalsToTest){
            int divisions = (int) ((max - min) / interval);
            if(divisions < maxDivisions){
                finalInterval = interval;
                break;
            }
        }
        
        double actualValue = min;
        while(actualValue < max){
            items.add(getChooseValueMenuItem(actualValue, actualValue, Math.min(max, actualValue + finalInterval), deep + 1));
            actualValue += finalInterval;
        }
        if(includeEdges) items.add(getChooseValueMenuItem(max, max, max, deep + 1));
        
        return items;
    }
    private MenuItem getChooseValueMenuItem(double value, double min, double max, int deep){
        
        if(max - min > .25 && deep < 3){
            NodeMenu menuItem = new NodeMenu(new HBox());
            menuItem.setName(MainWindow.gradesDigFormat.format(value));
            menuItem.setOnAction(e -> {
                panel.gradeField.setText(MainWindow.gradesDigFormat.format(value));
                menuItem.hideAll();
            });
            menuItem.setOnShown(e -> {
                NodeMenuItem.setupMenuNow(menuItem);
            });
            menuItem.getItems().addAll(getChooseValueMenuItems(min, max, deep, false));
            return menuItem;
        }else{
            NodeMenuItem menuItem = new NodeMenuItem(new HBox(), false);
            menuItem.setName(MainWindow.gradesDigFormat.format(value));
            menuItem.setOnAction(e -> {
                panel.gradeField.setText(MainWindow.gradesDigFormat.format(value));
            });
            return menuItem;
        }
    }
    
    //////////////////////////////////////
    //////////// "NAVIGATION" ////////////
    //////////////////////////////////////
    
    public GradeTreeItem getBeforeItem(){
        if(isRoot()) return null;
        
        GradeTreeItem parent = (GradeTreeItem) getParent();
        
        if(core.getIndex() == 0) return parent;
        
        // Descend le plus possible dans les enfants du parent pour retrouver le dernier
        GradeTreeItem newParent = (GradeTreeItem) parent.getChildren().get(core.getIndex() - 1);
        while(newParent.hasSubGrade()){
            newParent = (GradeTreeItem) newParent.getChildren().get(newParent.getChildren().size() - 1);
        }
        return newParent;
    }
    public GradeTreeItem getAfterItem(){
        
        if(hasSubGrade()) return (GradeTreeItem) getChildren().get(0);
        if(isRoot()) return null;
        
        GradeTreeItem parent = (GradeTreeItem) getParent();
        GradeTreeItem children = this;
        
        // Remonte dans les parents jusqu'a trouver un parent qui as un élément après celui-ci
        while(children.getCore().getIndex() == parent.getChildren().size() - 1){
            children = parent;
            if(parent.isRoot()) return null;
            parent = (GradeTreeItem) parent.getParent();
        }
        return (GradeTreeItem) parent.getChildren().get(children.getCore().getIndex() + 1);
    }
    public GradeTreeItem getBeforeChildItem(){
        GradeTreeItem beforeItem = getBeforeItem();
        while(beforeItem != null){
            GradeTreeItem beforeAfterItem = beforeItem.getBeforeItem();
            if(!beforeItem.hasSubGrade()) return beforeItem;
            if(beforeAfterItem == null) return null;
            beforeItem = beforeAfterItem;
        }
        return null;
    }
    public GradeTreeItem getAfterChildItem(){
        GradeTreeItem afterItem = getAfterItem();
        while(afterItem != null){
            GradeTreeItem afterAfterItem = afterItem.getAfterItem();
            if(!afterItem.hasSubGrade()) return afterItem;
            if(afterAfterItem == null) return null;
            afterItem = afterAfterItem;
        }
        return null;
    }
    
    ////////////////////////////////////////
    /////////////// CHILDREN ///////////////
    ////////////////////////////////////////
    
    public void makeSum(boolean updateLocation){
        if(!updateLocation) makeSum(-1, 0);
        else throw new RuntimeException("use makeSum(int previousPage, int previousRealY) to update Location");
    }
    public void makeSum(int previousPage, int previousRealY){
        if(!deleted){
            boolean hasValue = false;
            double value = 0;
            double total = 0;
            
            for(int i = 0; i < getChildren().size(); i++){
                GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
                
                // Don't count the "Bonus" children in the Total
                if(!children.getCore().isBonus()){
                    total += children.getCore().getTotal(); // count total
                }
                
                if(children.getCore().getValue() >= 0){
                    hasValue = true;
                    value += children.getCore().getValue();
                }
            }
            
            if(hasValue){
                if(!core.isFilled() && previousPage != -1){
                    if(previousPage != core.getPageNumber()) core.switchPage(previousPage);
                    core.nextRealYToUse = previousRealY - core.getRealHeight();
                }
                core.setValue(value);
            }else core.setValue(-1);
            
            core.setTotal(total);
        }
        
        if(getParent() != null){
            ((GradeTreeItem) getParent()).makeSum(core.getPageNumber(), core.getRealY());
        }
    }
    
    public void resetChildrenValues(){
        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            children.getCore().setValue(-1);
            children.resetChildrenValues();
        }
    }
    public void setChildrenValuesTo0(){
        if(hasSubGrade()){
            for(int i = 0; i < getChildren().size(); i++){
                GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
                children.setChildrenValuesTo0();
            }
        }else{
            getCore().setValue(0);
        }
    }
    public void setChildrenValuesToMax(){
        if(hasSubGrade()){
            for(int i = 0; i < getChildren().size(); i++){
                GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
                children.setChildrenValuesToMax();
            }
        }else{
            getCore().setValue(getCore().getTotal());
        }
    }
    public void setChildrenAlwaysVisible(boolean visible, boolean registerUndo){
        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            children.getCore().setAlwaysVisible(visible, registerUndo);
        }
    }
    public void reIndexChildren(){
        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            children.getCore().setIndex(i);
        }
    }
    public void resetParentPathChildren(){
        String path = GradeTreeView.getElementPath(this);
        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            children.getCore().setParentPath(path);
            if(children.hasSubGrade()) children.resetParentPathChildren();
        }
        
    }
    
    public boolean doExistTwice(String name){
        if(isRoot()) return false;
        int k = 0;
        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            if(children.getCore().getName().equals(name)) k++;
        }
        return k >= 2;
    }
    
    /////////////// DELETE ///////////////
    
    public void delete(boolean removePageElement, boolean markAsUnsave, UType undoType){
        deleted = true;
        
        panel.delete();
        panel = null;
        
        if(hasSubGrade()) deleteChildren(markAsUnsave, undoType);
        if(removePageElement){
            getCore().delete(markAsUnsave, undoType);
        }
        
        // Unbinds to prevent leaks
        core = null;
        if(cell != null){
            cell.setContextMenu(null);
            cell.setOnMouseEntered(null);
            cell.setOnMouseExited(null);
            cell.selectedProperty().removeListener(selectedListener);
        }
        selectedListener = null;
        mouseEnteredEvent = null;
    }
    // This method delete add children of this TreeItem, including pageElements
    public void deleteChildren(boolean markAsUnsave, UType undoType){
        undoType = (undoType == UType.NO_UNDO) ? UType.NO_UNDO : UType.NO_COUNT;
        while(hasSubGrade()){
            // Since we start the delete from here, the children have to has removePageElement = true.
            ((GradeTreeItem) getChildren().get(0)).delete(true, markAsUnsave, undoType);
        }
    }
    public boolean isDeleted(){
        return deleted;
    }
    
    
    /////////////// GETTERS ///////////////
    
    public boolean hasSubGrade(){
        return getChildren().size() != 0;
    }
    public boolean isRoot(){
        return StringUtils.cleanArray(core.getParentPath().split(Pattern.quote("\\"))).length == 0;
    }
    
    public ContextMenu getPageContextmenu(){
        return pageContextMenu;
    }
    public GradeElement getCore(){
        return core;
    }
    public TreeCell<String> getCell(){
        return cell;
    }
    public GradeTreeItemPanel getPanel(){
        return panel;
    }
}
