/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.datasaving.simpleconfigs;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.VectorData;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.svg.DefaultFavoriteVectors;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class VectorElementsData extends SimpleConfig {
    
    public VectorElementsData(){
        super("vectorelements");
    }
    
    @Override
    protected void manageLoadedData(Config config){
        Platform.runLater(() -> {
            
            ArrayList<VectorData> favouriteVectorsData = config.getList("favorites")
                    .stream().filter(data -> data instanceof HashMap)
                    .map(data -> (HashMap) data)
                    .map(VectorData::readYAMLDataAndGive)
                    .filter(vectorData -> !vectorData.getPath().isEmpty())
                    .collect(Collectors.toCollection(ArrayList::new));

            ArrayList<VectorData> lastVectorsData = config.getList("lasts")
                    .stream().filter(data -> data instanceof HashMap)
                    .map(data -> (HashMap) data)
                    .map(VectorData::readYAMLDataAndGive)
                    .filter(vectorData -> !vectorData.getPath().isEmpty())
                    .collect(Collectors.toCollection(ArrayList::new));

            PlatformUtils.printActionTimeIfDebug(() -> {
                MainWindow.paintTab.favouriteVectors.loadVectorsList(favouriteVectorsData, false);
                MainWindow.paintTab.lastVectors.loadVectorsList(lastVectorsData, false);
            }, "Load favorites/last vectors");
        });
    }
    
    @Override
    protected void unableToLoadConfig(){
        if(Main.firstLaunch || Main.settings.hasVersionChanged())
            MainWindow.paintTab.favouriteVectors.loadVectorsList(DefaultFavoriteVectors.getDefaultFavoriteVectors(), false);
    }
    
    @Override
    protected void addDataToConfig(Config config){
        ArrayList<Object> favorites = MainWindow.paintTab.favouriteVectors.getList()
                .getAllItems()
                .stream()
                .filter(item -> !item.isFake())
                .map(item -> item.getVectorData().getYAMLData())
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Object> lasts = MainWindow.paintTab.lastVectors.getList()
                .getAllItems()
                .stream()
                .filter(item -> !item.isFake())
                .map(item -> item.getVectorData().getYAMLData())
                .collect(Collectors.toCollection(ArrayList::new));

        config.set("favorites", favorites);
        config.set("lasts", lasts);
    }
    
}
