/*
 * Copyright (c) 2022-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.SkillTableElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.SkillUndoAction;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.EditionSkill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.*;

import java.util.concurrent.atomic.AtomicInteger;

public class SkillListCell extends ListCell<Skill> {
    
    private final HBox root = new HBox();
    private final VBox left = new VBox();
    private final Label acronym = new Label();
    private final Label name = new Label();
    private final ComboBox<Notation> comboBox = new ComboBox<>();
    
    private final ContextMenu contextMenu = new ContextMenu();
    
    // Edited each time the cell updates
    private EditionSkill editionSkill;
    
    private final ObjectProperty<SkillsAssessment> skillAssessment;
    private final ObjectProperty<SkillTableElement> skillTableElement;
    public SkillListCell(ObjectProperty<SkillsAssessment> skillAssessment, ObjectProperty<SkillTableElement> skillTableElement){
        this.skillAssessment = skillAssessment;
        this.skillTableElement = skillTableElement;
        
        Region spacer = new Region();
        GridPane.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinWidth(0);
        spacer.setPrefWidth(Double.MAX_VALUE);
    
        left.getChildren().addAll(acronym, name);
        root.getChildren().addAll(left, new HBoxSpacer(), comboBox);
        root.setMinWidth(0);
        root.setPrefWidth(1);
        root.setAlignment(Pos.CENTER_LEFT);
        
        acronym.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        name.setStyle("-fx-font-size: 11px;");
        
        comboBox.setCellFactory(c -> new ComboListCell(skillAssessment, true));
        comboBox.setButtonCell(new ComboListCell(skillAssessment, false));
        comboBox.setPadding(new Insets(0));
        HBox.setMargin(comboBox, new Insets(0, 0, 0, 5));
        comboBox.setPrefWidth(30);
        comboBox.setMinWidth(30);
        comboBox.setPrefHeight(30);
    
        
        setOnMouseClicked(e -> requestFocus());
        root.setPadding(new Insets(-5));
        
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) requestFocus();
        });
        
        // LISTENERS
        setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.TAB){
                int selected = getListView().getSelectionModel().getSelectedIndex(); // Index of this cell
                if(getListView().getItems().size() > selected+1){
                    getListView().getSelectionModel().select(selected+1);
                }else getListView().getSelectionModel().select(0);
                
                e.consume();
                
            }else if(e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE){
                if(getSkillAssessment() == null || editionSkill == null) return;
                comboBox.setValue(null); // editionSkill notationId will be updated in the ComboBox listener
                e.consume();
            }
        });
        setOnKeyTyped(e -> {
            if(getSkillAssessment() == null || editionSkill == null) return;
            
            String text = StringUtils.replaceSymbolsToDigitsIfFrenchLayout(e.getCharacter());
            for(Notation notation : getSkillAssessment().getNotationsWithDefaults()){
                if(notation.getKeyboardChar().equalsIgnoreCase(text)){
                    comboBox.setValue(notation); // editionSkill notationId will be updated in the ComboBox listener
                    e.consume();
                    return;
                }
            }
        });
        
        // MENU
    
        NodeMenuItem resetItem = new NodeMenuItem(TR.tr("actions.reset"), false);
        resetItem.setKeyCombinaison(new KeyCodeCombination(KeyCode.DELETE));
        NodeMenuItem copyItem = new NodeMenuItem(TR.tr("skillsTab.list.contextMenu.copyMarkOnAllSkills"), false);
        
        resetItem.setOnAction(e -> {
            if(editionSkill != null && editionSkill.getNotationId() != 0){
                MainWindow.mainScreen.getUndoEngine().registerNewAction(new SkillUndoAction(UType.ELEMENT, getSkillAssessment().getId(), editionSkill.getSkillId(), editionSkill.getNotationId()));
                editionSkill.setNotationId(0);
                MainWindow.skillsTab.refreshListView();
                getSkillTableElement().updateSkillsNotation();
                Edition.setUnsave("SkillListCell ComboBox notation changed");
            }
        });
        
        copyItem.setOnAction(e -> {
            if(editionSkill != null && getSkillAssessment() != null && getSkillTableElement() != null){
                AtomicInteger i = new AtomicInteger();
                getSkillAssessment().getSkills().forEach(skill -> {
                    EditionSkill editionSkill = getSkillTableElement().getEditionSkills().stream().filter(es -> es.getSkillId() == skill.getId()).findFirst().orElse(null);
                    // null, this one, or already the same notation
                    if(editionSkill == null || editionSkill == this.editionSkill || editionSkill.getNotationId() == this.editionSkill.getNotationId()) return;
                    
                    MainWindow.mainScreen.getUndoEngine().registerNewAction(new SkillUndoAction(i.getAndIncrement() == 0 ? UType.ELEMENT : UType.ELEMENT_NO_COUNT_BEFORE, getSkillAssessment().getId(), skill.getId(), editionSkill.getNotationId()));
                    editionSkill.setNotationId(this.editionSkill.getNotationId());
                });
                if(i.get() != 0){
                    MainWindow.skillsTab.refreshListView();
                    getSkillTableElement().updateSkillsNotation();
                    Edition.setUnsave("SkillListCell ComboBox notation changed");
                }
            }
        });
        
        contextMenu.getItems().addAll(resetItem, copyItem);
        
    }
    
    private static class ComboListCell extends ListCell<Notation>{
        private final Pane root = new Pane();
        private NotationGraph graph;
        
        private final ObjectProperty<SkillsAssessment> skillAssessment;
        private final boolean popup;
        public ComboListCell(ObjectProperty<SkillsAssessment> skillAssessment, boolean popup){
            this.skillAssessment = skillAssessment;
            this.popup = popup;
        }
        
        @Override
        protected void updateItem(Notation notation, boolean empty){
            super.updateItem(notation, empty);
            if(notation == null || empty){
                setGraphic(null);
            }else{
                
                if(!popup){
                    graph = new NotationGraph();
                    graph.setTranslateX(-5);
                    graph.setTranslateY(-1);
                }else{
                    graph = new NotationGraph(Main.settings.zoom.getValue());
                }
                root.getChildren().setAll(graph);
                
                // Negative ids are reserved for default not editable notations. => Italic
                graph.updateGraph(getNotationType(notation), notation, false);
                if(!popup && getNotationType(notation) == Notation.NotationType.ICON){ // Force the background to cover the combo arrow
                    if(Main.settings.darkTheme.getValue()) graph.setStyle(graph.getStyle() + " -fx-background-color: #111111;");
                    else graph.setStyle(graph.getStyle() + " -fx-background-color: white;");
                }
                
                setGraphic(root);
                
            }
        }
        private Notation.NotationType getNotationType(Notation notation){
            if(notation.isDefaultNotation()) return Notation.NotationType.CHAR; // Negative ids are reserved for default not editable notations.
            return skillAssessment.get().getNotationType();
        }
    }
    
    @Override
    public void updateItem(Skill skill, boolean empty){
        super.updateItem(skill, empty);
        
        if(skill == null || empty || getSkillTableElement() == null){
            editionSkill = null;
            setGraphic(null);
            setTooltip(null);
            setContextMenu(null);
            comboBox.setOnAction(null);
        }else{
            // Event disabled during loading of items.
            comboBox.setOnAction(null);
            
            // Get the matching EditionSkill that contains the selected Notation id.
            editionSkill = skill.getMatchingEditionSkill(getSkillTableElement().getEditionSkills());
            if(editionSkill == null){
                editionSkill = new EditionSkill(skill.getId(), 0);
                getSkillTableElement().getEditionSkills().add(editionSkill);
            }
            
            // Updates the ComboBox items, value and add the event
            comboBox.setItems(FXCollections.observableList(getSkillAssessment().getNotationsWithDefaults()));
            comboBox.setValue(editionSkill.getMatchingNotation(getSkillAssessment()));
            comboBox.setOnAction(e -> {
                long newId = comboBox.getValue() == null ? 0 : comboBox.getValue().getId();
                if(editionSkill != null && editionSkill.getNotationId() != newId){
                    MainWindow.mainScreen.getUndoEngine().registerNewAction(new SkillUndoAction(UType.ELEMENT, getSkillAssessment().getId(), skill.getId(), editionSkill.getNotationId()));
                    editionSkill.setNotationId(newId);
                    getSkillTableElement().updateSkillsNotation();
                    Edition.setUnsave("SkillListCell ComboBox notation changed");
                }
            });
    
            // Update text & set root
            acronym.setText(skill.getAcronym());
            name.setText(skill.getName());
            setTooltip(PaneUtils.genWrappedToolTip(skill.getName()));
            setGraphic(root);
            setContextMenu(contextMenu);
        }
        
    }
    
    
    
    
    public SkillsAssessment getSkillAssessment(){
        return skillAssessment.get();
    }
    public ObjectProperty<SkillsAssessment> skillAssessmentProperty(){
        return skillAssessment;
    }
    public SkillTableElement getSkillTableElement(){
        return skillTableElement.get();
    }
    public ObjectProperty<SkillTableElement> skillTableElementProperty(){
        return skillTableElement;
    }
}
