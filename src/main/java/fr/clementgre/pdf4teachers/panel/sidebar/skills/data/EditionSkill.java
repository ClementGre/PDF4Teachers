/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills.data;

import fr.clementgre.pdf4teachers.datasaving.Config;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class EditionSkill{
    
    private long skillId;
    private long notationId;
    public EditionSkill(long skillId, long notationId){
        this.skillId = skillId;
        this.notationId = notationId;
    }
    
    
    public LinkedHashMap<String, Object> toYAML(){
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("skillId", skillId);
        map.put("notationId", notationId);
        return map;
    }
    
    public static EditionSkill getFromYAML(HashMap<String, Object> map){
        return new EditionSkill(
                Config.getLong(map, "skillId"),
                Config.getLong(map, "notationId")
        );
    }
    
    public Skill getMatchingSkill(SkillsAssessment assessment){
        return assessment.getSkills()
                .stream()
                .filter(skill -> skill.getId() == skillId)
                .findFirst()
                .orElse(null);
    }
    
    public Notation getMatchingNotation(SkillsAssessment assessment){
        return assessment.getNotationsWithDefaults()
                .stream()
                .filter(notation -> notation.getId() == notationId)
                .findFirst()
                .orElse(null);
    }
    
    
    public long getSkillId(){
        return skillId;
    }
    public void setSkillId(long skillId){
        this.skillId = skillId;
    }
    public long getNotationId(){
        return notationId;
    }
    public void setNotationId(long notationId){
        this.notationId = notationId;
    }
}
