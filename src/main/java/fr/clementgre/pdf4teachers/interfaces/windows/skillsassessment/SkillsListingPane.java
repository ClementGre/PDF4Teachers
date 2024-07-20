/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.skillsassessment;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.autocompletiontextfield.AutoCompletionTextFieldBinding;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import fr.clementgre.pdf4teachers.utils.FilterUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class SkillsListingPane extends Tab {
    
    private final VBox root = new VBox();
    
    private final TableView<Skill> tableView = new TableView<>();
    private final TableColumn<Skill, String> acronymCol = new TableColumn<>(TR.tr("skillsTab.skill.acronym"));
    private final TableColumn<Skill, String> nameCol = new TableColumn<>(TR.tr("skillsTab.skill.name"));
    
    private final HBox inputs = new HBox();
    private final TextField acronymField = new TextField();
    private final TextField nameField = new TextField();
    private final Button addSkill = new Button(TR.tr("actions.add"));
    
    private final SkillsAssessmentWindow window;
    public SkillsListingPane(SkillsAssessmentWindow window){
        this.window = window;
        
        setText(TR.tr("skillsSettingsWindow.skillsListing.title"));
        setClosable(false);
        setContent(root);
        root.setStyle("-fx-padding: 15;");
        root.setSpacing(15);
        
        // Table
        
        tableView.getColumns().addAll(acronymCol, nameCol);
        
        acronymCol.setCellValueFactory(skill -> skill.getValue().acronymProperty());
        nameCol.setCellValueFactory(skill -> skill.getValue().nameProperty());
        acronymCol.setCellFactory(p -> getCellFactory(acronymCol, true));
        nameCol.setCellFactory(p -> getCellFactory(nameCol, false));
        acronymCol.setReorderable(false);
        nameCol.setReorderable(false);
        acronymCol.setSortable(false);
        nameCol.setSortable(false);
    
        acronymCol.setMinWidth(150);
        acronymCol.setMaxWidth(150);
        
        tableView.setRowFactory(getRowFactory());
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.setOnKeyPressed(e -> {
            if(e.getCode().equals(KeyCode.DELETE)){
                tableView.getItems().remove(tableView.getSelectionModel().getSelectedItem());
            }
        });
        
        VBox.setVgrow(tableView, Priority.ALWAYS);
        
        updateList();
        
        // Add inputs
        
        inputs.setSpacing(5);
        inputs.getStyleClass().add("noTextFieldClear");
        
        AutoCompletionTextFieldBinding<Skill> acronymAuto = new AutoCompletionTextFieldBinding<>(acronymField, param -> {
            return MainWindow.skillsTab.getAllSkills().stream()
                    .filter(skill -> skill.getAcronym().toLowerCase().contains(param.getUserText().toLowerCase()) && !skill.getAcronym().equals(param.getUserText()))
                    .unordered().filter(FilterUtils.distinctByKeys(Skill::getName, Skill::getAcronym))
                    .toList();
        }, new StringConverter<>() {
            @Override public String toString(Skill s){ return s.getAcronym(); }
            @Override public Skill fromString(String s){ return null; }
        });
        acronymAuto.setOnAutoCompleted(e -> {
            if(nameField.getText().isBlank()) nameField.setText(e.getCompletion().getName());
        });
        // TODO : scale auto completion popup
    
        AutoCompletionTextFieldBinding<Skill> nameAuto = new AutoCompletionTextFieldBinding<>(nameField, param -> {
            return MainWindow.skillsTab.getAllSkills().stream()
                    .filter(skill -> skill.getName().toLowerCase().contains(param.getUserText().toLowerCase()) && !skill.getName().equals(param.getUserText()))
                    .unordered().filter(FilterUtils.distinctByKeys(Skill::getName, Skill::getAcronym))
                    .toList();
        }, new StringConverter<>() {
            @Override public String toString(Skill s){ return s.getName(); }
            @Override public Skill fromString(String s){ return null; }
        });
        nameAuto.setOnAutoCompleted(e -> {
            if(acronymField.getText().isBlank()) acronymField.setText(e.getCompletion().getAcronym());
        });
        acronymField.setPrefWidth(150);
        HBox.setHgrow(nameField, Priority.ALWAYS);
    
        acronymField.setPromptText(TR.tr("skillsTab.skill.acronym"));
        nameField.setPromptText(TR.tr("skillsTab.skill.name"));
        
        acronymField.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ENTER){
                addSkill.fire();
                e.consume();
            }
        });
        nameField.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ENTER){
                addSkill.fire();
                e.consume();
            }
        });
        
        addSkill.setMinHeight(28);
        addSkill.setPrefHeight(28);
        addSkill.setPadding(new Insets(0, 5, 0, 5));
        addSkill.disableProperty().bind(acronymField.textProperty().isEmpty().or(nameField.textProperty().isEmpty()));
        addSkill.setOnAction(e -> {
            Skill skill = new Skill(acronymField.getText(), nameField.getText(), window.getAssessment());
            tableView.getItems().add(skill);
    
            acronymField.clear();
            nameField.clear();
        });
        
        inputs.getChildren().addAll(acronymField, nameField, addSkill);
        
        root.getChildren().addAll(inputs, tableView);
    }
    
    public void updateList(){
        tableView.getItems().setAll(window.getAssessment().getSkills());
    }
    
    private TableCell<Skill, String> getCellFactory(TableColumn<Skill, String> column, boolean acronym /* true: acronym, false: name */){
        return new TableCell<>(){
            @Override
            protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                if(item == null || empty){
                    setText(null);
                    setGraphic(null);
                }else{
                    setStyle("-fx-padding: 4 6;");
                    Text text = new Text(item);
                    text.wrappingWidthProperty().bind(column.widthProperty().subtract(25));
                    setGraphic(text);
                    setText(null);
                    setOnKeyPressed(e -> {
                        if(e.getCode() == KeyCode.DELETE){
                            tableView.getItems().remove(getIndex());
                        }
                    });
                }
            }
        };
    }
    
    
    // from https://stackoverflow.com/questions/28603224/sort-tableview-with-drag-and-drop-rows, edited by Clément Grennerat
    private static int draggingIndex;
    private static final String DRAG_KEY = "TableViewDragRow";
    private Callback<TableView<Skill>, TableRow<Skill>> getRowFactory(){
        return tv -> {
            TableRow<Skill> row = new TableRow<>();
            
            row.setOnDragDetected(event -> {
                if(!row.isEmpty()) {
                    draggingIndex = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(Main.INTERNAL_FORMAT, DRAG_KEY);
                    db.setContent(cc);
                    event.consume();
                }
            });
        
            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if(DRAG_KEY.equals(db.getContent(Main.INTERNAL_FORMAT))){
                    if(row.getIndex() != draggingIndex){
                        Skill draggedSkill = tableView.getItems().remove(draggingIndex);
                        
                        int dropIndex;
                        if(row.isEmpty()) dropIndex = tableView.getItems().size();
                        else dropIndex = row.getIndex();
                        
                        tableView.getItems().add(dropIndex, draggedSkill);
                        tableView.getSelectionModel().select(dropIndex);
                        draggingIndex = dropIndex;
                    }
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                }
            });
        
            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if(DRAG_KEY.equals(db.getContent(Main.INTERNAL_FORMAT))){
                    if(row.getIndex() != draggingIndex){
                        Skill draggedSkill = tableView.getItems().remove(draggingIndex);
    
                        int dropIndex;
                        if(row.isEmpty()) dropIndex = tableView.getItems().size();
                        else dropIndex = row.getIndex();
    
                        tableView.getItems().add(dropIndex, draggedSkill);
                    }
                    event.setDropCompleted(true);
                    tableView.getSelectionModel().select(draggingIndex);
                    event.consume();
                }
            });
    
            ContextMenu menu = new ContextMenu();
            NodeMenuItem delete = new NodeMenuItem(TR.tr("actions.delete"), false);
            menu.getItems().add(delete);
            NodeMenuItem.setupMenu(menu);
            
            delete.setOnAction(e -> {
                tableView.getItems().remove(row.getItem());
            });
            
            row.setContextMenu(menu);
            
            row.setOnMouseClicked(e -> {
                if(row.getItem() == null) return;
                acronymField.setText(row.getItem().getAcronym());
                nameField.setText(row.getItem().getName());
            });
        
            return row;
        };
    }
    
    public void save(){
        window.getAssessment().getSkills().clear();
        window.getAssessment().getSkills().addAll(tableView.getItems());
    }
}
