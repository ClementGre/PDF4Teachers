package fr.clementgre.pdf4teachers.datasaving;

import fr.clementgre.pdf4teachers.Main;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class SyncUserData{
    
    // SUPPORTES VAR TYPES :
    // String, boolean, long, double, List, HashMap, LinkedHashMap
    
    @UserDataObject(path = "barsOrganization.leftBar")
    public List<String> leftBarOrganization = Arrays.asList("files", "text", "grades", "paint");
    
    @UserDataObject(path = "barsOrganization.rightBar")
    public List<String> rightBarOrganization = Collections.emptyList();
    
    @UserDataObject(path = "mainWindowSize.width")
    public long mainWindowWidth = 1300;
    @UserDataObject(path = "mainWindowSize.height")
    public long mainWindowHeight = 800;
    @UserDataObject(path = "mainWindowSize.x")
    public long mainWindowX = -1;
    @UserDataObject(path = "mainWindowSize.y")
    public long mainWindowY = -1;
    @UserDataObject(path = "mainWindowSize.fullScreen")
    public boolean mainWindowMaximized = false;
    
    //////
    
    private static final String FILE_NAME = "sync_userdata.yml";
    
    public SyncUserData(){
        load();
    }
    
    private void load(){
        loadYAML();
    }
    
    public void save(){
        saveYAML();
    }
    
    
    public void loadYAML(){
        try{
            new File(Main.dataFolder).mkdirs();
            File file = new File(Main.dataFolder + FILE_NAME);
            if(file.createNewFile()) return; // Config does not exists
            
            Config config = new Config(file);
            config.load();
            
            for(Field field : getClass().getDeclaredFields()){
                if(field.isAnnotationPresent(UserDataObject.class)){
                    try{
                        if(field.getType() == String.class){
                            String value = config.getString(field.getAnnotation(UserDataObject.class).path());
                            if(!value.isEmpty()) field.set(this, value);
                        }else if(field.getType() == boolean.class){
                            Boolean value = config.getBooleanNull(field.getAnnotation(UserDataObject.class).path());
                            if(value != null) field.set(this, value);
                        }else if(field.getType() == long.class){
                            Long value = config.getLongNull(field.getAnnotation(UserDataObject.class).path());
                            if(value != null) field.set(this, value);
                        }else if(field.getType() == double.class){
                            Double value = config.getDoubleNull(field.getAnnotation(UserDataObject.class).path());
                            if(value != null) field.set(this, value);
                        }else if(field.getType() == List.class){
                            List<Object> value = config.getListNull(field.getAnnotation(UserDataObject.class).path());
                            if(value != null) field.set(this, value);
                        }else if(field.getType() == HashMap.class){
                            field.set(this, config.getSection(field.getAnnotation(UserDataObject.class).path()));
                        }else if(field.getType() == LinkedHashMap.class){
                            field.set(this, config.getLinkedSection(field.getAnnotation(UserDataObject.class).path()));
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Unable to load " + FILE_NAME);
        }
    }
    
    public void saveYAML(){
        try{
            new File(Main.dataFolder).mkdirs();
            Config config = new Config(new File(Main.dataFolder + FILE_NAME));
            
            for(Field field : getClass().getDeclaredFields()){
                if(field.isAnnotationPresent(UserDataObject.class)){
                    try{
                        config.set(field.getAnnotation(UserDataObject.class).path(), field.get(this));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            
            config.save();
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Unable to save " + FILE_NAME);
        }
    }
    
}
