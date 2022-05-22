/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills.data;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.scene.paint.Color;

import java.util.*;

public class SkillsAssessment {
    
    public static void setup(){
        loadOtherNotations();
    }
    
//    DEFAULT & OTHER NOTATIONS
    
    private static final ArrayList<Notation> otherNotations = new ArrayList<>();
    private static void loadOtherNotations(){
        otherNotations.addAll(Arrays.asList(
                new Notation("AB", TR.tr("skills.notation.missing"), "A"), // absent
                new Notation("DI", TR.tr("skills.notation.exempted"), "D"), // dispensé
                new Notation("NE", TR.tr("skills.notation.notAssessed"), "E"), // non évalué
                new Notation("NF", TR.tr("skills.notation.notMade"), "F"), // non fait
                new Notation("NN", TR.tr("skills.notation.notGraded"), "N"), // non noté
                new Notation("NR", TR.tr("skills.notation.notReturned"), "R") // non rendu
        ));
    }
    
    
    private static Notation.NotationType userDefaultNotationType = Notation.NotationType.COLOR;
    private static ArrayList<Notation> userDefaultNotations = new ArrayList<>();
    
    private static ArrayList<Notation> getGlobalDefaultNotations(){
        return new ArrayList<>(Arrays.asList(
                new Notation("1", TR.tr("skills.notation.veryInsufficient"), "1", Color.DARKRED.toString()),
                new Notation("2", TR.tr("skills.notation.insufficient"), "2", Color.YELLOW.toString()),
                new Notation("3", TR.tr("skills.notation.good"), "3", Color.LIGHTGREEN.toString()),
                new Notation("4", TR.tr("skills.notation.veryGood"), "4", Color.DARKGREEN.toString())
        ));
    }
    public static ArrayList<Notation> getDefaultNotations(){
        if(userDefaultNotations.size() == 0) return getGlobalDefaultNotations();
        return userDefaultNotations;
    }
    public static void setDefaultNotations(ArrayList<Notation> notations){
        userDefaultNotations = notations;
    }
    public static void setDefaultNotationsType(Notation.NotationType userDefaultNotationType){
        SkillsAssessment.userDefaultNotationType = userDefaultNotationType;
    }
    public static Notation.NotationType getDefaultNotationsType(){
        return userDefaultNotationType;
    }
    
    
//    LOADING FROM CONFIG
    
    public static SkillsAssessment loadFromConfig(HashMap<String, Object> map){
        return new SkillsAssessment(
                Config.getString(map, "name"),
                Config.getString(map, "date"),
                Config.getString(map, "class"),
                Notation.NotationType.valueOf(Config.getString(map, "notationType")),
                getSkillsFromConfig(Config.getList(map, "skills")),
                getNotationsFromConfig(Config.getList(map, "notations")),
                Config.getLong(map, "id")
        );
    }
    private static ArrayList<Skill> getSkillsFromConfig(List<Object> list){
        return new ArrayList<>(list.stream().filter(d -> d instanceof HashMap).map(s -> Skill.loadFromConfig((HashMap) s)).toList());
    }
    public static ArrayList<Notation> getNotationsFromConfig(List<Object> list){
        return new ArrayList<>(list.stream().filter(d -> d instanceof HashMap).map(n -> Notation.loadFromConfig((HashMap) n)).toList());
    }
    
//    WRITING FROM CONFIG
    
    public static List<LinkedHashMap<String, Object>> notationsToYAML(ArrayList<Notation> notations){
        return notations.stream().map(Notation::toYAML).toList();
    }
    public static List<LinkedHashMap<String, Object>> skillsToYAML(ArrayList<Skill> skill){
        return skill.stream().map(Skill::toYAML).toList();
    }
    
//    OTHER STATIC
    
    public static SkillsAssessment getById(long id){
        return MainWindow.skillsTab.getAssessments().stream().filter(s -> s.getId() == id).findAny().orElse(null);
    }
    public static long getNewSkillsAssessmentUniqueId(){
        long id = new Random().nextLong();
        while(getById(id) != null) id = new Random().nextLong();
        return id;
    }
    
//    OBJECT

    private final long id;
    private String name;
    private String date;
    private String clasz;
    private final Notation.NotationType notationType;
    private final ArrayList<Skill> skills;
    private final ArrayList<Notation> notations;
    
    
    public SkillsAssessment(String name){
        this(name, "", "");
    }
    public SkillsAssessment(String name, String date, String clasz){
        this(name, date, clasz, getDefaultNotationsType(), new ArrayList<>(), getDefaultNotations(), getNewSkillsAssessmentUniqueId());
    }
    public SkillsAssessment(String name, String date, String clasz, Notation.NotationType notationType, ArrayList<Skill> skills, ArrayList<Notation> notations, long id){
        this.name = name;
        this.date = date;
        this.clasz = clasz;
        this.notationType = notationType;
        this.skills = skills;
        this.notations = notations;
        this.id = id;
    }
    
    public LinkedHashMap<String, Object> toYAML(){
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("date", date);
        map.put("class", clasz);
        map.put("notationType", notationType.name());
        map.put("skills", skillsToYAML(skills));
        map.put("notations", notationsToYAML(notations));
        return map;
    }
    
    
    public Notation.NotationType getNotationType(){
        return notationType;
    }
    public ArrayList<Skill> getSkills(){
        return skills;
    }
    public ArrayList<Notation> getNotations(){
        return notations;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getDate(){
        return date;
    }
    public void setDate(String date){
        this.date = date;
    }
    public String getClasz(){
        return clasz;
    }
    public void setClasz(String clasz){
        this.clasz = clasz;
    }
    public long getId(){
        return id;
    }
}
