/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills.data;

import fr.clementgre.pdf4teachers.datasaving.Config;

import java.util.*;
import java.util.stream.Collectors;

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
        ArrayList<EditionSkill> editionSkills = Config.getList(map, "editionSkills")
                .stream()
                .filter(skillData -> skillData instanceof Map)
                .map(skillData -> EditionSkill.getFromYAML((HashMap<String, Object>) skillData))
                .collect(Collectors.toCollection(ArrayList::new));

        return new Student(
                Config.getString(map, "name"),
                Config.getString(map, "supportModality"),
                Config.getLong(map, "id"),
                editionSkills
        );
    }
    
}
