/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import javafx.scene.control.ListCell;

public class SkillListCell extends ListCell<Skill> {
    
    @Override
    public void updateItem(Skill skill, boolean empty){
        super.updateItem(skill, empty);
        
        if(empty){
            setGraphic(null);
            setTooltip(null);
            setContextMenu(null);
            setOnMouseClicked(null);
            setText(null);
            
        }else{
            setText(skill.getAcronym());
            
        }
    }
    
}
