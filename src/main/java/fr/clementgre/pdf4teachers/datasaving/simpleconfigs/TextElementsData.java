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

public class TextElementsData extends SimpleConfig{
    
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
                    if(!item.getText().isBlank()) MainWindow.textTab.treeView.favoritesSection.getChildren().add(item);
                }
                
            }
        
            for(Object data : config.getList("lasts")){
                if(data instanceof Map){
                    TextTreeItem item = TextTreeItem.readYAMLDataAndGive(Config.castSection(data), TextTreeSection.LAST_TYPE);
                    if(!item.getText().isBlank()) MainWindow.textTab.treeView.lastsSection.getChildren().add(item);
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
        ArrayList<Object> favorites = new ArrayList<>();
        for(Object item : MainWindow.textTab.treeView.favoritesSection.getChildren()){
            if(item instanceof TextTreeItem) favorites.add(((TextTreeItem) item).getYAMLData());
        }
    
        ArrayList<Object> lasts = new ArrayList<>();
        for(Object item : MainWindow.textTab.treeView.lastsSection.getChildren()){
            if(item instanceof TextTreeItem) lasts.add(((TextTreeItem) item).getYAMLData());
        }
    
        LinkedHashMap<String, Object> lists = new LinkedHashMap<>();
        for(Map.Entry<String, ArrayList<TextListItem>> list : TextTreeSection.lists.entrySet()){
            List<Object> listTexts = new ArrayList<>();
            for(TextListItem item : list.getValue()) listTexts.add(item.getYAMLData());
            lists.put(list.getKey(), listTexts);
        }
    
        config.set("favorites", favorites);
        config.set("lasts", lasts);
        config.set("lists", lists);
    }
    
}
