/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills.data;

import fr.clementgre.pdf4teachers.datasaving.Config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

public class Skill {
    private long id;
    private String acronym;
    private String name;
    
    public Skill(String acronym, String name){
        this(new Random().nextLong(9999999L), acronym, name);
    }
    public Skill(long id, String acronym, String name){
        this.id = id;
        this.acronym = acronym;
        this.name = name;
    }
    public static Skill loadFromConfig(HashMap<String, Object> map){
        long id = Config.getLong(map, "id");
        String acronym = Config.getString(map, "acronym");
        String name = Config.getString(map, "name");
        return new Skill(id, acronym, name);
    }
    
    
    public LinkedHashMap<String, Object> toYAML(){
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("acronym", acronym);
        map.put("name", name);
        map.put("id", id);
        return map;
    }
    
    
    public long getId(){
        return id;
    }
    public void setId(long id){
        this.id = id;
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
}
