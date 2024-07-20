/*
 * Copyright (c) 2019-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.texts;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeFavorites;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeLasts;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeOnFile;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.sort.Sorter;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TextTreeView extends TreeView<String> {
    
    public TreeItem<String> treeViewRoot = new TreeItem<>();
    
    public TextTreeFavorites favoritesSection = new TextTreeFavorites();
    public TextTreeLasts lastsSection = new TextTreeLasts();
    public TextTreeOnFile onFileSection = new TextTreeOnFile();
    
    public TextTreeView(Pane pane){
        
        setShowRoot(false);
        setEditable(true);
        setRoot(treeViewRoot);
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeViewRoot.getChildren().addAll(favoritesSection, lastsSection, onFileSection);
        
        disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        setBackground(new Background(new BackgroundFill(Color.rgb(244, 244, 244), CornerRadii.EMPTY, Insets.EMPTY)));
        
        prefHeightProperty().bind(pane.heightProperty().subtract(layoutYProperty()));
        prefWidthProperty().bind(pane.widthProperty());
        widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            // Update element's graphic only if it is the last width value
            new Thread(() -> {
                try{
                    Thread.sleep(200);
                }catch(InterruptedException e){
                    Log.eNotified(e);
                }
                Platform.runLater(() -> {
                    if(getWidth() == newValue.longValue()) updateListsGraphic();
                });
            }).start();
        });
        
        getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if(newValue instanceof TextTreeItem textTreeItem){
                    if(MainWindow.textTab.treeView.getSelectionModel().getSelectedItem() == newValue)
                        textTreeItem.onSelected();
                }
            });
        });
        
        setCellFactory((TreeView<String> param) -> new TreeCell<>() {
            @Override
            protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                
                // Null
                if(empty){
                    setGraphic(null);
                    setStyle(null);
                    setContextMenu(null);
                    setOnMouseClicked(null);
                    return;
                }
                
                // TextElement
                if(getTreeItem() instanceof TextTreeItem){
                    ((TextTreeItem) getTreeItem()).updateCell(this);
                    return;
                }
                // TreeSection
                if(getTreeItem() instanceof TextTreeSection){
                    ((TextTreeSection) getTreeItem()).updateCell(this);
                    return;
                }
                // SortPanel
                if(getTreeItem() instanceof SortPanelTreeItem){
                    ((SortPanelTreeItem) getTreeItem()).updateCell(this);
                    return;
                }
                
                // Other
                setStyle(null);
                setGraphic(null);
                setContextMenu(null);
                setOnMouseClicked(null);
            }
        });
        
        if(Main.settings.textSmall.getValue()){
            Platform.runLater(this::refresh);
        }
        Main.settings.textSmall.valueProperty().addListener((observable, oldValue, newValue) -> {
            refresh();
        });
        Main.settings.textOnlyStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            refresh();
        });
    }
    
    public static ContextMenu getCategoryMenu(TextTreeSection section){
        
        ContextMenu menu = new ContextMenu();
        
        
        if(section.sectionType == TextTreeSection.ONFILE_TYPE){
            NodeMenuItem item3 = new NodeMenuItem(TR.tr("textTab.onDocumentList.menu.clear"), true);
            item3.setToolTip(TR.tr("textTab.onDocumentList.menu.clear.tooltip"));
            menu.getItems().addAll(item3);
            
            item3.setOnAction(e -> {
                for(PageRenderer page : MainWindow.mainScreen.document.getPages()){
                    page.clearTextElements();
                }
                MainWindow.textTab.treeView.onFileSection.updateElementsList();
                Edition.setUnsave("Clear onDocument TextElements");
            });
        }else{
            NodeMenuItem item1 = new NodeMenuItem(TR.tr("menuBar.file.clearList"), true);
            item1.setToolTip(TR.tr("textTab.listMenu.clear.tooltip"));
            NodeMenuItem item2 = new NodeMenuItem(TR.tr("textTab.listMenu.clear.resetUseData"), true);
            item2.setToolTip(TR.tr("textTab.listMenu.clear.resetUseData.tooltip"));
            menu.getItems().addAll(item1, item2);
            
            item1.setOnAction(e -> {
                section.clearElements(true);
            });
            item2.setOnAction(e -> {
                for(Object element : section.getChildren()){
                    if(element instanceof TextTreeItem){
                        ((TextTreeItem) element).setUses(0);
                    }
                }
                if(section.sortManager.getSelectedButton().getText().equals(TR.tr("sorting.sortType.use"))){
                    section.sortManager.simulateCall();
                }
            });
        }
        NodeMenuItem.setupMenu(menu);
        return menu;
    }
    
    public void updateAutoComplete(){
        
        getSelectionModel().clearSelection();
        String matchText = MainWindow.textTab.txtArea.getText();
        
        
        if(!MainWindow.textTab.txtArea.isDisabled() && !matchText.isBlank()){
            
            int totalIndex = selectSectionIndices(1, favoritesSection, matchText);
            totalIndex = selectSectionIndices(totalIndex + 1, lastsSection, matchText);
            selectSectionIndices(totalIndex + 1, onFileSection, matchText);
            
        }
        getSelectionModel().select(null);
        
    }
    private int selectSectionIndices(int totalIndex, TextTreeSection section, String matchText){
        int i;
        for(i = 0; i < section.getChildren().size(); i++){
            if(section.getChildren().get(i) instanceof TextTreeItem item){
                if(item.getCore() != MainWindow.mainScreen.getSelected()
                        && TextElement.invertMathIfNeeded(item.getText()).toLowerCase().contains(matchText.toLowerCase())){
                    getSelectionModel().selectIndices(totalIndex + i, getSelectionModel().getSelectedIndices().stream().mapToInt(value -> value).toArray());
                }
            }
        }
        return totalIndex + i;
    }
    
    public boolean selectNextInSelection(){
        if(!getSelectionModel().getSelectedIndices().isEmpty()){
            
            if(getSelectionModel().getSelectedItem() == null){
                selectFromSelectedIndex(0);
            }else{
                int lastIndex = getSelectionModel().getSelectedIndices().size() - 1;
                int toSelectIndex = getSelectionModel().getSelectedIndices().indexOf(getSelectionModel().getSelectedIndex()) + 1;
                
                if(toSelectIndex > lastIndex) selectTextField();
                else selectFromSelectedIndex(toSelectIndex);
            }
            return true;
        }
        return false;
    }
    
    public boolean selectPreviousInSelection(){
        if(!getSelectionModel().getSelectedIndices().isEmpty()){
            int lastIndex = getSelectionModel().getSelectedIndices().size() - 1;
            
            if(getSelectionModel().getSelectedItem() == null){
                selectFromSelectedIndex(lastIndex);
            }else{
                int toSelectIndex = getSelectionModel().getSelectedIndices().indexOf(getSelectionModel().getSelectedIndex()) - 1;
                
                if(toSelectIndex < 0) selectTextField();
                else selectFromSelectedIndex(toSelectIndex);
            }
            return true;
        }
        return false;
    }
    
    private void selectTextField(){
        scrollTo(0);
        getSelectionModel().select(null);
        MainWindow.textTab.txtArea.requestFocus();
    }
    
    private void selectFromSelectedIndex(int index){
        scrollTo(getSelectionModel().getSelectedIndices().get(index) - 3);
        int realIndex = getSelectionModel().getSelectedIndices().get(index);
        
        if(getTreeItem(realIndex) instanceof TextTreeItem textTreeItem){
            PlatformUtils.runLaterOnUIThread(25, () -> {
                getSelectionModel().select(textTreeItem);
            });
        }
    }
    
    
    public static ContextMenu getNewMenu(TextTreeItem element){
        
        ContextMenu menu = new ContextMenu();
        NodeMenuItem item1 = new NodeMenuItem(TR.tr("textTab.listMenu.addNLink"), false);
        item1.setToolTip(TR.tr("textTab.listMenu.addNLink.tooltip"));
        NodeMenuItem item2 = new NodeMenuItem(TR.tr("actions.remove"), false);
        item2.setToolTip(TR.tr("textTab.listMenu.remove.tooltip"));
        NodeMenuItem item3 = new NodeMenuItem(TR.tr("elementMenu.addToFavouriteList"), false);
        item3.setToolTip(TR.tr("elementMenu.addToPreviousList.tooltip"));
        NodeMenuItem item4 = new NodeMenuItem(TR.tr("elementMenu.addToPreviousList"), false);
        item4.setToolTip(TR.tr("elementMenu.addToFavouritesList.tooltip"));
        NodeMenuItem item5 = new NodeMenuItem(TR.tr("textTab.listMenu.unlink"), false);
        item5.setToolTip(TR.tr("textTab.listMenu.unlink.tooltip"));
        
        
        // Ajouter les items en fonction du type
        if(element.getType() != TextTreeSection.ONFILE_TYPE) menu.getItems().add(item1);
        menu.getItems().add(item2);
        if(element.getType() != TextTreeSection.FAVORITE_TYPE) menu.getItems().add(item3); // onFile & lasts
        if(element.getType() == TextTreeSection.ONFILE_TYPE) menu.getItems().add(item4); // onFile
        if(element.getType() != TextTreeSection.ONFILE_TYPE && element.getCore() != null)
            menu.getItems().add(item5); // élément précédent qui est lié
        
        NodeMenuItem.setupMenu(menu);
        
        // Définis les actions des boutons
        item1.setOnAction((e) -> {
            element.addToDocument(true, true);
            if(element.getType() == TextTreeSection.FAVORITE_TYPE){
                if(MainWindow.textTab.treeView.favoritesSection.sortManager.getSelectedButton().getText().equals(TR.tr("sorting.sortType.use"))){
                    MainWindow.textTab.treeView.favoritesSection.sortManager.simulateCall();
                }
            }else if(element.getType() == TextTreeSection.LAST_TYPE){
                if(MainWindow.textTab.treeView.lastsSection.sortManager.getSelectedButton().getText().equals(TR.tr("sorting.sortType.use"))){
                    MainWindow.textTab.treeView.lastsSection.sortManager.simulateCall();
                }
            }
            MainWindow.textTab.selectItem();
        });
        item2.setOnAction((e) -> {
            if(element.getType() == TextTreeSection.ONFILE_TYPE){
                element.getCore().delete(true, UType.ELEMENT);
            }else{
                removeSavedElement(element);
            }
        });
        item3.setOnAction((e) -> {
            addSavedElement(new TextTreeItem(element.getFont(), element.getText(), element.getColor(), element.getMaxWidth(), TextTreeSection.FAVORITE_TYPE, 0, System.currentTimeMillis() / 1000));
            if(element.getType() == TextTreeSection.LAST_TYPE){
                if(Main.settings.listsMoveAndDontCopy.getValue()){
                    removeSavedElement(element);
                }
            }
        });
        item4.setOnAction((e) -> {
            addSavedElement(new TextTreeItem(element.getFont(), element.getText(), element.getColor(), element.getMaxWidth(), TextTreeSection.LAST_TYPE, 0, System.currentTimeMillis() / 1000));
        });
        item5.setOnAction((e) -> {
            element.unLink(true);
        });
        return menu;
        
    }
    
    public void onCloseDocument(){
        favoritesSection.unlinkAll();
        lastsSection.unlinkAll();
        onFileSection.clearElements(true);
    }
    
    public static void updateListsGraphic(){
        MainWindow.textTab.treeView.favoritesSection.updateChildrenGraphics();
        MainWindow.textTab.treeView.lastsSection.updateChildrenGraphics();
        MainWindow.textTab.treeView.onFileSection.updateChildrenGraphics();
    }
    
    public static void addSavedElement(TextTreeItem element){
        if(element.getType() == TextTreeSection.FAVORITE_TYPE){
            MainWindow.textTab.treeView.favoritesSection.addElement(element);
        }else if(element.getType() == TextTreeSection.LAST_TYPE){
            MainWindow.textTab.treeView.lastsSection.addElement(element);
        }
    }
    
    public static void removeSavedElement(TextTreeItem element){
        if(element.getType() == TextTreeSection.FAVORITE_TYPE){
            MainWindow.textTab.treeView.favoritesSection.removeElement(element);
        }else if(element.getType() == TextTreeSection.LAST_TYPE){
            MainWindow.textTab.treeView.lastsSection.removeElement(element);
        }
    }
    
    public static List<TextTreeItem> autoSortList(List<TextTreeItem> toSort, String sortType, boolean order){
        
        if(sortType.equals(TR.tr("sorting.sortType.addDate.short"))){
            return Sorter.sortElementsByDate(toSort, order);
        }
        if(sortType.equals(TR.tr("sorting.sortType.name"))){
            return Sorter.sortElementsByName(toSort, order);
        }
        if(sortType.equals(TR.tr("sorting.sortType.use"))){
            return Sorter.sortElementsByUtils(toSort, order);
        }
        if(sortType.equals(TR.tr("sorting.sortType.fontFamily"))){
            return Sorter.sortElementsByPolice(toSort, order);
        }
        if(sortType.equals(TR.tr("sorting.sortType.fontSize"))){
            return Sorter.sortElementsBySize(toSort, order);
        }
        if(sortType.equals(TR.tr("string.color"))){
            return Sorter.sortElementsByColor(toSort, order);
        }
        if(sortType.equals(TR.tr("sorting.sortType.location"))){
            return Sorter.sortElementsByCorePosition(toSort, order);
        }
        return toSort;
    }
    
    public static List<TextTreeItem> getMostUseElements(){
        
        List<TextTreeItem> toSort = IntStream.range(0, MainWindow.textTab.treeView.favoritesSection.getChildren().size())
                .filter(i -> MainWindow.textTab.treeView.favoritesSection.getChildren().get(i) instanceof TextTreeItem)
                .mapToObj(i -> (TextTreeItem) MainWindow.textTab.treeView.favoritesSection.getChildren().get(i))
                .collect(Collectors.toList());
        for(int i = 0; i < MainWindow.textTab.treeView.lastsSection.getChildren().size(); i++){
            if(MainWindow.textTab.treeView.lastsSection.getChildren().get(i) instanceof TextTreeItem){
                toSort.add((TextTreeItem) MainWindow.textTab.treeView.lastsSection.getChildren().get(i));
            }
        }
        return autoSortList(toSort, TR.tr("sorting.sortType.use"), true);
        
    }
}


