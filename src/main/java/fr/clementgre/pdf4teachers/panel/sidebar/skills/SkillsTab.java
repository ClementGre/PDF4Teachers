/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills;

import fr.clementgre.pdf4teachers.components.IconButton;
import fr.clementgre.pdf4teachers.components.ScaledSearchableComboBox;
import fr.clementgre.pdf4teachers.document.editions.elements.SkillTableElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.skillsassessment.SkillsAssessmentWindow;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Student;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ConfirmAlert;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    
    private SkillTableElement skillTableElement;
    
    private final ListProperty<SkillsAssessment> assessments = new SimpleListProperty<>(FXCollections.observableArrayList());
    
    private final VBox pane = new VBox();
    private final HBox optionPane = new HBox();
    private final ScaledSearchableComboBox<SkillsAssessment> assessmentCombo = new ScaledSearchableComboBox<>(true);
    private final HBox studentPane = new HBox();
    private final ScaledSearchableComboBox<Student> studentCombo = new ScaledSearchableComboBox<>(true);
    
    private final ListView<Skill> listView = new ListView<>();
    
    private final Button settings = new IconButton(SVGPathIcons.WRENCH, TR.tr("skillsTab.settings.tooltip"), e -> {
        if(getCurrentAssessment() != null) new SkillsAssessmentWindow(getCurrentAssessment());
    });
    private final Button deleteAssessment = new IconButton(SVGPathIcons.TRASH, TR.tr("skillsTab.deleteAssessment.tooltip"), e -> {
        if(getCurrentAssessment() != null){
            ConfirmAlert alert = new ConfirmAlert(true, TR.tr("skillsTab.deleteAssessment.confirm", getCurrentAssessment().getName()));
            if(alert.execute()){
                SkillsAssessment toDelete = getCurrentAssessment();
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
    
        optionPane.disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        pane.getChildren().setAll(optionPane, listView);
        
        /* OPTION PANE */
        
        optionPane.setStyle("-fx-padding: 5;");
        optionPane.setSpacing(3);
    
        PaneUtils.setHBoxPosition(assessmentCombo, -1, 30, 0);
        assessmentCombo.setConverter(new StringConverter<>() {
            @Override public String toString(SkillsAssessment assessment){
                if(assessment == null) return null;
                String text = assessment.getName();
                if(!assessment.getClasz().isBlank()) text += " - " + assessment.getClasz();
                if(!assessment.getDate().isBlank()) text += " | " + assessment.getDate();
                return text;
            }
            @Override public SkillsAssessment fromString(String assessmentName){ return null; }
        });
        assessmentCombo.itemsProperty().bind(assessments);
        
        settings.disableProperty().bind(assessmentCombo.valueProperty().isNull());
        deleteAssessment.disableProperty().bind(assessmentCombo.valueProperty().isNull());
        
        optionPane.getChildren().addAll(assessmentCombo, settings, deleteAssessment, newAssessment);
    
        /* STUDENT PANE */
        
        studentPane.setStyle("-fx-padding: 5;");
        studentPane.setSpacing(3);
    
        PaneUtils.setVBoxPosition(studentCombo, -1, 30, 0);
        studentCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Student s){ return s.name(); }
            @Override public Student fromString(String name){ return null; }
        });
        studentCombo.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            if(getCurrentAssessment() != null) return FXCollections.observableArrayList(getCurrentAssessment().getStudents());
            return FXCollections.observableArrayList();
        }, assessmentCombo.valueProperty()));
    
        assessmentCombo.valueProperty().addListener(o -> {
            if(getCurrentAssessment() != null && getCurrentAssessment().getStudents().size() != 0){
                pane.getChildren().setAll(optionPane, studentPane, listView);
            }else pane.getChildren().setAll(optionPane, listView);
        });
        
        
        /* LIST VIEW */
    
        listView.setBorder(null);
        listView.setPadding(new Insets(0));
        VBox.setVgrow(listView, Priority.SOMETIMES);
    
        listView.setCellFactory(param -> new SkillListCell());
        listView.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            if(getCurrentAssessment() != null) return FXCollections.observableArrayList(getCurrentAssessment().getSkills());
            return FXCollections.observableArrayList();
        }, assessmentCombo.valueProperty()));
        
        
        /* LINK WITH SkillTableElement */
    
        // Sync assessment id
        assessmentCombo.valueProperty().addListener(o -> {
            if(skillTableElement != null){
                if(getCurrentAssessment() != null) skillTableElement.setAssessmentId(getCurrentAssessment().getId());
                else skillTableElement.setAssessmentId(0);
            }
        });
        // Sync student id
        studentCombo.valueProperty().addListener(o -> {
            if(skillTableElement != null){
                if(getCurrentAssessment() != null && getCurrentStudent() != null) skillTableElement.setStudentId(getCurrentStudent().id());
                else skillTableElement.setStudentId(0);
            }
        });
        
    }
    
    public void registerSkillTableElement(SkillTableElement skillTableElement){
        this.skillTableElement = skillTableElement;
        
        for(SkillsAssessment assessment : assessments){
            if(assessment.getId() == skillTableElement.getAssessmentId()){
                assessmentCombo.setValue(assessment);
                return;
            }
        }
        
        if(getCurrentAssessment() == null) return;
        for(Student student : getCurrentAssessment().getStudents()){
            if(student.id() == skillTableElement.getStudentId()){
                studentCombo.setValue(student);
                return;
            }
        }
        
    }
    
    
    // Update assessment name in the combo box
    public void updateComboBoxSelectedAssessmentName(){
        SkillsAssessment assessment = getCurrentAssessment();
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
    public SkillsAssessment getCurrentAssessment(){
        return assessmentCombo.getValue();
    }
    public Student getCurrentStudent(){
        return studentCombo.getValue();
    }
}
