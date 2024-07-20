/*
 * Copyright (c) 2020-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradeTreeView extends TreeView<String> {
    
    private ScrollBar verticalScrollbar;
    
    public GradeTreeView(GradeTab gradeTab){
        getStyleClass().add("grade-tree-view");
        
        disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        prefHeightProperty().bind(gradeTab.pane.heightProperty().subtract(layoutYProperty()));
        prefWidthProperty().bind(gradeTab.pane.widthProperty());
        
        setCellFactory(new Callback<>() {
            @Override
            public TreeCell<String> call(TreeView<String> param){
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty){
                        super.updateItem(item, empty);
                        
                        // Empty cell or String Data
                        if(empty || item != null){
                            setGraphic(null);
                            setStyle(null);
                            setContextMenu(null);
                            setOnMouseClicked(null);
                            setTooltip(null);
                            return;
                        }
                        // TreeGradeData
                        if(getTreeItem() instanceof GradeTreeItem gradeTreeItem){
                            gradeTreeItem.updateCell(this);
                            return;
                        }
                        // Other
                        setStyle(null);
                        setGraphic(null);
                        setContextMenu(null);
                        setOnMouseClicked(null);
                        setTooltip(null);
                    }
                };
            }
        });
        
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Update cells max-width (system to prevent horizontal scroll)
        widthProperty().addListener((observable, oldValue, newValue) -> {
            refresh();
        });
    
        Platform.runLater(() -> {
            verticalScrollbar = getVerticalScrollbar();
            if(verticalScrollbar != null){
                // Setup listener for updating view when bar visibility changes (So system to prevent horizontal scroll can update its max-width)
                verticalScrollbar.visibleProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    refresh();
                });
            }
        });
    }
    
    // Clear elements in tree and in page
    public void clearElements(boolean regenerateRoot, boolean markAsUnsave){
        if(getRoot() != null){
            getRootTreeItem().delete(true, markAsUnsave, markAsUnsave ? UType.ELEMENT : UType.NO_UNDO);
        }
        if(regenerateRoot) generateRoot(false);
        else setRoot(null);
    }
    
    private void generateRoot(boolean update){
        MainWindow.gradeTab.newGradeElement(TR.tr("gradeTab.gradeDefaultName.total"), -1, 0, 0, "", update);
    }
    
    public void addElement(GradeElement element){
        
        if(element.getParentPath().isEmpty()){ // ROOT
            // If is root, we need to delete the old root.
            if(getRoot() != null && getRootTreeItem().getCore() != null)
                getRootTreeItem().delete(true, false, UType.NO_UNDO);
            
            GradeTreeItem item = element.toGradeTreeItem();
            item.setExpanded(true);
            setRoot(item);
            getSelectionModel().select(getRoot());
            
        }else{ // CHILD
            GradeTreeItem treeElement = element.toGradeTreeItem();
            addToList(getGradeTreeItemParent(element), treeElement);
            treeElement.setExpanded(true);
        }
    }
    
    // When the deletion start from a GradeTreeItem : GradeTreeItem --> GradeElement --> THIS (We end the loop here with the isDeleted())
    // When the deletion start from a GradeElement : GradeElement --> THIS --> GradeTreeItem (We end the loop here with the removePageElement arg)
    // GradeElement must always be before this in the stack. That's why this method is only called by GradeElement
    public void removeElement(GradeElement element, boolean markAsUnsave){
        
        if(element.getParentPath().isEmpty()){ // ROOT
            
            // Delete only if it wasn't already deleted (See comment above).
            if(!getRootTreeItem().isDeleted()) getRootTreeItem().delete(false, markAsUnsave, UType.ELEMENT);
            // Remove the item from its parent
            setRoot(null);
            
        }else{ // CHILD
            GradeTreeItem treeElement = getGradeTreeItem((GradeTreeItem) getRoot(), element);
            if(treeElement == null) return;
            // Delete only if it wasn't already deleted (See comment above).
            if(!treeElement.isDeleted()) treeElement.delete(false, markAsUnsave, UType.ELEMENT);
            
            // Remove the item from its parent
            GradeTreeItem parent = (GradeTreeItem) treeElement.getParent();
            parent.getChildren().remove(treeElement);
            
            if(markAsUnsave){
                // These actions are unSaving the edition
                parent.reIndexChildren();
                parent.makeSum(false);
            }
        }
    }
    
    public GradeTreeItem getGradeTreeItemParent(GradeElement element){
        
        // ELEMENT IS SUB-ROOT
        String[] path = StringUtils.cleanArray(element.getParentPath().split(Pattern.quote("\\")));
        if(path[0].equals(getRootTreeItem().getCore().getName()) && path.length == 1){
            return (GradeTreeItem) getRoot();
        }
        
        // OTHER
        path = StringUtils.cleanArray(element.getParentPath()
                .replaceFirst(Pattern.quote(getRootTreeItem().getCore().getName()), "")
                .split(Pattern.quote("\\")));
        
        GradeTreeItem parent = (GradeTreeItem) getRoot();
        for(String parentName : path){
            
            // Cherche l'enfant qui correspond au nom du chemin
            for(int i = 0; i < parent.getChildren().size(); i++){
                GradeTreeItem children = (GradeTreeItem) parent.getChildren().get(i);
                if(children.getCore().getName().equals(parentName)){
                    parent = children;
                    break;
                }
            }
        }
        if(parent.equals(getRoot())){
            Log.w("L'element Note \"" + element.getName() + "\" a ete place dans Root car aucun parent ne lui a été retrouve.");
            Log.w("ParentPath = " + element.getParentPath());
        }
        return parent;
    }
    
    public GradeTreeItem getGradeTreeItem(GradeTreeItem parent, GradeElement element){
        
        for(int i = 0; i < parent.getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) parent.getChildren().get(i);
            if(element.equals(children.getCore())){
                return children;
            }
            if(children.hasSubGrade()){
                // Si l'élément a des enfants, on refait le test sur ses enfants
                GradeTreeItem testChildren = getGradeTreeItem(children, element);
                if(testChildren != null) return testChildren;
            }
        }
        return null;
        
    }
    
    private void addToList(GradeTreeItem parent, GradeTreeItem element){
        
        int index = element.getCore().getIndex();
        int before = (int) parent.getChildren()
                .stream()
                .map(stringTreeItem -> (GradeTreeItem) stringTreeItem)
                .filter(children -> children.getCore().getIndex() < index)
                .count();

        parent.getChildren().add(before, element);
    }
    
    public static GradeElement getNextGrade(int page, int y){
        
        ArrayList<GradeElement> items = getGradesArrayByCoordinates();
        
        GradeElement before = null;
        GradeElement after = items.size() >= 2 ? items.get(1) : null;
        
        int i = 0;
        for(GradeElement grade : items){
            int minPage = 0;
            int minY = 0;
            if(before != null){
                minPage = before.getPageNumber();
                minY = (int) before.getLayoutY();
            }
            int maxPage = 999999;
            int maxY = 999999;
            if(after != null){
                maxPage = after.getPageNumber();
                maxY = (int) after.getLayoutY();
            }
            if((page == maxPage && y < maxY || page < maxPage) && (page == minPage && y > minY || page > minPage)){
                return grade;
            }
            i++;
            before = grade;
            after = items.size() >= i + 2 ? items.get(i + 1) : null;
        }
        return null;
    }
    
    public static GradeTreeItem getNextLogicGrade(){
        return getGradesArray(GradeTreeView.getTotal())
                .stream()
                .filter(grade -> !grade.hasSubGrade() && grade.getCore().getValue() == -1)
                .findFirst()
                .orElse(null);
    }
    
    public static GradeTreeItem getNextLogicGradeNonNull(){
        ArrayList<GradeTreeItem> items = getGradesArray(GradeTreeView.getTotal());
        for(GradeTreeItem grade : items){
            if(!grade.hasSubGrade() && grade.getCore().getValue() == -1) return grade;
        }
        for(GradeTreeItem grade : items){
            if(!grade.hasSubGrade()) return grade;
        }
        return items.getFirst();
    }
    
    public static void defineNaNLocations(){
        ArrayList<GradeTreeItem> items = getGradesArray(GradeTreeView.getTotal());
        ArrayList<GradeTreeItem> itemsToSend = new ArrayList<>();
        
        boolean afterItemHaveToDropDown = false;
        int i = 0;
        for(GradeTreeItem item : items){
            
            if(!item.getCore().isFilled() && item.isRoot()){ // ramène le root tout en haut si il n'a pas de valeur
                if(item.getCore().getPageNumber() != 0) item.getCore().switchPage(0);
                item.getCore().setRealY(0);
            }
            
            // Drop down grades if item is visible
            if(item.getCore().isFilled()){
                // Ramène tous les éléments au niveau de celui-ci
                for(GradeTreeItem itemToSend : itemsToSend){
                    if(itemToSend.getCore().getPageNumber() != item.getCore().getPageNumber())
                        itemToSend.getCore().switchPage(item.getCore().getPageNumber());
                    itemToSend.getCore().setRealY(item.getCore().getPage().toGridY(item.getCore().getLayoutY() - itemToSend.getCore().getLayoutBounds().getHeight()));
                }
                itemsToSend = new ArrayList<>();
            }
            
            if(items.size() > i + 1){
                GradeTreeItem afterItem = items.get(i + 1);
                
                if(!afterItem.getCore().isFilled()){ // si l'élément d'après n'a pas de valeur
                    if((item.getCore().isFilled() || item.hasSubGrade()) && !afterItemHaveToDropDown){
                        // Cas 1 : Ramène l'élément d'après à celui-ci
                        
                        if(afterItem.getCore().getPageNumber() != item.getCore().getPageNumber()){
                            afterItem.getCore().switchPage(item.getCore().getPageNumber());
                        }
                        afterItem.getCore().setRealY(item.getCore().getPage().toGridY(item.getCore().getLayoutY() + afterItem.getCore().getLayoutBounds().getHeight()));
                        
                        afterItemHaveToDropDown = false;
                    }else{
                        // Cas 2 : Demande a envoyer plus bas l'élément d'après
                        itemsToSend.add(afterItem);
                        afterItemHaveToDropDown = true;
                    }
                }else{
                    afterItemHaveToDropDown = false;
                }
            }
            i++;
        }
        // Drop down all others items in the dropDown array
        for(GradeTreeItem itemToSend : itemsToSend){
            if(itemToSend.getCore().getPageNumber() != MainWindow.mainScreen.document.getPagesNumber() - 1){
                itemToSend.getCore().switchPage(MainWindow.mainScreen.document.getPagesNumber() - 1);
            }
            itemToSend.getCore().setRealY((int) Element.GRID_HEIGHT);
        }
    }
    
    public static GradeTreeItem getTotal(){
        return (GradeTreeItem) MainWindow.gradeTab.treeView.getRoot();
    }
    
    public static ArrayList<GradeTreeItem> getGradesArray(GradeTreeItem root){
        ArrayList<GradeTreeItem> items = new ArrayList<>();
        items.add(root);
        
        for(int i = 0; i < root.getChildren().size(); i++){
            if(((GradeTreeItem) root.getChildren().get(i)).hasSubGrade()){
                items.addAll(getGradesArray((GradeTreeItem) root.getChildren().get(i)));
            }else{
                items.add((GradeTreeItem) root.getChildren().get(i));
            }
        }
        return items;
    }
    
    public static ArrayList<GradeElement> getGradesArrayByCoordinates(){
        
        return MainWindow.mainScreen.document.getPages()
                .stream()
                .map(page -> page.getElements()
                        .stream()
                        .filter(element -> element instanceof GradeElement)
                        .map(element -> (GradeElement) element)
                        .sorted(Comparator.comparingInt(Element::getRealY))
                        .collect(Collectors.toCollection(ArrayList::new)))
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    
    public static String getElementPath(GradeTreeItem parent){
        return parent.getCore().getParentPath() + "\\" + parent.getCore().getName();
    }
    
    public static int getElementTier(String parentPath){
        return StringUtils.cleanArray(parentPath.split(Pattern.quote("\\"))).length;
    }
    
    public void updateAllSum(){
        ArrayList<GradeTreeItem> items = getGradesArray(GradeTreeView.getTotal());
        
        for(GradeTreeItem item : items){
            if(item.hasSubGrade()){
                item.makeSum(false);
            }
        }
    }
    
    
    public GradeTreeItem getRootTreeItem(){
        return (GradeTreeItem) getRoot();
    }
    
    private ScrollBar getVerticalScrollbar(){
        return lookupAll(".scroll-bar").stream()
                .filter(node -> node instanceof ScrollBar)
                .map(node -> (ScrollBar) node)
                .filter(bar -> bar.getOrientation().equals(Orientation.VERTICAL))
                .findFirst()
                .orElse(null);
    }
    
    public double getVScrollbarVisibleWidth(){
        if(verticalScrollbar == null) return 0;
        return verticalScrollbar.isVisible() ? verticalScrollbar.getWidth() : 0;
    }
}
