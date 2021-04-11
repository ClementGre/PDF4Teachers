package fr.clementgre.pdf4teachers.datasaving;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("unchecked")
public class VectorElementsData{
    
    public VectorElementsData(){
        loadDataFromYAML();
    }
    
    private void loadDataFromYAML(){
    
        new Thread(() -> {
        
            try{
                new File(Main.dataFolder).mkdirs();
                File file = new File(Main.dataFolder + "vectorelements.yml");
                if(file.createNewFile()) return; // File does not exist or can't create it
            
                Config config = new Config(file);
                config.load();
            
                Platform.runLater(() -> {
                    // TEXTS
                    for(Object data : config.getList("favorites")){
                        if(data instanceof Map){
                            // TODO : init VectorData and autoAdd to list. Ex :
                            // MainWindow.textTab.treeView.favoritesSection.getChildren().add(TextTreeItem.readYAMLDataAndGive(Config.castSection(data), TextTreeSection.FAVORITE_TYPE));
                        }
                    
                    }
                
                    for(Object data : config.getList("lasts")){
                        if(data instanceof Map){
                            // TODO : init VectorData and autoAdd to list
                        }
                    }
                
                    // TODO : call sort managers
                });
            
            }catch(IOException e){
                e.printStackTrace();
            }
        }, "VectorElementsData loader").start();
    }
    
    public void saveData(){
    
        ArrayList<Object> favorites = new ArrayList<>();
        for(Object item : MainWindow.textTab.treeView.favoritesSection.getChildren()){
            // TODO : add YAML data in the list favorites Ex :
            //favorites.add(((TextTreeItem) item).getYAMLData());
        }
    
        ArrayList<Object> lasts = new ArrayList<>();
        for(Object item : MainWindow.textTab.treeView.lastsSection.getChildren()){
            // TODO : add YAML data in the list lasts
        }
    
        try{
            new File(Main.dataFolder).mkdirs();
            Config config = new Config(new File(Main.dataFolder + "vectorelements.yml"));
        
            config.set("favorites", favorites);
            config.set("lasts", lasts);
        
            config.save();
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }
    
}
