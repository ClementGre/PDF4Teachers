/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.texts;

import fr.clementgre.pdf4teachers.components.NoArrowMenuButton;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.*;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuButton;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class ListsManager {
    
    public final MenuButton loadListBtn = new NoArrowMenuButton();
    public final Button saveListBtn = new Button();
    
    private final TextTreeSection section;
    
    public ListsManager(TextTreeSection section){
        this.section = section;
        
        loadListBtn.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.LIST, "black", 0, 18, ImageUtils.defaultDarkColorAdjust));
        loadListBtn.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("textTab.lists.show.tooltip")));
        saveListBtn.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.SAVE, "black", 0, 18, ImageUtils.defaultDarkColorAdjust));
        saveListBtn.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("textTab.lists.save.tooltip")));
        
        PaneUtils.setPosition(loadListBtn, 0, 0, 30, 30, true);
        PaneUtils.setPosition(saveListBtn, 0, 0, 30, 30, true);
        
        updateGraphics();
        
        setupMenu();
//        loadListBtn.setOnMouseClicked(e -> {
//            menu.show(loadListBtn, e.getScreenX(), e.getScreenY());
//        });
        
        saveListBtn.setOnAction(event -> {
            TextInputAlert inputAlert = new TextInputAlert(TR.tr("textTab.lists.save.dialog.title"), TR.tr("textTab.lists.save.dialog.header"), TR.tr("textTab.lists.save.dialog.details"));
            inputAlert.setText(TR.tr("textTab.lists.defaultName"));
            
            if(inputAlert.getShowAndWaitIsDefaultButton() && !inputAlert.getText().isBlank()){
                if(TextTreeSection.lists.containsKey(inputAlert.getText())){
                    CustomAlert alert = new CustomAlert(Alert.AlertType.WARNING, TR.tr("textTab.lists.save.alreadyExistDialog.title"),
                            TR.tr("textTab.lists.save.alreadyExistDialog.header"));
                    
                    alert.addButton(TR.tr("actions.rename"), ButtonPosition.CLOSE);
                    alert.addButton(TR.tr("dialog.actionError.overwrite"), ButtonPosition.DEFAULT);
                    
                    if(alert.getShowAndWaitIsCancelButton()){
                        saveListBtn.fire();
                    }else{
                        saveList(inputAlert.getText());
                    }
                }else saveList(inputAlert.getText());
            }
        });
    }
    
    public void updateGraphics(){
        loadListBtn.setStyle("-fx-background-color: " + StyleManager.getHexAccentColor() + ";");
        saveListBtn.setStyle("-fx-background-color: " + StyleManager.getHexAccentColor() + ";");
    }
    
    public static void setupMenus(){
        MainWindow.textTab.treeView.favoritesSection.listsManager.setupMenu();
        MainWindow.textTab.treeView.lastsSection.listsManager.setupMenu();
    }
    
    public void setupMenu(){
        loadListBtn.getItems().clear();
        //menu.setMinWidth(400);
        //menu.setPrefWidth(400);
        
        if(!TextTreeSection.lists.isEmpty()){
            for(Map.Entry<String, ArrayList<TextListItem>> list : TextTreeSection.lists.entrySet()){
                NodeMenuItem menuItem = new NodeMenuItem(list.getKey(), false);
                loadListBtn.getItems().add(menuItem);
                menuItem.setOnAction(event -> {
                    
                    CustomAlert alert = new CustomAlert(Alert.AlertType.CONFIRMATION, TR.tr("textTab.lists.actionDialog.title"),
                            TR.tr("textTab.lists.actionDialog.header"), TR.tr("textTab.lists.actionDialog.details"));
                    
                    alert.addCancelButton(ButtonPosition.CLOSE);
                    ButtonType load = alert.addButton(TR.tr("actions.load"), ButtonPosition.DEFAULT);
                    ButtonType loadReplace = alert.addButton(TR.tr("actions.clearAndLoad"), ButtonPosition.OTHER_LEFT);
                    ButtonType delete = alert.addButton(TR.tr("actions.delete"), ButtonPosition.OTHER_LEFT);
                    
                    ButtonType result = alert.getShowAndWait();
                    if(result == load) loadList(list.getValue(), false);
                    else if(result == loadReplace) loadList(list.getValue(), true);
                    else if(result == delete) deleteList(list.getKey());
                    
                });
            }
        }else{
            loadListBtn.getItems().add(new NodeMenuItem(TR.tr("textTab.lists.show.none"), false));
        }
    }
    
    public void loadList(ArrayList<TextListItem> items, boolean flush){
        if(flush) section.clearElements(true);
        for(TextListItem item : items) section.getChildren().add(item.toTextTreeItem(section.sectionType));
        section.sortManager.simulateCall();
    }
    
    public void saveList(String listName){
        TextTreeSection.lists.remove(listName);
        ArrayList<TextListItem> list = section.getChildren()
                .stream()
                .filter(item -> item instanceof TextTreeItem)
                .map(item -> ((TextTreeItem) item).toTextItem())
                .collect(Collectors.toCollection(ArrayList::new));
        if(list.isEmpty()){
            new WrongAlert(Alert.AlertType.ERROR, TR.tr("textTab.lists.save.voidListDialog.title"),
                    TR.tr("textTab.lists.save.voidListDialog.header"), TR.tr("textTab.lists.save.voidListDialog.details"), false).showAndWait();
            return;
        }
        TextTreeSection.lists.put(listName, list);
        
        new OKAlert(TR.tr("textTab.lists.save.completedDialog.title"),
                TR.tr("textTab.lists.save.completedDialog.header"), TR.tr("textTab.lists.save.completedDialog.details")).showAndWait();
        ListsManager.setupMenus();
    }
    
    public void deleteList(String listName){
        TextTreeSection.lists.remove(listName);
        
        new OKAlert(TR.tr("textTab.lists.deleteCompletedDialog.title"),
                TR.tr("textTab.lists.deleteCompletedDialog.header", listName)).show();
        ListsManager.setupMenus();
    }
    
}
