/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills;

import fr.clementgre.pdf4teachers.components.ScaledSearchableComboBox;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.skillsassessment.SkillsAssessmentWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;

/* INFO DEVOIR

----- Compétences : ----- (liste dans l'application)

ID / Code / Nom

----- RÉSULTATS : ----- (Enregistrer par défaut)

Sigle / Légende / Touche Clavier / Image / ID

Et en plus les résultats par défaut :
    Signe / Légende / Touche clavier (/ Image à partir du Signe / Pas D'ID)
    AB pour "absent" (touche A)
    DI pour "dispensé" (touche D)
    NE pour "non évalué" (touche E)
    NF pour "non fait" (touche F)
    NN pour "non noté" (touche N)
    NR pour "non rendu" (touche R)
    

----- ÉLÈVES -----

NOM / ID


----- INFO DE BASE -----

Classe / Date / Nom de l'eval

*/

/* INFO ÉDITIONS

- Info élément de grille résultat (position, taille).
- Résultats dans chaque compétence :
    - ID /

*/

public class SkillsTab extends SideTab {
    
    
    private final ListProperty<SkillsAssessment> assessments = new SimpleListProperty<>(FXCollections.observableArrayList());
    
    private final VBox pane = new VBox();
    private final HBox optionPane = new HBox();
    private final ListView<String> listView = new ListView<>();
    
    private final ScaledSearchableComboBox<SkillsAssessment> assessmentCombo = new ScaledSearchableComboBox<>();
    
    private final Button settings = setupButton(SVGPathIcons.WRENCH, TR.tr("skillsTab.settings.tooltip"), e -> {
        // TEST
        new SkillsAssessmentWindow(assessmentCombo.getValue()).show();
    
    });
    private final Button deleteAssessment = setupButton(SVGPathIcons.TRASH, TR.tr("skillsTab.link.tooltip"), e -> {
    
    });
    private final Button newAssessment = setupButton(SVGPathIcons.PLUS, TR.tr("skillsTab.export.tooltip"), e -> {
    
    });
    
    public SkillsTab(){
        super("skills", SVGPathIcons.A_CIRCLED, 0, 27, new int[]{1, 1});
        
        SkillsAssessment.setup();
        
        setup();
        setContent(pane);
    }
    
    private void setup(){

        optionPane.setStyle("-fx-padding: 5;");
        optionPane.setSpacing(3);
    
        PaneUtils.setHBoxPosition(assessmentCombo, -1, 30, 0);
    
        assessmentCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(SkillsAssessment assessment){
                if(assessment == null) return null;
                String text = assessment.getName();
                if(!assessment.getClasz().isBlank()) text += " - " + assessment.getClasz();
                if(!assessment.getDate().isBlank()) text += " | " + assessment.getDate();
                return text;
            }
            @Override
            public SkillsAssessment fromString(String assessmentName){ return null; }
        });
        assessmentCombo.itemsProperty().bind(assessments);
        
        settings.disableProperty().bind(assessmentCombo.valueProperty().isNull());
        deleteAssessment.disableProperty().bind(assessmentCombo.valueProperty().isNull());
        
        optionPane.getChildren().addAll(assessmentCombo, settings, deleteAssessment, newAssessment);
        
        // TODO: setup listView for showing skills.
    
        pane.getChildren().addAll(optionPane, listView);
        
    }
    
    private Button setupButton(String iconPath, String tooltip, EventHandler<ActionEvent> onAction){
        Button button = new Button();
        PaneUtils.setHBoxPosition(button, 30, 30, 0);
        button.setCursor(Cursor.HAND);
        button.setGraphic(SVGPathIcons.generateImage(iconPath, "black", 0, 16, 0, ImageUtils.defaultDarkColorAdjust));
        button.setTooltip(PaneUtils.genWrappedToolTip(tooltip));
        button.setOnAction(onAction);
        return button;
    }
    
    
    public ObservableList<SkillsAssessment> getAssessments(){
        return assessments.get();
    }
    public ListProperty<SkillsAssessment> assessmentsProperty(){
        return assessments;
    }
    public void setAssessments(ObservableList<SkillsAssessment> assessments){
        this.assessments.set(assessments);
    }
    public List<Skill> getAllSkills(){
        return assessments.stream().flatMap(a -> a.getSkills().stream()).toList();
    }
}
