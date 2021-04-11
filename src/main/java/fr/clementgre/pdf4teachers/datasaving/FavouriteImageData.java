package fr.clementgre.pdf4teachers.datasaving;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageData;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class FavouriteImageData{
    
    public FavouriteImageData(){
        loadDataFromYAML();
    }
    
    private void loadDataFromYAML(){
        
        new Thread(() -> {
            
            try{
                new File(Main.dataFolder).mkdirs();
                File file = new File(Main.dataFolder + "imageelements.yml");
                if(file.createNewFile()) return; // File does not exist or can't create it
                
                Config config = new Config(file);
                config.load();
                
                ArrayList<ImageData> favouriteImageData = new ArrayList<>();
                
                Platform.runLater(() -> {
                    // TEXTS
                    for(Object data : config.getList("favorites")){
                        if(data instanceof HashMap map){
                            favouriteImageData.add(ImageData.readYAMLDataAndGive(map));
                        }
                    }
                    
                    MainWindow.paintTab.favouriteImages.getList().editImages(favouriteImageData.stream().map(ImageGridElement::new).toList());
                });
                
            }catch(IOException e){
                e.printStackTrace();
            }
        }, "FavouriteImageData loader").start();
    }
    
    public void saveData(){
        
        ArrayList<Object> favorites = new ArrayList<>();
        for(ImageGridElement item : MainWindow.paintTab.favouriteImages.getList().getAllItems()){
            if(item.hasLinkedImageData()) favorites.add(item.getLinkedImageData().getYAMLData());
        }
        
        try{
            new File(Main.dataFolder).mkdirs();
            Config config = new Config(new File(Main.dataFolder + "imageelements.yml"));
            
            config.set("favorites", favorites);
            
            config.save();
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }
    
}
