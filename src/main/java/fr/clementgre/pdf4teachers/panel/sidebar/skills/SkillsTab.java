/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.IconButton;
import fr.clementgre.pdf4teachers.components.ScaledSearchableComboBox;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.SkillTableElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;

import java.util.ArrayList;
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

    private final ObjectProperty<SkillTableElement> skillTableElement = new SimpleObjectProperty<>(null);

    private final ListProperty<SkillsAssessment> assessments = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final VBox pane = new VBox();
    private final HBox optionPane = new HBox();
    private final ScaledSearchableComboBox<SkillsAssessment> assessmentCombo = new ScaledSearchableComboBox<>(true);
    private final HBox studentPane = new HBox();
    private final ScaledSearchableComboBox<Student> studentCombo = new ScaledSearchableComboBox<>(true);
    private final Label supportModality = new Label();

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
    private final Button newAssessment = new IconButton(SVGPathIcons.PLUS, TR.tr("skillsTab.newAssessment.tooltip"), e -> {
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
        pane.setAlignment(Pos.TOP_CENTER);

        MainWindow.mainScreen.statusProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue() != MainScreen.Status.OPEN){
                clearEditRelatedData();
            }
        });

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

        studentPane.setStyle("-fx-padding: 0 5 5 5;");
        studentPane.setSpacing(5);
        studentPane.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(TR.tr("skillsTab.student.combobox"));
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setStyle("-fx-font-size: 12; -fx-font-weight: 800;");
        studentPane.getChildren().addAll(label, studentCombo);

        PaneUtils.setVBoxPosition(studentCombo, -1, 30, 0);
        studentCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Student s){ return s == null ? "" : s.name(); }
            @Override public Student fromString(String name){ return null; }
        });
        studentCombo.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            if(getCurrentAssessment() != null) return FXCollections.observableArrayList(getCurrentAssessment().getStudents());
            return FXCollections.observableArrayList();
        }, assessmentCombo.valueProperty()));

        assessmentCombo.valueProperty().addListener(o -> {
            if(getCurrentAssessment() != null && !getCurrentAssessment().getStudents().isEmpty()){
                pane.getChildren().setAll(optionPane, studentPane, listView);
            }else pane.getChildren().setAll(optionPane, listView);
            updateSupportModalityMessage();
        });

        supportModality.setPadding(new Insets(5));
        supportModality.setTextAlignment(TextAlignment.CENTER);
        supportModality.setStyle("-fx-font-size: 14; -fx-font-weight: 800;");
        supportModality.setWrapText(true);


        /* LIST VIEW */

        listView.disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        listView.setBorder(null);
        listView.setPadding(new Insets(0));
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        VBox.setVgrow(listView, Priority.SOMETIMES);

        skillTableElement.addListener((observable, oldValue, newValue) -> listView.refresh());
        listView.setCellFactory(param -> new SkillListCell(assessmentCombo.valueProperty(), skillTableElement));
        listView.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            if(getCurrentAssessment() != null){
                Platform.runLater(this::trySelectStudent);
                return FXCollections.observableArrayList(getCurrentAssessment().getSkills());
            }
            return FXCollections.observableArrayList();
        }, assessmentCombo.valueProperty()));

        // Theme change update
        Main.settings.darkTheme.valueProperty().addListener((observable, oldValue, newValue) -> listView.refresh());
        Main.settings.zoom.valueProperty().addListener((observable, oldValue, newValue) -> listView.refresh());

        /* LINK WITH SkillTableElement */

        // Sync assessment id
        assessmentCombo.valueProperty().addListener(o -> {
            if(getSkillTableElement() != null){
                switchSkillTableElementAssessment(getCurrentAssessmentIdOr0());
            }else if(getCurrentAssessment() != null){
                addSkillTableElement();
            }
        });
        // Sync student id
        studentCombo.valueProperty().addListener((o, oldValue, newValue) -> {
            if(getSkillTableElement() != null && getCurrentAssessmentIdOr0() != 0){
                if(getSkillTableElement().getStudentId() != getCurrentStudentIdOr0()){ // Needs update
                    getSkillTableElement().setStudentId(getCurrentStudentIdOr0());
                    getSkillTableElement().tryLoadFromStudent(oldValue);

                    Edition.setUnsave("Changed selected Student");
                }
            }else if(getCurrentAssessment() != null){
                addSkillTableElement();
            }
            updateSupportModalityMessage();
        });

    }
    private void updateSupportModalityMessage(){
        if(getCurrentStudent() != null && !getCurrentStudent().supportModality().isBlank()){
            supportModality.setText(TR.tr("skillsTab.student.supportModality", getCurrentStudent().supportModality()));
            if(!pane.getChildren().contains(supportModality)) pane.getChildren().add(supportModality);
        }else{
            supportModality.setText("");
            pane.getChildren().remove(supportModality);
        }
    }

    private void switchSkillTableElementAssessment(long id){
        Edition.setUnsave("Changed selected Assessment");
        if(getSkillTableElement().getAssessmentId() != id){ // Needs update
            // Sometimes, the id can be changed very quickly,
            // then we need to check that the id is still the same before starting the element update process
            Platform.runLater(() -> {
                if(getCurrentAssessmentIdOr0() == id){
                    getSkillTableElement().saveDefaultSize();
                    getSkillTableElement().setAssessmentId(getCurrentAssessmentIdOr0());
                }
            });
        }
    }

    // When added to document from edition, select the right options
    public void registerSkillTableElement(SkillTableElement skillTableElement){
        this.skillTableElement.set(skillTableElement);

        assessments.stream()
                .filter(assessment -> assessment.getId() == skillTableElement.getAssessmentId())
                .findFirst()
                .ifPresent(assessmentCombo::setValue);
        trySelectStudent();
    }
    public void clearEditRelatedData(){
        skillTableElement.set(null);
        assessmentCombo.setValue(null);
    }
    private void trySelectStudent(){
        if(getSkillTableElement() == null || getCurrentAssessment() == null) return;
        getCurrentAssessment()
                .getStudents()
                .stream()
                .filter(student -> student.id() == getSkillTableElement().getStudentId())
                .findFirst()
                .ifPresent(studentCombo::setValue);
    }

    // Generate the element
    private void addSkillTableElement(){
        skillTableElement.set(new SkillTableElement(0, 0, 0, true, 0, 0, 0.8, getCurrentAssessmentIdOr0(), getCurrentStudentIdOr0(), new ArrayList<>()));
        MainWindow.mainScreen.document.getPage(0).addElement(getSkillTableElement(), true, UType.NO_UNDO);
    }


    // Called when closing the SkillAssessmentWindow
    public void updateData(){
        // Update assessment name in the combo
        // This will also update the list element (updates assessmentCombo.getValue() changes)
        SkillsAssessment assessment = getCurrentAssessment();
        Student student = getCurrentStudent();
        assessmentCombo.setValue(null);
        assessmentCombo.setValue(assessment);
        studentCombo.setValue(student); // Preserve the student

        // Updating the list
        listView.refresh();
        // Updating the element
        getSkillTableElement().updateLayout();
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
    public long getCurrentAssessmentIdOr0(){
        return assessmentCombo.getValue() == null ? 0 : assessmentCombo.getValue().getId();
    }
    public Student getCurrentStudent(){
        return studentCombo.getValue();
    }
    public long getCurrentStudentIdOr0(){
        return (assessmentCombo.getValue() == null || studentCombo.getValue() == null) ? 0 : studentCombo.getValue().id();
    }

    public SkillTableElement getSkillTableElement(){
        return skillTableElement.get();
    }
    public void refreshListView(){
        listView.refresh();
    }
    public void selectAssessment(SkillsAssessment assessment){
        assessmentCombo.setValue(assessment);
    }
}
