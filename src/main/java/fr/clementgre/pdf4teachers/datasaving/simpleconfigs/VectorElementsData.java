package fr.clementgre.pdf4teachers.datasaving.simpleconfigs;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.datasaving.UserData;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.application.Platform;
import java.util.*;

@SuppressWarnings("unchecked")
public class VectorElementsData extends SimpleConfig{
    
    public VectorElementsData(){
        super("vectorelements");
    }
    
    @Override
    protected void manageLoadedData(Config config){
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
    }
    
    @Override
    protected void unableToLoadConfig(){
    
    }
    
    @Override
    protected void addDataToConfig(Config config){
        ArrayList<Object> favorites = new ArrayList<>();
        for(Object item : MainWindow.textTab.treeView.favoritesSection.getChildren()){
            // TODO : add YAML data in the list favorites Ex :
            //favorites.add(((TextTreeItem) item).getYAMLData());
        }
    
        ArrayList<Object> lasts = new ArrayList<>();
        for(Object item : MainWindow.textTab.treeView.lastsSection.getChildren()){
            // TODO : add YAML data in the list lasts
        }
    
        config.set("favorites", favorites);
        config.set("lasts", lasts);
    }
    
}
