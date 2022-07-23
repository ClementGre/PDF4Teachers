/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.undoEngine;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.EditionSkill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;

public class SkillUndoAction extends UndoAction{
    
    private final long assessmentId;
    private final long skillId;
    private long oldNotationId;
    
    public SkillUndoAction(UType undoType, long assessmentId, long skillId, long oldNotationId){
        super(undoType);
        this.assessmentId = assessmentId;
        this.skillId = skillId;
        this.oldNotationId = oldNotationId;
    }
    
    @Override
    public boolean undoAndInvert(){
        
        // Changing the assessment linked to the document does not count as an UndoAction,
        // but when a notation change is undone, it will automatically switch on the right assessment.
        if(MainWindow.skillsTab.getCurrentAssessment().getId() != assessmentId){
            SkillsAssessment assessment = MainWindow.skillsTab.getAssessments().stream().filter(a -> a.getId() == assessmentId).findFirst().orElse(null);
            if(assessment == null) return false;
    
            MainWindow.skillsTab.selectAssessment(assessment);
        }
            
        // Fetching/Creating the matching EditionSkill object
        EditionSkill editionSkill = MainWindow.skillsTab.getSkillTableElement().getEditionSkills().stream().filter(s -> s.getSkillId() == skillId).findFirst().orElseGet(() -> {
            EditionSkill newEditionSkill = new EditionSkill(skillId, 0);
            MainWindow.skillsTab.getSkillTableElement().getEditionSkills().add(newEditionSkill);
            return newEditionSkill;
        });
        
        // Updating & inverting
        long oldNotationId = editionSkill.getNotationId();
        editionSkill.setNotationId(this.oldNotationId);
        this.oldNotationId = oldNotationId;
    
        MainWindow.skillsTab.getSkillTableElement().updateSkillsNotation();
        MainWindow.skillsTab.refreshListView();
        Edition.setUnsave("SkillListCell ComboBox notation changed");
        
        
        return true;
    }
    
    @Override
    public String toString(){
        return TR.tr("actions.editSkill");
    }
}
