/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.skillsassessment;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

public class NotationsListingPane extends Tab {
    
    private final VBox root = new VBox();
    
    private final SkillsAssessmentWindow window;
    public NotationsListingPane(SkillsAssessmentWindow window){
        this.window = window;
    
        setText(TR.tr("skillsSettingsWindow.notationsListing.title"));
        setClosable(false);
        setContent(root);
        root.setStyle("-fx-padding: 15;");
        
        
    }
    
}
