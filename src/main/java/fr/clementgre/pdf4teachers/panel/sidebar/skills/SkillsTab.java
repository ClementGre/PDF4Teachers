/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills;

import fr.clementgre.pdf4teachers.components.IconButton;
import fr.clementgre.pdf4teachers.components.ScaledSearchableComboBox;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.skillsassessment.SkillsAssessmentWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ConfirmAlert;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

Sigle / Légende / Touche Clavier / Graphique / ID
                                   |-> Caractère (AUTO) / Couleur / Image B64

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


- Résultats dans chaque compétence :
    - ID / Nom
- Info élément de grille résultat.

*/

public class SkillsTab extends SideTab {
    
    
    private final ListProperty<SkillsAssessment> assessments = new SimpleListProperty<>(FXCollections.observableArrayList());
    
    private final VBox pane = new VBox();
    private final HBox optionPane = new HBox();
    private final ListView<String> listView = new ListView<>();
    
    private final ScaledSearchableComboBox<SkillsAssessment> assessmentCombo = new ScaledSearchableComboBox<>(true);
    
    private final Button settings = new IconButton(SVGPathIcons.WRENCH, TR.tr("skillsTab.settings.tooltip"), e -> {
        if(assessmentCombo.getValue() != null) new SkillsAssessmentWindow(assessmentCombo.getValue());
    });
    private final Button deleteAssessment = new IconButton(SVGPathIcons.TRASH, TR.tr("skillsTab.deleteAssessment.tooltip"), e -> {
        if(assessmentCombo.getValue() != null){
            ConfirmAlert alert = new ConfirmAlert(true, TR.tr("skillsTab.deleteAssessment.confirm", assessmentCombo.getValue().getName()));
            if(alert.execute()){
                SkillsAssessment toDelete = assessmentCombo.getValue();
                assessmentCombo.setValue(null);
                assessments.remove(toDelete);
            }
        }
    });
    private final Button newAssessment = new IconButton(SVGPathIcons.PLUS, TR.tr("skillsTab.export.tooltip"), e -> {
        SkillsAssessment assessment = new SkillsAssessment();
        assessments.add(assessment);
        assessmentCombo.setValue(assessment);
        Platform.runLater(() -> new SkillsAssessmentWindow(assessment));
    });
    
    public SkillsTab(){
        super("skills", SVGPathIcons.SKILLS_GRAPH, 26, 1);
        
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
    
    // Update assessment name in the combo box
    public void updateComboBoxSelectedAssessmentName(){
        SkillsAssessment assessment = assessmentCombo.getValue();
        assessmentCombo.getSelectionModel().clearSelection();
        assessmentCombo.getSelectionModel().select(assessment);
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
