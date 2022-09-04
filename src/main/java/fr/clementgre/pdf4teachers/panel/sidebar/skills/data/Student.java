/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills.data;

import fr.clementgre.pdf4teachers.datasaving.Config;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;

import java.util.*;

public record Student(String name, String supportModality, long id,
                      ArrayList<EditionSkill> editionSkills /* Temporary, must be automatically added to the associated edition */){
    
    public LinkedHashMap<String, Object> toYAML(){
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("supportModality", supportModality);
        map.put("id", id);
        map.put("editionSkills", editionSkills.stream().filter(s -> s.getNotationId() != 0).map(EditionSkill::toYAML).toList());
        return map;
    }
    
    public static Student loadFromConfig(HashMap<String, Object> map){
        ArrayList<EditionSkill> editionSkills = new ArrayList<>();
        for(Object skillData : Config.getList(map, "editionSkills")){
            if(skillData instanceof Map) editionSkills.add(EditionSkill.getFromYAML((HashMap<String, Object>) skillData));
        }
        
        return new Student(
                Config.getString(map, "name"),
                Config.getString(map, "supportModality"),
                Config.getLong(map, "id"),
                editionSkills
        );
    }
    
}
