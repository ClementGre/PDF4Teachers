/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills.data;

import fr.clementgre.pdf4teachers.datasaving.Config;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class Skill {
    private long id;
    private final StringProperty acronym = new SimpleStringProperty("");
    private final StringProperty name = new SimpleStringProperty("");
    
    public Skill(String acronym, String name){
        this(new Random().nextLong(9999999L), acronym, name);
    }
    public Skill(long id, String acronym, String name){
        this.id = id;
        this.acronym.set(acronym);
        this.name.set(name);
    }
    public static Skill loadFromConfig(HashMap<String, Object> map){
        long id = Config.getLong(map, "id");
        String acronym = Config.getString(map, "acronym");
        String name = Config.getString(map, "name");
        return new Skill(id, acronym, name);
    }
    
    
    public LinkedHashMap<String, Object> toYAML(){
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("acronym", acronym.get());
        map.put("name", name.get());
        map.put("id", id);
        return map;
    }
    
    public EditionSkill getMatchingEditionSkill(List<EditionSkill> editionSkills){
        for(EditionSkill editionSkill : editionSkills){
            if(editionSkill.getSkillId() == id) return editionSkill;
        }
        return null;
    }
    
    
    
    public long getId(){
        return id;
    }
    public void setId(long id){
        this.id = id;
    }
    public String getAcronym(){
        return acronym.get();
    }
    public StringProperty acronymProperty(){
        return acronym;
    }
    public void setAcronym(String acronym){
        this.acronym.set(acronym);
    }
    public String getName(){
        return name.get();
    }
    public StringProperty nameProperty(){
        return name;
    }
    public void setName(String name){
        this.name.set(name);
    }
}
