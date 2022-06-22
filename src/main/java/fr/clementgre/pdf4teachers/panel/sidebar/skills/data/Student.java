/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills.data;

import fr.clementgre.pdf4teachers.datasaving.Config;

import java.util.HashMap;
import java.util.LinkedHashMap;

public record Student(String name, long id){
    
    public LinkedHashMap<String, Object> toYAML(){
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("name", name);
        return map;
    }
    
    public static Student loadFromConfig(HashMap<String, Object> map){
        return new Student(
                Config.getString(map, "name"),
                Config.getLong(map, "id")
        );
    }
    
}
