/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills;

import fr.clementgre.pdf4teachers.interfaces.windows.AlternativeWindow;
import javafx.scene.layout.VBox;

public class SkillsAssessmentWindow extends AlternativeWindow<VBox> {
    
    public SkillsAssessmentWindow(){
        super(new VBox(), StageWidth.NORMAL, "title", "header", "subheader");
    }
    @Override
    public void setupSubClass(){
    
    }
    @Override
    public void afterShown(){
    
    }
}
