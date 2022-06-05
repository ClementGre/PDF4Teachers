/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.datasaving.simpleconfigs;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import javafx.collections.FXCollections;

import java.util.HashMap;
import java.util.stream.Collectors;

public class SkillsAssessmentData extends SimpleConfig{

    
    public SkillsAssessmentData(){
        super("skillsassessments");
    }
    
    @Override
    protected void manageLoadedData(Config config){
        
        MainWindow.skillsTab.setAssessments(
                FXCollections.observableArrayList(config.getList("assessments").stream()
                        .filter(d -> d instanceof HashMap)
                        .map(a -> SkillsAssessment.loadFromConfig((HashMap) a)).toList())
        );
        
        SkillsAssessment.setDefaultNotations(SkillsAssessment.getNotationsFromConfig(config.getList("userDefaultNotations")));
        SkillsAssessment.setDefaultNotationsType(Notation.NotationType.valueOf(config.getString("userDefaultNotationType")));
    }
    @Override
    protected void unableToLoadConfig(){
    
    }
    @Override
    protected void addDataToConfig(Config config){
    
        config.set("assessments", MainWindow.skillsTab.getAssessments().stream().map(SkillsAssessment::toYAML).collect(Collectors.toList()));
        
        config.set("userDefaultNotations", SkillsAssessment.notationsToYAML(SkillsAssessment.getDefaultNotations()));
        config.set("userDefaultNotationType", SkillsAssessment.getDefaultNotationsType().name());
    }
}
