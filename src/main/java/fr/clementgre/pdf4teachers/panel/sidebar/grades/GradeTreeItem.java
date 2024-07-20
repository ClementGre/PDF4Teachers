/*
 * Copyright (c) 2020-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.components.menus.NodeMenu;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
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
    private ContextMenu pageContextMenu;
    private boolean deleted;
    
    // UI
    private TreeCell<String> cell;
    private final VBox root = new VBox();
    private GradeTreeItemPanel outOfPanel;
    private GradeTreeItemPanel panel;
    
    // EVENTS
    private ChangeListener<Boolean> selectedListener;
    private EventHandler<MouseEvent> mouseEnteredEvent;
    private EventHandler<MouseEvent> mouseExitedEvent;
    private boolean isMouseOver;
    
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
            
            if(newValue && !oldValue){ // Devient selectionné
                panel.onSelected();
                if(outOfPanel != null) outOfPanel.onSelected();
                
            }else if(oldValue && !newValue){ // N'est plus selectionné
                panel.onDeselected(isMouseOver);
                if(outOfPanel != null) outOfPanel.onDeselected(isMouseOver);
            }
        };
        
        mouseEnteredEvent = event -> {
            isMouseOver = true;
            panel.onMouseOver();
            
            if(MainWindow.gradeTab.isLockGradeScaleProperty().get()){
                if(cell.getTooltip() == null)
                    cell.setTooltip(PaneUtils.genToolTip(TR.tr("gradeTab.lockGradeScale.unableToEditTooltip")));
            }else if(cell.getTooltip() != null){
                cell.setTooltip(null);
            }
        };
        mouseExitedEvent = event -> {
            isMouseOver = false;
            // Hide add button only if cell is not selected.
            if(!cell.isSelected()) panel.onMouseOut();
        };
        
    }
    
    public void updateCell(TreeCell<String> cell){
        if(cell == null) return;
        if(core == null){
            Log.e("Trying to update a GradeTreeItem which should be deleted (core == null).");
            return;
        }
        
        // Switching from this.cell to cell.
        if(this.cell != null){
            this.cell.selectedProperty().removeListener(selectedListener);
            this.cell.setOnMouseExited(null);
            this.cell.setOnMouseEntered(null);
            this.cell.setContextMenu(null);
        }
        
        this.cell = cell;
        cell.setGraphic(root);
        
        Platform.runLater(() -> {
            double diff = MainWindow.gradeTab.treeView.sceneToLocal(cell.localToScene(root.getLayoutX(), 0)).getX();
            root.setMaxWidth(MainWindow.gradeTab.treeView.getWidth() - diff - 6 - MainWindow.gradeTab.treeView.getVScrollbarVisibleWidth());
        });
        
        cell.setStyle(null);
        cell.setStyle("-fx-padding: 6 6 6 2;");
    
        cell.selectedProperty().addListener(selectedListener);
        cell.setOnMouseExited(mouseExitedEvent);
        cell.setOnMouseEntered(mouseEnteredEvent);
        cell.setContextMenu(core.menu);
    
        // Remove the :pressed pseudo class to prevent the bug where the cell stays pressed because the TextField has appeared and consumed the release event.
        cell.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), false);
        
        if(MainWindow.gradeTab.isLockGradeScaleProperty().get()){
            if(cell.getTooltip() == null)
                cell.setTooltip(PaneUtils.genToolTip(TR.tr("gradeTab.lockGradeScale.unableToEditTooltip")));
        }else if(cell.getTooltip() != null){
            cell.setTooltip(null);
        }
        
        // DEBUG
        if(Log.doDebug())
            cell.setTooltip(PaneUtils.genToolTip(core.getParentPath() + " - n°" + (core.getIndex() + 1) + "\nPage n°" + core.getPageNumber()));
        
    }
    
    /////////////////////////////////////
    /////////////// MENUS ///////////////
    /////////////////////////////////////
    
    public MenuItem getEditMenuItem(ContextMenu menu, PageRenderer page){
        
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
    
        menu.setOnHiding(event -> {
            name.textProperty().unbind();
            value.textProperty().unbind();
            total.textProperty().unbind();
            pane.setOnMouseEntered(null);
            pane.setOnMouseClicked(null);
        });
        
        if(!hasSubGrade()){
            pane.setOnMouseClicked((e) -> {
                if(e.getButton() == MouseButton.PRIMARY){
                    panel.gradeField.setText(MainWindow.gradesDigFormat.format(core.getTotal()));
                    setChildrenValuesToMax();
                }else{
                    e.consume();
    
                    ContextMenu chooseValueMenu = new ContextMenu();
                    chooseValueMenu.getItems().setAll(getChooseValueMenuItemsAuto());
                    NodeMenuItem.setupMenu(chooseValueMenu);
                    chooseValueMenu.show(Main.window, e.getScreenX(), e.getScreenY());
                    
                    chooseValueMenu.setOnHiding(event -> menu.hide());
                    menu.setOnHiding(event -> {
                        chooseValueMenu.hide();
                        name.textProperty().unbind();
                        value.textProperty().unbind();
                        total.textProperty().unbind();
                        pane.setOnMouseEntered(null);
                        pane.setOnMouseClicked(null);
                    });
                    
                }
            });
        }
        
        return menuItem;
    }
    public ArrayList<MenuItem> getChooseValueMenuItemsAuto(){
        return getChooseValueMenuItems(0, getCore().getTotal(), 0, true);
    }
    public ArrayList<MenuItem> getChooseValueMenuItems(double min, double max, int depth, boolean includeEdges){
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
            items.add(getChooseValueMenuItem(actualValue, actualValue, Math.min(max, actualValue + finalInterval), depth + 1));
            actualValue += finalInterval;
        }
        if(includeEdges) items.add(getChooseValueMenuItem(max, max, max, depth + 1));
        
        return items;
    }
    private MenuItem getChooseValueMenuItem(double value, double min, double max, int depth){
        
        if(max - min > .25 && depth < 3){
            NodeMenu menuItem = new NodeMenu(new HBox());
            menuItem.setName(MainWindow.gradesDigFormat.format(value));
            menuItem.setOnAction(e -> {
                panel.gradeField.setText(MainWindow.gradesDigFormat.format(value));
                menuItem.hideAll();
            });
            menuItem.setOnShown(e -> {
                NodeMenuItem.setupMenuNow(menuItem);
            });
            menuItem.getItems().addAll(getChooseValueMenuItems(min, max, depth, false));
            return menuItem;
        }
        NodeMenuItem menuItem = new NodeMenuItem(new HBox(), false);
        menuItem.setName(MainWindow.gradesDigFormat.format(value));
        menuItem.setOnAction(e -> {
            panel.gradeField.setText(MainWindow.gradesDigFormat.format(value));
        });
        return menuItem;
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
            newParent = (GradeTreeItem) newParent.getChildren().getLast();
        }
        return newParent;
    }
    public GradeTreeItem getAfterItem(){
        
        if(hasSubGrade()) return (GradeTreeItem) getChildren().getFirst();
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
        if(!deleted && !getChildren().isEmpty()){
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
                if(!core.isFilled() && previousPage != -1){ // Add core element to the page if needed
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
    public boolean doContainsChildrenUnfilledAndAlwaysVisible(){
        if(getCore().isAlwaysVisible() && getCore().getValue() == -1) return true;
        
        return getChildren().stream()
                .map(stringTreeItem -> (GradeTreeItem) stringTreeItem)
                .anyMatch(GradeTreeItem::doContainsChildrenUnfilledAndAlwaysVisible);
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
        int k = (int) getChildren()
                .stream()
                .map(stringTreeItem -> (GradeTreeItem) stringTreeItem)
                .filter(children -> children.getCore().getName().equals(name))
                .count();
        return k >= 2;
    }
    
    /////////////// DELETE ///////////////
    
    public void delete(boolean removePageElement, boolean markAsUnsave, UType undoType){
        deleted = true;
        
        panel.delete();
        panel = null;
        
        // The childrens are deleted before the parent, then, the children UType is ELEMENT_NO_COUNT_AFTER
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
        mouseExitedEvent = null;
    }
    // This method delete add children of this TreeItem, including pageElements
    public void deleteChildren(boolean markAsUnsave, UType undoType){
        undoType = (undoType == UType.NO_UNDO) ? UType.NO_UNDO : UType.ELEMENT_NO_COUNT_AFTER;
        while(hasSubGrade()){
            // Since we start the delete from here, the children have to has removePageElement = true.
            ((GradeTreeItem) getChildren().getFirst()).delete(true, markAsUnsave, undoType);
        }
    }
    public boolean isDeleted(){
        return deleted;
    }
    
    
    /////////////// GETTERS ///////////////
    
    public boolean hasSubGrade(){
        return !getChildren().isEmpty();
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
    public GradeTreeItemPanel getOutOfPanel(){
        return outOfPanel;
    }
    public boolean hasOutOfPanel(){
        return outOfPanel != null;
    }
}
