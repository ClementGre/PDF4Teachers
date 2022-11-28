/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.datasaving.simpleconfigs;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageData;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class FavouriteImageData extends SimpleConfig {
    
    public FavouriteImageData(){
        super("imageelements");
    }
    
    @Override
    protected void manageLoadedData(Config config){
        
        ArrayList<ImageData> favouriteImageData = config.getList("favorites")
                .stream()
                .filter(data -> data instanceof HashMap)
                .map(data -> (HashMap) data)
                .map(ImageData::readYAMLDataAndGive)
                .collect(Collectors.toCollection(ArrayList::new));

        PlatformUtils.printActionTimeIfDebug(() -> MainWindow.paintTab.favouriteImages.reloadFavouritesImageList(favouriteImageData, false), "Load favorites images");
        
    }
    
    @Override
    protected void unableToLoadConfig(){
    
    }
    
    @Override
    protected void addDataToConfig(Config config){
        ArrayList<Object> favorites = MainWindow.paintTab.favouriteImages.getList()
                .getAllItems()
                .stream()
                .filter(ImageGridElement::hasLinkedImageData)
                .map(item -> item.getLinkedImageData().getYAMLData())
                .collect(Collectors.toCollection(ArrayList::new));

        config.set("favorites", favorites);
    }
    
}
