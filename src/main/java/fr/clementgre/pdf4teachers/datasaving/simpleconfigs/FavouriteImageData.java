package fr.clementgre.pdf4teachers.datasaving.simpleconfigs;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageData;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;

import java.util.*;

@SuppressWarnings("unchecked")
public class FavouriteImageData extends SimpleConfig{
    
    public FavouriteImageData(){
        super("imageelements");
    }
    
    @Override
    protected void manageLoadedData(Config config){
        
        ArrayList<ImageData> favouriteImageData = new ArrayList<>();
        for(Object data : config.getList("favorites")){
            if(data instanceof HashMap map){
                favouriteImageData.add(ImageData.readYAMLDataAndGive(map));
            }
        }
        
        PlatformUtils.printActionTimeIfDebug(() -> {
            MainWindow.paintTab.favouriteImages.reloadFavouritesImageList(favouriteImageData, false);
        }, "Load favorites images");
        
    }
    
    @Override
    protected void unableToLoadConfig(){
    
    }
    
    @Override
    protected void addDataToConfig(Config config){
        ArrayList<Object> favorites = new ArrayList<>();
        for(ImageGridElement item : MainWindow.paintTab.favouriteImages.getList().getAllItems()){
            if(item.hasLinkedImageData()) favorites.add(item.getLinkedImageData().getYAMLData());
        }
        
        config.set("favorites", favorites);
    }
    
}
