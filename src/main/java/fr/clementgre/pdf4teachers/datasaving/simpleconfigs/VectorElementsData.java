package fr.clementgre.pdf4teachers.datasaving.simpleconfigs;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.VectorGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.VectorData;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.svg.DefaultFavoriteVectors;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class VectorElementsData extends SimpleConfig{
    
    public VectorElementsData(){
        super("vectorelements");
    }
    
    @Override
    protected void manageLoadedData(Config config){
        Platform.runLater(() -> {
    
            ArrayList<VectorData> favouriteVectorsData = new ArrayList<>();
            ArrayList<VectorData> lastVectorsData = new ArrayList<>();
    
            for(Object data : config.getList("favorites")){
                if(data instanceof HashMap map){
                    VectorData vectorData = VectorData.readYAMLDataAndGive(map);
                    if(!vectorData.getPath().isEmpty()) favouriteVectorsData.add(vectorData);
                }
            }
            for(Object data : config.getList("lasts")){
                if(data instanceof HashMap map){
                    VectorData vectorData = VectorData.readYAMLDataAndGive(map);
                    if(!vectorData.getPath().isEmpty()) lastVectorsData.add(vectorData);
                }
            }
            
            PlatformUtils.printActionTimeIfDebug(() -> {
                MainWindow.paintTab.favouriteVectors.loadVectorsList(favouriteVectorsData, false);
                MainWindow.paintTab.lastVectors.loadVectorsList(lastVectorsData, false);
            }, "Load favorites/last vectors");
        });
    }
    
    @Override
    protected void unableToLoadConfig(){
        if(Main.firstLaunch || Main.settings.hasVersionChanged()) MainWindow.paintTab.favouriteVectors.loadVectorsList(DefaultFavoriteVectors.getDefaultFavoriteVectors(), false);
    }
    
    @Override
    protected void addDataToConfig(Config config){
        ArrayList<Object> favorites = new ArrayList<>();
        for(VectorGridElement item : MainWindow.paintTab.favouriteVectors.getList().getAllItems()){
            if(!item.isFake()) favorites.add(item.getVectorData().getYAMLData());
        }
    
        ArrayList<Object> lasts = new ArrayList<>();
        for(VectorGridElement item : MainWindow.paintTab.lastVectors.getList().getAllItems()){
            if(!item.isFake()) lasts.add(item.getVectorData().getYAMLData());
        }
    
        config.set("favorites", favorites);
        config.set("lasts", lasts);
    }
    
}
