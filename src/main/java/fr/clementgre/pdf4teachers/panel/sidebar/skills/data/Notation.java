/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills.data;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

public class Notation {

// Sigle / Légende / Touche Clavier / Graphique / ID
//                                    |-> Caractère (AUTO) / Couleur / Image B64
    
    @NotNull
    private String acronym;
    @NotNull
    private String name;
    @NotNull
    private String keyboardChar;
    @NotNull
    private String data; // Can be base64 icon or color string or just a letter
    private long id;
    
    // SkillsAssessment is only used to generate a unique ID
    public Notation(@NotNull SkillsAssessment assessment){
        this("", "", "", "", getNewNotationUniqueId(assessment));
    }
    // SkillsAssessment is only used to generate a unique ID
    public Notation(@NotNull SkillsAssessment assessment, @NotNull String acronym, @NotNull String name, @NotNull String keyboardChar){
        this(acronym, name, keyboardChar, "", getNewNotationUniqueId(assessment));
    }
    // SkillsAssessment is only used to generate a unique ID
    public Notation(@NotNull SkillsAssessment assessment, @NotNull String acronym, @NotNull String name, @NotNull String keyboardChar, @NotNull String data){
        this(acronym, name, keyboardChar, data, getNewNotationUniqueId(assessment));
    }
    public Notation(@NotNull String acronym, @NotNull String name, @NotNull String keyboardChar, @NotNull String data, long id){
        this.acronym = acronym;
        this.name = name;
        this.keyboardChar = keyboardChar;
        this.data = data;
        this.id = id;
    }
    public static long getNewNotationUniqueId(SkillsAssessment assessment){
        // Negative ids are reserved for default not editable notations.
        // 0 id is reserved for not filled notations.
        long id = Math.abs(new Random().nextLong());
        while(id == 0 || getById(assessment, id) != null) id = new Random().nextLong();
        return id;
    }
    public static Notation getById(SkillsAssessment assessment, long id){
        return assessment.getNotations().stream().filter(s -> s.getId() == id).findAny().orElse(null);
    }
    
    
    public static Notation loadFromConfig(HashMap<String, Object> map){
        String acronym = Config.getString(map, "acronym");
        String name = Config.getString(map, "name");
        String keyboardChar = Config.getString(map, "keyboardChar");
        String data = Config.getString(map, "data");
        long id = Config.getLong(map, "id");
        return new Notation(acronym, name, keyboardChar, data, id);
    }
    public LinkedHashMap<String, Object> toYAML(){
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("acronym", acronym);
        map.put("name", name);
        map.put("keyboardChar", keyboardChar);
        map.put("data", data);
        map.put("id", id);
        return map;
    }
    
    public enum NotationType{
        CHAR, COLOR, ICON;
        
        public String getText(){
            if(this == CHAR) return TR.tr("skillsSettingsWindow.notationMode.chars");
            else if(this == COLOR) return TR.tr("skillsSettingsWindow.notationMode.colors");
            else if(this == ICON) return TR.tr("skillsSettingsWindow.notationMode.icons");
            else return "";
        }
    }
    
    @Override
    public Notation clone(){
        return new Notation(acronym, name, keyboardChar, data, id);
    }
    
    @Override
    public String toString(){
        return "Notation{" +
                "name='" + name + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        
        Notation notation = (Notation) o;
        
        if(id != notation.id) return false;
        if(!acronym.equals(notation.acronym)) return false;
        if(!name.equals(notation.name)) return false;
        if(!keyboardChar.equals(notation.keyboardChar)) return false;
        return data.equals(notation.data);
    }
    @Override
    public int hashCode(){
        int result = acronym.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + keyboardChar.hashCode();
        result = 31 * result + data.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }
    
    public String getAcronym(){
        return acronym;
    }
    public void setAcronym(String acronym){
        this.acronym = acronym;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getKeyboardChar(){
        return keyboardChar;
    }
    public void setKeyboardChar(String keyboardChar){
        this.keyboardChar = keyboardChar;
    }
    public String getData(){
        return data;
    }
    public void setData(String data){
        this.data = data;
    }
    public long getId(){
        return id;
    }
    public void setId(long id){
        this.id = id;
    }
}
