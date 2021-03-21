package fr.clementgre.pdf4teachers.datasaving;

import fr.clementgre.pdf4teachers.Main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SyncUserData{
    
    // SUPPORTES VAR TYPES :
    // String, boolean, long, double, List, HashMap, LinkedHashMap
    
    @UserDataObject(path = "barsOrganization.leftBar")
    public List<String> leftBarOrganization = Arrays.asList("files", "text");
    
    @UserDataObject(path = "barsOrganization.rightBar")
    public List<String> rightBarOrganization = Arrays.asList("grades", "paint");
    
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
        }catch(IOException e){
            e.printStackTrace();
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
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
}
