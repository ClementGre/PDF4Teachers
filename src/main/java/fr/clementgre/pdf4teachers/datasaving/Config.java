/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.datasaving;

import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.scene.paint.Color;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class Config {
    
    public HashMap<String, Object> base = new HashMap<>();
    
    private Yaml yaml;
    private File file;
    private File destFile;
    private String name;
    
    public Config(){
        setupYAML();
    }
    
    public Config(File file) throws IOException{
        file.createNewFile();
        this.file = file;
        setupYAML();
    }
    
    static {
        System.setProperty("yaml.max.aliases", "99999");
    }
    private void setupYAML(){
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setWidth(120);
        
        LoaderOptions options = new LoaderOptions();
        options.setMaxAliasesForCollections(Integer.MAX_VALUE);
        options.setAllowRecursiveKeys(true);
        
        yaml = new Yaml(new SafeConstructor(options));
    }
    
    public void load() throws IOException{
        if(file == null) return;
        
        InputStream input = new FileInputStream(file);
        base = yaml.load(input);
        input.close();
        
        
        if(base == null) base = new HashMap<>();
    }
    
    public void save() throws IOException{
        if(file == null) return;
        Writer output = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        yaml.dump(base, output);
        output.close();
    }
    
    public void saveTo(File file) throws IOException{
        Writer output = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        yaml.dump(base, output);
        output.close();
    }
    
    public void saveToDestFile() throws IOException{
        if(destFile == null) return;
        Writer output = new OutputStreamWriter(new FileOutputStream(destFile), StandardCharsets.UTF_8);
        yaml.dump(base, output);
        output.close();
    }
    
    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public File getFile(){
        return file;
    }
    
    public void setFile(File file){
        this.file = file;
    }
    
    public File getDestFile(){
        return destFile;
    }
    
    public void setDestFile(File destFile){
        this.destFile = destFile;
    }
    
    // GET SECTION / CASTS
    
    public static ArrayList<Object> castList(Object list){
        if(list instanceof List) return (ArrayList<Object>) list;
        return new ArrayList<>();
    }
    
    public static HashMap<String, Object> castSection(Object list){
        if(list instanceof Map) return (HashMap<String, Object>) list;
        return new HashMap<>();
    }
    
    public static Object getValue(HashMap<String, Object> base, String path){
        
        String[] splitedPath = StringUtils.cleanArray(path.split(Pattern.quote(".")));
        HashMap<String, Object> section = base;
        int i = splitedPath.length;
        
        for(String key : splitedPath){
            if(section.containsKey(key)){ // Key exist
                Object value = section.get(key);
                if(value == null) return "";
                if(i == 1) return value; // Value is a value or this is the last iteration : return value
                if(!(section.get(key) instanceof Map)) return "";
                section = (HashMap<String, Object>) value; // Continue loop
                i--;
            }else{
                return "";
            }
        }
        Log.w("for loop returned anything");
        return "";
    }
    
    // GET VALUE
    
    public String getString(String path){
        return getString(base, path);
    }
    
    public long getLong(String path){
        return getLong(base, path);
    }
    
    public Long getLongNull(String path){
        return getLongNull(base, path);
    }
    
    public double getDouble(String path){
        return getDouble(base, path);
    }
    
    public Double getDoubleNull(String path){
        return getDoubleNull(base, path);
    }
    public Color getColor(String path){
        return getColor(base, path);
    }
    public Color getColorNull(String path){
        return getColorNull(base, path);
    }
    
    public boolean getBoolean(String path){
        return getBoolean(base, path);
    }
    
    public Boolean getBooleanNull(String path){
        return getBooleanNull(base, path);
    }
    
    public ArrayList<Object> getList(String path){
        return getList(base, path);
    }
    
    public ArrayList<Object> getListNull(String path){
        return getListNull(base, path);
    }
    
    public static String getString(HashMap<String, Object> base, String path){
        return getValue(base, path).toString();
    }
    
    public static long getLong(HashMap<String, Object> base, String path){
        return MathUtils.parseLongOrDefault(getValue(base, path).toString());
    }
    
    public static Long getLongNull(HashMap<String, Object> base, String path){
        return MathUtils.parseLongOrNull(getValue(base, path).toString());
    }
    
    public static double getDouble(HashMap<String, Object> base, String path){
        return MathUtils.parseDoubleOrDefault(getValue(base, path).toString());
    }
    
    public static Double getDoubleNull(HashMap<String, Object> base, String path){
        return MathUtils.parseDoubleOrNull(getValue(base, path).toString());
    }
    
    public static Color getColor(HashMap<String, Object> base, String path){
        try{
            return Color.valueOf(getValue(base, path).toString());
        }catch(NullPointerException | IllegalArgumentException ignored){
            return Color.BLACK;
        }
    }
    public static Color getColorNull(HashMap<String, Object> base, String path){
        try{
            return Color.valueOf(getValue(base, path).toString());
        }catch(NullPointerException | IllegalArgumentException ignored){
            return null;
        }
    }
    
    public static boolean getBoolean(HashMap<String, Object> base, String path){
        return Boolean.parseBoolean(getValue(base, path).toString());
    }
    
    public static Boolean getBooleanNull(HashMap<String, Object> base, String path){
        return StringUtils.getBoolean(getValue(base, path).toString());
    }
    
    public static ArrayList<Object> getList(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof List) return (ArrayList<Object>) value;
        return new ArrayList<>();
    }
    
    public static ArrayList<Object> getListNull(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof List) return (ArrayList<Object>) value;
        return null;
    }
    
    // SET VALUE
    public void set(String path, Object value){
        set(base, path, value);
    }
    
    public static void set(HashMap<String, Object> base, String path, Object value){
        createSectionAndSet(base, path, value);
    }
    
    // GET KEY (SECTION)
    
    public HashMap<String, Object> getSectionSecure(String path){
        createSection(path);
        return getSection(base, path);
    }
    
    public static HashMap<String, Object> getSectionSecure(HashMap<String, Object> base, String path){
        createSection(base, path);
        return getSection(base, path);
    }
    
    public HashMap<String, Object> getSection(String path){
        return getSection(base, path);
    }
    
    public LinkedHashMap<String, Object> getLinkedSection(String path){
        return getLinkedSection(base, path);
    }
    
    public static HashMap<String, Object> getSection(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof Map) return new HashMap<>(((Map<?, ?>) value).entrySet()
                .stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue)));
        return new HashMap<>();
    }
    
    public static LinkedHashMap<String, Object> getLinkedSection(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof Map) return new LinkedHashMap<>(((Map<?, ?>) value).entrySet()
                .stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue)));
        return new LinkedHashMap<>();
    }
    
    public static HashMap<String, Object> getSectionNull(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof Map) return new HashMap<>(((Map<?, ?>) value).entrySet()
                .stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue)));
        return null;
    }
    
    // SET KEY (CREATE SECTION)
    
    public void createSection(String path){
        createSection(base, path);
    }
    
    public static void createSection(HashMap<String, Object> base, String path){
        String[] splitedPath = StringUtils.cleanArray(path.split(Pattern.quote(".")));
        
        HashMap<String, Object> section = base;
        for(String key : splitedPath){
            if(!section.containsKey(key) || !(section.get(key) instanceof Map)){ // section does not exist : Create section
                HashMap<String, Object> value = new HashMap<>();
                section.put(key, value);
                section = value;
            }else{ // use existing section
                section = (HashMap<String, Object>) section.get(key);
            }
        }
    }
    
    public static void createSectionAndSet(HashMap<String, Object> base, String path, Object value){
        String[] splitedPath = StringUtils.cleanArray(path.split(Pattern.quote(".")));
        
        HashMap<String, Object> section = base;
        int i = splitedPath.length;
        for(String key : splitedPath){
            if(i == 1){
                section.put(key, value);
            }else if(!section.containsKey(key) || !(section.get(key) instanceof Map)){ // section does not exist : Create section
                HashMap<String, Object> newSection = new HashMap<>();
                section.put(key, newSection);
                section = newSection;
            }else{ // use existing section
                section = (HashMap<String, Object>) section.get(key);
            }
            i--;
        }
    }
    
    // CHECK EXIST SECTION
    
    public boolean exist(String path){
        return exist(base, path);
    }
    
    public static boolean exist(HashMap<String, Object> base, String path){
        String[] splitedPath = StringUtils.cleanArray(path.split(Pattern.quote(".")));
        
        HashMap<String, Object> section = base;
        for(String key : splitedPath){
            if(!section.containsKey(key) || !(section.get(key) instanceof Map)){ // section does not exist
                return false;
            }
            
            // use existing section to continue loop
            section = (HashMap<String, Object>) section.get(key);
        }
        return true;
    }
    
}
