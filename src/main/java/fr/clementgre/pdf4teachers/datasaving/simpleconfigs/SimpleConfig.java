/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.datasaving.simpleconfigs;


import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.datasaving.UserData;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;

import java.io.File;

public abstract class SimpleConfig {
    
    public static void registerClasses(){
        UserData.registerSimpleConfig(new FavouriteImageData());
        UserData.registerSimpleConfig(new TextElementsData());
        UserData.registerSimpleConfig(new VectorElementsData());
        UserData.registerSimpleConfig(new SkillsAssessmentData());
        UserData.registerSimpleConfig(new SystemFontsData());
    }
    
    private final String filename;
    public SimpleConfig(String fileName){
        filename = fileName;
    }
    
    protected abstract void manageLoadedData(Config config);
    protected abstract void unableToLoadConfig();
    protected abstract void addDataToConfig(Config config);
    
    public final void loadData(){
        
        new Thread(() -> {
            
            try{
                new File(Main.dataFolder).mkdirs();
                File file = new File(Main.dataFolder + filename + ".yml");
                if(file.createNewFile()){
                    unableToLoadConfig();
                    return; // File does not exist or can't create it
                }
                
                Config config = new Config(file);
                config.load();
                
                manageLoadedData(config); // Subclass cass
                
            }catch(Exception e){
                Log.eNotified(e, "Unable to load " + filename);
                unableToLoadConfig();
            }
        }, "SimpleConfigs loader").start();
    }
    public final void saveData(){
        try{
            new File(Main.dataFolder).mkdirs();
            Config config = new Config(new File(Main.dataFolder + filename + ".yml"));
            
            addDataToConfig(config); // Subclass cass
            
            config.save();
        }catch(Exception e){
            Log.eNotified(e, "Unable to save " + filename);
        }
    }
    
}
