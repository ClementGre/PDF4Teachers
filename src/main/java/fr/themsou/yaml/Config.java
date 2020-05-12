package fr.themsou.yaml;

import fr.themsou.utils.Builders;
import fr.themsou.utils.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Config {

    public HashMap<String, Object> base = new HashMap<>();

    private Yaml yaml;
    private File file;
    public Config(File file){
        this.file = file;
        yaml = new Yaml(new SafeConstructor());
    }

    public void load() throws IOException {
        InputStream input = new FileInputStream(file);
        base = (HashMap<String, Object>) yaml.load(input);
        input.close();

        if(base == null) base = new HashMap<>();
    }
    public void save() throws IOException {
        Writer output = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        yaml.dump(base, output);
        output.close();
    }

    // GET VALUE

    public String getString(String path){
        return getValue(base, path).toString();
    }
    public long getLong(String path){
        return StringUtils.getAlwaysLong(getValue(base, path).toString());
    }
    public double getDouble(String path){
        return StringUtils.getAlwaysDouble(getValue(base, path).toString());
    }
    public boolean getBoolean(String path){
        return Boolean.parseBoolean(getValue(base, path).toString());
    }
    public ArrayList<Object> getList(String path){
        return getList(base, path);
    }

    public static String getString(HashMap<String, Object> base, String path){
        return getValue(base, path).toString();
    }
    public static long getLong(HashMap<String, Object> base, String path){
        return StringUtils.getAlwaysLong(getValue(base, path).toString());
    }
    public static double getDouble(HashMap<String, Object> base, String path){
        return StringUtils.getAlwaysDouble(getValue(base, path).toString());
    }
    public static boolean getBoolean(HashMap<String, Object> base, String path){
        return Boolean.parseBoolean(getValue(base, path).toString());
    }
    public static ArrayList<Object> getList(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof List) return (ArrayList<Object>) value;
        return new ArrayList<>();
    }

    public static ArrayList<Object> castList(Object list){
        if(list instanceof List) return (ArrayList<Object>) list;
        return new ArrayList<>();
    }
    public static HashMap<String, Object> castSection(Object list){
        if(list instanceof Map) return (HashMap<String, Object>) list;
        return new HashMap<>();
    }

    public static Object getValue(HashMap<String, Object> base, String path){

        String[] splitedPath = Builders.cleanArray(path.split(Pattern.quote(".")));
        HashMap<String, Object> section = base;
        int i = splitedPath.length;

        for(String key : splitedPath){
            if(section.containsKey(key)){ // Key exist
                Object value = section.get(key);
                if(value == null) return "";
                else if(!(section.get(key) instanceof Map) || i == 1) return value; // Value is a value or this is the last iteration : return value
                else section = (HashMap<String, Object>) value; // Continue loop
                i--;
            }else{
                return "";
            }
        }
        System.err.println("WARNING: for loop return anything"); return "";
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
    public static HashMap<String, Object> getSection(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof Map) return (HashMap<String, Object>) value;
        return new HashMap<>();
    }

    // CREATE SECTION

    public void createSection(String path){
        createSection(base, path);
    }
    public static void createSection(HashMap<String, Object> base, String path){
        String[] splitedPath = Builders.cleanArray(path.split(Pattern.quote(".")));

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

    // CHECK EXIST SECTION

    public boolean exist(String path){
        return exist(base, path);
    }
    public static boolean exist(HashMap<String, Object> base, String path){
        String[] splitedPath = Builders.cleanArray(path.split(Pattern.quote(".")));

        HashMap<String, Object> section = base;
        for(String key : splitedPath){
            if(!section.containsKey(key) || !(section.get(key) instanceof Map)){ // section does not exist
                return false;
            }else{ // use existing section to continue loop
                section = (HashMap<String, Object>) section.get(key);
            }
        }
        return true;
    }

}
