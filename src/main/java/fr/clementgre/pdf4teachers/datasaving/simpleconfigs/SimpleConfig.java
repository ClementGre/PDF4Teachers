package fr.clementgre.pdf4teachers.datasaving.simpleconfigs;


import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.datasaving.UserData;

import java.io.File;

public abstract class SimpleConfig{
    
    public static void registerClasses(){
        UserData.registerSimpleConfig(new FavouriteImageData());
        UserData.registerSimpleConfig(new TextElementsData());
        UserData.registerSimpleConfig(new VectorElementsData());
        UserData.registerSimpleConfig(new SystemFontsData());
    }
    
    private final String filename;
    public SimpleConfig(String fileName){
        this.filename = fileName;
    }
    
    protected abstract void manageLoadedData(Config config);
    protected abstract void unableToLoadConfig();
    protected abstract void addDataToConfig(Config config);
    
    public final void loadData(){
        if(Main.settings.getSettingsVersion().equals("1.2.0")) return;
        
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
                e.printStackTrace();
                System.err.println("Unable to load " + filename);
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
            e.printStackTrace();
            System.err.println("Unable to save " + filename);
        }
    }
    
}
