package fr.themsou.yaml;

import fr.themsou.utils.Builders;
import fr.themsou.utils.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FileConfiguration {

    public HashMap<String, Object> base = new HashMap<>();

    private Yaml yaml;
    private File file;
    public FileConfiguration(File file){
        this.file = file;
        yaml = new Yaml(new SafeConstructor());
    }

    public void load() throws FileNotFoundException {
        InputStream input = new FileInputStream(file);
        base = (HashMap<String, Object>) yaml.load(input);
    }
    public void save() throws FileNotFoundException {
        yaml.dump(base, new OutputStreamWriter(new FileOutputStream(file)));
    }

    // GET VALUE

    public String getString(String path){
        return getValue(base, path).toString();
    }
    public int getInt(String path){
        return StringUtils.getAlwaysInt(getValue(base, path).toString());
    }
    public double getDouble(String path){
        return StringUtils.getAlwaysDouble(getValue(base, path).toString());
    }
    public boolean getBoolean(String path){
        return Boolean.parseBoolean(getValue(base, path).toString());
    }
    public List<Object> getList(String path){
        return getList(base, path);
    }

    public String getString(HashMap<String, Object> base, String path){
        return getValue(base, path).toString();
    }
    public int getInt(HashMap<String, Object> base, String path){
        return StringUtils.getAlwaysInt(getValue(base, path).toString());
    }
    public double getDouble(HashMap<String, Object> base, String path){
        return StringUtils.getAlwaysDouble(getValue(base, path).toString());
    }
    public boolean getBoolean(HashMap<String, Object> base, String path){
        return Boolean.parseBoolean(getValue(base, path).toString());
    }
    public List<Object> getList(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof List) return (List<Object>) value;
        return new ArrayList<>();
    }

    public Object getValue(HashMap<String, Object> base, String path){

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
                System.out.println("WARNING: section " + key + " does not exist in " + section); return "";
            }
        }
        System.out.println("WARNING: for loop return anything"); return "";
    }

    // GET KEY (SECTION)

    public HashMap<String, Object> getSectionSecure(String path){
        createSection(path);
        return getSection(base, path);
    }
    public HashMap<String, Object> getSectionSecure(HashMap<String, Object> base, String path){
        createSection(base, path);
        return getSection(base, path);
    }
    public HashMap<String, Object> getSection(String path){
        return getSection(base, path);
    }
    public HashMap<String, Object> getSection(HashMap<String, Object> base, String path){
        Object value = getValue(base, path);
        if(value instanceof Map) return (HashMap<String, Object>) value;
        return new HashMap<>();
    }

    // CREATE SECTION

    public void createSection(String path){
        createSection(base, path);
    }
    public void createSection(HashMap<String, Object> base, String path){
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
    public boolean exist(HashMap<String, Object> base, String path){
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
