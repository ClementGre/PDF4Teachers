/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.skillsassessment;

import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.components.ScaledComboBox;
import fr.clementgre.pdf4teachers.components.SyncColorPicker;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.NotationGraph;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;
import fr.clementgre.pdf4teachers.utils.image.ColorUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import fr.clementgre.pdf4teachers.utils.interfaces.ReturnCallBack;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
        root.setSpacing(10);
        
        // Notation style boxes
    
        HBox notationModeBox = new HBox();
        notationModeBox.setSpacing(5);
        notationModeBox.setAlignment(Pos.CENTER_LEFT);
    
        ScaledComboBox<Notation.NotationType> notationMode = new ScaledComboBox<>(false);
        notationMode.maxHeightProperty().bind(new SimpleDoubleProperty(28));
        notationMode.setPadding(new Insets(0, 5, 0, 5));
        notationMode.getItems().addAll(Notation.NotationType.values());
        notationMode.setConverter(new StringConverter<>(){
            @Override public String toString(Notation.NotationType notationType){ return notationType.getText(); }
            @Override public Notation.NotationType fromString(String string){ return null; }
        });
        notationMode.setValue(window.getAssessment().getNotationType());
        
        notationMode.setOnAction(e -> {
            window.getAssessment().setNotationType(notationMode.getValue());
            updateGrid();
        });
        
        Button saveAsDefault = getTopButton(TR.tr("skillsSettingsWindow.notationsListing.saveAsDefault"), (e) -> {
            SkillsAssessment.setDefaultNotationsType(window.getAssessment().getNotationType());
            SkillsAssessment.setDefaultNotations(window.getAssessment().getNotations());
        });
        Button loadDefault = getTopButton(TR.tr("skillsSettingsWindow.notationsListing.loadDefault"), (e) -> {
            notationMode.setValue(SkillsAssessment.getDefaultNotationsType());
            window.getAssessment().getNotations().clear();
            window.getAssessment().getNotations().addAll(SkillsAssessment.getDefaultNotations());
            updateGrid();
        });
        Button resetDefault = getTopButton(TR.tr("skillsSettingsWindow.notationsListing.reset"), (e) -> {
            notationMode.setValue(Notation.NotationType.COLOR);
            window.getAssessment().getNotations().clear();
            window.getAssessment().getNotations().addAll(SkillsAssessment.getGlobalDefaultNotations());
            updateGrid();
        });
        
        notationModeBox.getChildren().addAll(new Label(TR.tr("skillsSettingsWindow.notationMode")), notationMode, new HBoxSpacer(), saveAsDefault, loadDefault, resetDefault);
        
        // Grid
        
        grid.setVgap(5);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);
    
        updateGrid();
        
        root.getChildren().addAll(notationModeBox, grid);
        
    }
    
    private Button getTopButton(String text, EventHandler<ActionEvent> onAction){
        Button button = new Button(text);
        button.setOnAction(onAction);
        button.setMaxHeight(28);
        button.setPadding(new Insets(0, 5, 0, 5));
        return button;
    }
    
    private void updateGrid(){
        grid.getChildren().clear();
        
        grid.addRow(0, getLabel(TR.tr("skillsSettingsWindow.notationsListing.grid.acronym")),
                getLabel(TR.tr("skillsSettingsWindow.notationsListing.grid.name")),
                getLabel(TR.tr("skillsSettingsWindow.notationsListing.grid.keyboardChar")));
    
        grid.add(getLabel(TR.tr("skillsSettingsWindow.notationsListing.grid.graph")), 3, 0, 2, 1);
    
        int row = 1;
        ArrayList<NotationRow> notationRows = new ArrayList<>();
        for(Notation notation : window.getAssessment().getNotations()){
            NotationRow notationRow = new NotationRow(window.getAssessment(), notation);
            int finalRow = row;
            notationRow.fillLineWithNotation(grid, row, () -> { // Delete notation
                window.getAssessment().getNotations().remove(notation);
                updateGrid();
            }, () -> { // Get next row NotationRow to select field
                return (notationRows.size() > finalRow) ? notationRows.get(finalRow) : null;
            });
            notationRows.add(notationRow);
            row++;
        }
        
        Button addNotationButton = new Button(TR.tr("skillsSettingsWindow.notationsListing.grid.addNotation"));
        addNotationButton.setPadding(new Insets(5, 10, 5, 10));
        GridPane.setMargin(addNotationButton, new Insets(5, 0, 0, 0));
        addNotationButton.setDisable(window.getAssessment().getNotations().size() >= 8);
        addNotationButton.setOnAction(e -> {
            Notation notation = new Notation(window.getAssessment());
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
        grid.getColumnConstraints().addAll(getConstraint(14), getConstraint(34), getConstraint(21), new ColumnConstraints(20), new ColumnConstraints(120), new ColumnConstraints(28));
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
    
    private static class NotationRow {
        private final SkillsAssessment assessment;
        private final Notation notation;
    
        public final TextField acronym;
        public final TextField name;
        public final TextField keyboardChar;
        private final NotationGraph graph;
        
        public NotationRow(SkillsAssessment assessment, Notation notation){
            this.assessment = assessment;
            this.notation = notation;
            acronym = new TextField(notation.getAcronym().toUpperCase());
            name = new TextField(notation.getName());
            keyboardChar = new TextField(notation.getKeyboardChar().toUpperCase());
            graph = new NotationGraph(assessment.getNotationType(), notation, false);
        }
    
        private void fillLineWithNotation(GridPane grid, int line, CallBack onDelete, ReturnCallBack<NotationRow> getNextRow){
            
            acronym.getStyleClass().add("noTextFieldClear");
            keyboardChar.getStyleClass().add("noTextFieldClear");
            
            acronym.textProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue.length() > 2) newValue = newValue.substring(newValue.length() - 2);
                newValue = newValue.toUpperCase();
                acronym.setText(newValue);
                notation.setAcronym(newValue);
                if(assessment.getNotationType() == Notation.NotationType.CHAR) graph.updateGraph(assessment.getNotationType(), notation, false);
                
            });
            name.textProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue.length() > 100){
                    newValue = newValue.substring(0, 100);
                    name.setText(newValue);
                }
                notation.setName(newValue);
            });
            keyboardChar.textProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue.length() > 1) newValue = newValue.substring(newValue.length() - 1);
                newValue = newValue.toUpperCase();
                keyboardChar.setText(newValue);
                notation.setKeyboardChar(newValue);
            });
            
            // Enter and tab support to move to next field
            focusNextField(acronym, () -> {
                NotationRow nextRow = getNextRow.call();
                if(nextRow != null) return nextRow.acronym;
                return null;
            }, () -> name);
            focusNextField(name, () -> {
                NotationRow nextRow = getNextRow.call();
                if(nextRow != null) return nextRow.name;
                return null;
            }, () -> keyboardChar);
            focusNextField(keyboardChar, () -> {
                NotationRow nextRow = getNextRow.call();
                if(nextRow != null) return nextRow.keyboardChar;
                return null;
            },  () -> {
                NotationRow nextRow = getNextRow.call();
                if(nextRow != null) return nextRow.acronym;
                return null;
            });
    
            acronym.setMaxWidth(50);
            name.setMaxWidth(200);
            keyboardChar.setMaxWidth(50);
        
            grid.addRow(line, acronym, name, keyboardChar, graph);
        
            // GRAPH EDITOR
        
            if(assessment.getNotationType() == Notation.NotationType.COLOR){
                ColorPicker colorPicker = new SyncColorPicker(ColorUtils.parseWebOr(notation.getData(), Color.DARKGREEN));
                colorPicker.setMaxHeight(28);
                colorPicker.setPadding(new Insets(0, 5, 0, 5));
                colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                    notation.setData(ColorUtils.toRGBHex(newValue));
                    graph.updateGraph(assessment.getNotationType(), notation, false);
                });
                grid.addRow(line, colorPicker);
            
            }else if(assessment.getNotationType() == Notation.NotationType.ICON){
                Button browseButton = new Button(TR.tr("file.browse"));
                browseButton.setPadding(new Insets(0, 5, 0, 5));
                browseButton.setMaxHeight(28);
                browseButton.setOnAction(e -> {
    
                    File file = FilesChooserManager.showFileDialog(FilesChooserManager.SyncVar.LAST_GALLERY_OPEN_DIR, TR.tr("dialog.file.extensionType.image"),
                            ImageUtils.ACCEPTED_EXTENSIONS.stream().map((s) -> "*." + s).toList().toArray(new String[0]));
                    if(file == null || !file.exists()) return;
                    
                    try{
                        notation.setData(ImageUtils.imageToBase64(ImageUtils.resizeImageToSquare(ImageIO.read(file), 40)));
                        graph.updateGraph(assessment.getNotationType(), notation, false);
                    }catch(IOException ex){
                        ex.printStackTrace();
                        new ErrorAlert(null, ex.getMessage(), false);
                    }
                });
                grid.addRow(line, browseButton);
            }
        
            // DELETE
        
            Button deleteButton = new Button();
            deleteButton.setOnAction(e -> onDelete.call());
            deleteButton.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("actions.delete")));
            PaneUtils.setHBoxPosition(deleteButton, 28, 28, 0);
            deleteButton.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.TRASH, "darkred", 0, 20, ImageUtils.defaultDarkColorAdjust));
            deleteButton.setCursor(Cursor.HAND);
            grid.addRow(line, deleteButton);
        }
        private void focusNextField(TextField field, ReturnCallBack<TextField> getNextRowField, ReturnCallBack<TextField> getNextColumnField){
            field.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                if(e.getCode() == KeyCode.ENTER){
                    TextField nextRow = getNextRowField.call();
                    if(nextRow != null) nextRow.requestFocus();
                    e.consume();
                }else if(e.getCode() == KeyCode.TAB){
                    TextField nextCol = getNextColumnField.call();
                    if(nextCol != null) nextCol.requestFocus();
                    e.consume();
                }
            });
        }
        
    }
    
    private void fillLineWithDefaultNotation(int line, Notation notation){
        Label acronym = new Label();
        acronym.setText(notation.getAcronym());
        Label name = new Label();
        name.setText(notation.getName());
        Label keyboardChar = new Label();
        keyboardChar.setText(notation.getKeyboardChar());
        NotationGraph graph = new NotationGraph(Notation.NotationType.CHAR, notation, true);
        
        grid.addRow(line, acronym, name, keyboardChar, graph);
    }
}
