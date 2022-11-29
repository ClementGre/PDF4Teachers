/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.datasaving.simpleconfigs;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.ListsManager;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextListItem;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TextElementsData extends SimpleConfig {
    
    public TextElementsData(){
        super("textelements");
    }
    
    @Override
    protected void manageLoadedData(Config config){
        Platform.runLater(() -> {
            // TEXTS
            for(Object data : config.getList("favorites")){
                if(data instanceof Map){
                    TextTreeItem item = TextTreeItem.readYAMLDataAndGive(Config.castSection(data), TextTreeSection.FAVORITE_TYPE);
                    if(!item.getText().isBlank()) {
                        MainWindow.textTab.treeView.favoritesSection.getChildren().add(item);
                    }
                }
                
            }
            
            for(Object data : config.getList("lasts")){
                if(data instanceof Map){
                    TextTreeItem item = TextTreeItem.readYAMLDataAndGive(Config.castSection(data), TextTreeSection.LAST_TYPE);
                    if(!item.getText().isBlank()) {
                        MainWindow.textTab.treeView.lastsSection.getChildren().add(item);
                    }
                }
            }
            
            for(Map.Entry<String, Object> list : config.getSection("lists").entrySet()){
                if(list.getValue() instanceof List){
                    ArrayList<TextListItem> listTexts = new ArrayList<>();
                    for(Object data : ((List<?>) list.getValue())){
                        listTexts.add(TextListItem.readYAMLDataAndGive(Config.castSection(data)));
                    }
                    listTexts = new ArrayList<>(listTexts.stream().filter((listItem) -> !listItem.getText().isBlank()).toList());
                    TextTreeSection.lists.put(list.getKey(), listTexts);
                }
            }
            
            MainWindow.textTab.treeView.favoritesSection.sortManager.simulateCall();
            MainWindow.textTab.treeView.lastsSection.sortManager.simulateCall();
            ListsManager.setupMenus();
        });
    }
    
    @Override
    protected void unableToLoadConfig(){
        ListsManager.setupMenus();
    }
    
    @Override
    protected void addDataToConfig(Config config){
        ArrayList<Object> favorites = MainWindow.textTab.treeView.favoritesSection.getChildren()
                .stream()
                .filter(item -> item instanceof TextTreeItem)
                .map(item -> ((TextTreeItem) item).getYAMLData())
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Object> lasts = MainWindow.textTab.treeView.lastsSection.getChildren()
                .stream()
                .filter(item -> item instanceof TextTreeItem)
                .map(item -> ((TextTreeItem) item).getYAMLData())
                .collect(Collectors.toCollection(ArrayList::new));

        LinkedHashMap<String, Object> lists = new LinkedHashMap<>();
        for(Map.Entry<String, ArrayList<TextListItem>> list : TextTreeSection.lists.entrySet()){
            List<Object> listTexts = list.getValue()
                    .stream()
                    .map(TextListItem::getYAMLData)
                    .collect(Collectors.toList());
            lists.put(list.getKey(), listTexts);
        }
        
        config.set("favorites", favorites);
        config.set("lasts", lasts);
        config.set("lists", lists);
    }
    
}
