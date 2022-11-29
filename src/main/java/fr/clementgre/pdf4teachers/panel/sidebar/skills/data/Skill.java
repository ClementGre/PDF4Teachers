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
    
    public Skill(String acronym, String name, SkillsAssessment assessment){
        this(getNewNotationUniqueId(assessment), acronym, name);
    }
    public Skill(long id, String acronym, String name){
        this.id = id;
        this.acronym.set(acronym);
        this.name.set(name);
    }
    public static long getNewNotationUniqueId(SkillsAssessment assessment){
        // Negative ids are reserved for default not editable notations.
        // 0 id is reserved for not filled notations.
        long id = Math.abs(new Random().nextLong());
        while(id == 0 || getById(assessment, id) != null) {
            id = new Random().nextLong();
        }
        return id;
    }
    public static Skill getById(SkillsAssessment assessment, long id){
        return assessment.getSkills().stream().filter(s -> s.getId() == id).findAny().orElse(null);
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
        return editionSkills.stream()
                .filter(editionSkill -> editionSkill.getSkillId() == id)
                .findFirst()
                .orElse(null);
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
