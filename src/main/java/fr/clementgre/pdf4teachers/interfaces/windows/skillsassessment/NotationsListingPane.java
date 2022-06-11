/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.skillsassessment;

import fr.clementgre.pdf4teachers.components.SyncColorPicker;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.NotationGraph;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class NotationsListingPane extends Tab {
    
    private final VBox root = new VBox();
    
    private final GridPane grid = new GridPane();
    
    private final SkillsAssessmentWindow window;
    public NotationsListingPane(SkillsAssessmentWindow window){
        this.window = window;
    
        setText(TR.tr("skillsSettingsWindow.notationsListing.title"));
        setClosable(false);
        setContent(root);
        root.setStyle("-fx-padding: 15;");
        root.setSpacing(5);
        
        grid.setVgap(5);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);
    
        updateGrid();
        
        root.getChildren().add(grid);
        
    }
    
    private void updateGrid(){
        grid.getChildren().clear();
        
        grid.addRow(0, getLabel(TR.tr("skillsSettingsWindow.notationsListing.grid.acronym")),
                getLabel(TR.tr("skillsSettingsWindow.notationsListing.grid.name")),
                getLabel(TR.tr("skillsSettingsWindow.notationsListing.grid.keyboardChar")));
    
        grid.add(getLabel(TR.tr("skillsSettingsWindow.notationsListing.grid.graph")), 3, 0, 2, 1);
    
        int row = 1;
        for(Notation notation : window.getAssessment().getNotations()){
            fillLineWithNotation(row, window.getAssessment(), notation);
            row++;
        }
        
        Button addNotationButton = new Button(TR.tr("skillsSettingsWindow.notationsListing.grid.addNotation"));
        addNotationButton.setPadding(new Insets(5, 10, 5, 10));
        GridPane.setMargin(addNotationButton, new Insets(5, 0, 0, 0));
        addNotationButton.setDisable(window.getAssessment().getNotations().size() >= 8);
        addNotationButton.setOnAction(e -> {
            Notation notation = new Notation();
            window.getAssessment().getNotations().add(notation);
            updateGrid();
        });
        grid.add(addNotationButton, 0, row, 5, 1);
        row++;
        
        grid.add(getLabel(TR.tr("skillsSettingsWindow.notationsListing.grid.defaultsNotations")), 0, row, 5, 1);
        row++;
    
        for(Notation notation : SkillsAssessment.getOtherNotations()){
            fillLineWithDefaultNotation(row, notation);
            row++;
        }
        
        grid.getColumnConstraints().clear();
        grid.getColumnConstraints().addAll(getConstraint(15), getConstraint(35), getConstraint(25), new ColumnConstraints(20), getConstraint(15), new ColumnConstraints(28));
    }
    
    private static ColumnConstraints getConstraint(int percent){
        ColumnConstraints constraint = new ColumnConstraints();
        constraint.setPercentWidth(percent);
        return constraint;
    }
    private static Label getLabel(String text){
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");
        label.setPadding(new Insets(10, 0, 5, 0));
        return label;
    }
    
    public void save(){
    
    }
    
    
    private void fillLineWithNotation(int line, SkillsAssessment assessment, Notation notation){
        TextField acronym = new TextField(notation.getAcronym().toUpperCase());
        TextField name = new TextField(notation.getName());
        TextField keyboardChar = new TextField(notation.getKeyboardChar().toUpperCase());
        NotationGraph graph;
        
        acronym.getStyleClass().add("noTextFieldClear");
        keyboardChar.getStyleClass().add("noTextFieldClear");
        
        if(assessment.getNotationType() == Notation.NotationType.CHAR) notation.setData(notation.getAcronym());
        graph = new NotationGraph(assessment.getNotationType(), notation.getData());

        acronym.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length() > 2) newValue = newValue.substring(newValue.length() - 2);
            newValue = newValue.toUpperCase();
            acronym.setText(newValue);
        });
        name.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length() > 100){
                newValue = newValue.substring(0, 100);
                name.setText(newValue);
            }
        });
        keyboardChar.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length() > 1) newValue = newValue.substring(newValue.length() - 1);
            newValue = newValue.toUpperCase();
            keyboardChar.setText(newValue);
        });
        
        acronym.setMaxWidth(50);
        name.setMaxWidth(200);
        keyboardChar.setMaxWidth(50);

        grid.addRow(line, acronym, name, keyboardChar, graph);

        // GRAPH EDITOR
        
        if(assessment.getNotationType() == Notation.NotationType.COLOR){
            ColorPicker colorPicker = new SyncColorPicker(Color.web(notation.getData()));
            grid.addRow(line, colorPicker);
    
        }else if(assessment.getNotationType() == Notation.NotationType.ICON){
            Button browseButton = new Button(TR.tr("file.browse"));
            browseButton.setPadding(new Insets(0, 5, 0, 5));
            browseButton.setMaxHeight(28);
            grid.addRow(line, browseButton);
        }
        
        // DELETE
        
        Button deleteButton = new Button();
        deleteButton.setOnAction(e -> {
            assessment.getNotations().remove(notation);
            updateGrid();
        });
        deleteButton.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("actions.delete")));
        PaneUtils.setHBoxPosition(deleteButton, 28, 28, 0);
        deleteButton.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.TRASH, "darkred", 0, 20, ImageUtils.defaultDarkColorAdjust));
        deleteButton.setCursor(Cursor.HAND);
        grid.addRow(line, deleteButton);
    }
    
    private void fillLineWithDefaultNotation(int line, Notation notation){
        Label acronym = new Label();
        acronym.setText(notation.getAcronym());
        Label name = new Label();
        name.setText(notation.getName());
        Label keyboardChar = new Label();
        keyboardChar.setText(notation.getKeyboardChar());
        NotationGraph graph = new NotationGraph(Notation.NotationType.CHAR, notation.getAcronym());
        
        grid.addRow(line, acronym, name, keyboardChar, graph);
    }
}
