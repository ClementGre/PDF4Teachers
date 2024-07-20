/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.fonts;

import fr.clementgre.pdf4teachers.datasaving.Config;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.HashMap;


public class FontPaths{
    
    private String name;
    private FontPath regular;
    private FontPath italic;
    private FontPath bold;
    private FontPath boldItalic;
    private ArrayList<FontPath> otherStyles = new ArrayList<>();
    
    public FontPaths(String name){
        this.name = name;
    }
    
    public void addPathAuto(String path, FontWeight weight, boolean italic){
        FontPath fontPath = new FontPath(weight, italic, path);
        if(weight == FontWeight.BOLD && italic){
            boldItalic = fontPath;
        }else if(weight == FontWeight.BOLD){
            bold = fontPath;
        }else if(italic && weight == FontWeight.NORMAL){
            this.italic = fontPath;
        }else if(weight == FontWeight.NORMAL){
            regular = fontPath;
        }else{
            otherStyles.add(fontPath);
        }
    }
    
    public FontPath getPath(FontWeight weight, boolean italic){
        if(weight == FontWeight.BOLD && italic){
            if(boldItalic != null) return boldItalic;
            return getPath(FontWeight.NORMAL, false);
        }
        if(weight == FontWeight.BOLD){
            if(bold != null) return bold;
            return getPath(FontWeight.NORMAL, false);
        }
        if(italic && weight == FontWeight.NORMAL){
            if(this.italic != null) return this.italic;
            return getPath(FontWeight.NORMAL, false);
        }
        if(weight == FontWeight.NORMAL){
            return regular;
        }
        return otherStyles.stream()
                .filter(fontPath -> fontPath.isItalic() == italic && fontPath.getWeight().equals(weight))
                .findFirst()
                .orElse(null);
    }
    
    public FontPath getOtherStyles(FontWeight weight, boolean italic){
        return otherStyles.stream()
                .filter(fontPath -> fontPath.isItalic() == italic && fontPath.getWeight().equals(weight))
                .findFirst()
                .orElse(null);
    }
    public void addOtherStyles(FontPath path){
        otherStyles.add(path);
    }
    
    
    public HashMap<String, Object> serialize(){
        HashMap<String, Object> map = new HashMap<>();
        if(regular != null) map.put("regular", regular.serialize());
        if(boldItalic != null) map.put("boldItalic", boldItalic.serialize());
        if(bold != null) map.put("bold", bold.serialize());
        if(italic != null) map.put("italic", italic.serialize());
        return map;
    }
    public void deSerialize(HashMap<String, Object> map){
        
        if(map.containsKey("regular")) setRegular(FontPath.deSerialize(Config.getSection(map, "regular")));
        if(map.containsKey("boldItalic")) setBoldItalic(FontPath.deSerialize(Config.getSection(map, "boldItalic")));
        if(map.containsKey("bold")) setBold(FontPath.deSerialize(Config.getSection(map, "bold")));
        if(map.containsKey("italic")) setItalic(FontPath.deSerialize(Config.getSection(map, "italic")));
        
    }
    
    
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public FontPath getRegular(){
        return regular;
    }
    public void setRegular(FontPath regular){
        this.regular = regular;
    }
    public FontPath getItalic(){
        return italic;
    }
    public void setItalic(FontPath italic){
        this.italic = italic;
    }
    public FontPath getBold(){
        return bold;
    }
    public void setBold(FontPath bold){
        this.bold = bold;
    }
    public FontPath getBoldItalic(){
        return boldItalic;
    }
    public void setBoldItalic(FontPath boldItalic){
        this.boldItalic = boldItalic;
    }
    public ArrayList<FontPath> getOtherStyles(){
        return otherStyles;
    }
    public void setOtherStyles(ArrayList<FontPath> otherStyles){
        this.otherStyles = otherStyles;
    }
    
    
    
    
    public static class FontPath{
        private FontWeight weight;
        private boolean italic;
        private String path;
    
        public FontPath(FontWeight weight, boolean italic, String path){
            this.weight = weight;
            this.italic = italic;
            this.path = path;
        }
    
        public HashMap<String, Object> serialize(){
            HashMap<String, Object> map = new HashMap<>();
            map.put("path", path);
            map.put("weight", weight.toString());
            map.put("italic", italic);
            return map;
        }
        public static FontPath deSerialize(HashMap<String, Object> data){
            String path = Config.getString(data, "path");
            if(path == null || path.isBlank()) return null;
            
            FontWeight weight = FontWeight.findByName(Config.getString(data, "weight"));
            if(weight == null) weight = FontWeight.NORMAL;
            
            boolean italic = Config.getBoolean(data, "italic");
            
            return new FontPath(weight, italic, path);
        }
        
        public FontWeight getWeight(){
            return weight;
        }
        public void setWeight(FontWeight weight){
            this.weight = weight;
        }
        public boolean isItalic(){
            return italic;
        }
        public void setItalic(boolean italic){
            this.italic = italic;
        }
        public String getPath(){
            return path;
        }
        public void setPath(String path){
            this.path = path;
        }
    }
}
