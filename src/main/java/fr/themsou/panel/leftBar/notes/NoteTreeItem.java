package fr.themsou.panel.leftBar.notes;

import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class NoteTreeItem extends TreeItem {

    private NoteElement core;

    public static DecimalFormat format = new DecimalFormat("0.#");

    // JavaFX
    private TreeCell<String> cell;
    private HBox pane;

    Region spacer = new Region();
    private Text name = new Text();
    private Text value = new Text();
    private Text slash = new Text("/");
    private Text total = new Text();

    private Button newNote;

    private TextArea nameField = new TextArea("☺");
    private TextArea noteField = new TextArea("☺");
    private TextArea totalField = new TextArea("☺");

    // EVENTS
    private EventHandler<MouseEvent> mouseEnteredEvent;
    private EventHandler<MouseEvent> mouseExitedEvent;
    private ChangeListener<Boolean> selectedListener;

    public NoteTreeItem(NoteElement core){

        this.core = core;

        setupGraphic();
        setupEvents();
    }

    public void setupEvents(){
        
        selectedListener = (observable, oldValue, newValue) -> {
            if(newValue){ // Est sélectionné
                newNote.setVisible(true);
                newNote.setStyle("-fx-background-color: #0078d7");

                nameField.setText(core.getName());
                noteField.setText(core.getValue() == -1 ? "" : format.format(core.getValue()));
                totalField.setText(format.format(core.getTotal()));
                pane.getChildren().clear();

                if(LBNoteTab.lockRatingScale.get()){
                    if(hasSubNote()) pane.getChildren().addAll(name, spacer, value, slash, total, newNote);
                    else pane.getChildren().addAll(name, spacer, noteField, slash, total, newNote);
                }else{
                    if(hasSubNote()) pane.getChildren().addAll(nameField, spacer, value, slash, total, newNote);
                    else pane.getChildren().addAll(nameField, spacer, noteField, slash, totalField, newNote);
                }

                Platform.runLater(() -> {
                    noteField.requestFocus();
                    noteField.positionCaret(noteField.getText().length());
                });

            }else if(oldValue){ // n'est plus selectionné
                newNote.setVisible(false);
                newNote.setStyle(null);

                pane.getChildren().clear();
                pane.getChildren().addAll(name, spacer, value, slash, total, newNote);
            }
        };

        mouseEnteredEvent = event -> {
            if(!cell.isFocused()) newNote.setVisible(true);
        };

        mouseExitedEvent = event -> {
            if(!cell.isFocused()) newNote.setVisible(false);
        };

        newNote.setOnAction(event -> {
            setExpanded(true);
            Main.lbNoteTab.newNoteElementAuto(this).select();
        });

    }

    public void setupGraphic(){

        pane = new HBox();
        pane.setAlignment(Pos.CENTER);
        pane.setPrefHeight(18);
        pane.setStyle("-fx-padding: -6 -6 -6 0;"); // top - right - bottom - left

        // TEXTS

        name.setFont(new Font(14));
        name.textProperty().bind(core.nameProperty().concat(" | ").concat(core.getIndex()));
        slash.setFont(new Font(14));

        value.setFont(new Font(14));
        HBox.setMargin(value, new Insets(0, 0, 0, 5));
        value.textProperty().bind(Bindings.createStringBinding(() -> (core.getValue() == -1 ? "?" : format.format(core.getValue())), core.valueProperty()));

        total.setFont(new Font(14));
        HBox.setMargin(total, new Insets(0, 5, 0, 0));
        total.textProperty().bind(Bindings.createStringBinding(() -> format.format(core.getTotal()), core.totalProperty()));

        // FIELDS

        nameField.setStyle("-fx-font-size: 13;");
        nameField.setFont(new Font(13));
        nameField.setMinHeight(29);
        nameField.setMaxHeight(29);
        nameField.setMinWidth(29);

        noteField.setStyle("-fx-font-size: 13;");
        noteField.setFont(new Font(13));
        noteField.setMinHeight(29);
        noteField.setMaxHeight(29);
        noteField.setMinWidth(29);
        HBox.setMargin(noteField, new Insets(0, 0, 0, 5));

        totalField.setStyle("-fx-font-size: 13;");
        totalField.setFont(new Font(13));
        totalField.setMinHeight(29);
        totalField.setMaxHeight(29);
        totalField.setMinWidth(29);
        HBox.setMargin(totalField, new Insets(0, 5, 0, 0));

        Text meter = new Text();
        meter.setFont(nameField.getFont());
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            String newText = newValue.replaceAll("[^ -~À-ÿ]", "");
            if(newText.length() >= 20) newText = newText.substring(0, 20);

            nameField.setText(newText);
            meter.setText(newText);
            nameField.setMaxWidth(meter.getLayoutBounds().getWidth()+20);

            core.setName(newText);
        });
        noteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.contains("/")){
                totalField.requestFocus();
                totalField.positionCaret(totalField.getText().length());
            }
            String newText = newValue.replaceAll("[^0123456789.]", "");
            if(newText.length() >= 5) newText = newText.substring(0, 5);

            noteField.setText(newText);
            meter.setText(newText);
            noteField.setMaxWidth(meter.getLayoutBounds().getWidth()+20);

            try{
                core.setValue(Double.parseDouble(newText.replaceAll(Pattern.quote(","), ".")));
            }catch(NumberFormatException e){
                core.setValue(-1);
            }
        });
        totalField.textProperty().addListener((observable, oldValue, newValue) -> {
            String newText = newValue.replaceAll("[^0123456789.,]", "");
            if(newText.length() >= 5) newText = newText.substring(0, 5);

            totalField.setText(newText);
            meter.setText(newText);
            totalField.setMaxWidth(meter.getLayoutBounds().getWidth()+20);

            try{
                core.setTotal(Double.parseDouble(newText.replaceAll(Pattern.quote(","), ".")));
            }catch(NumberFormatException e){
                core.setTotal(0);
            }
        });

        // OTHER

        HBox.setHgrow(spacer, Priority.ALWAYS);

        newNote = new Button();
        newNote.setGraphic(Builders.buildImage(getClass().getResource("/img/more.png")+"", 0, 0));
        Builders.setPosition(newNote, 0, 0, 30, 30, true);
        newNote.disableProperty().bind(LBNoteTab.lockRatingScale);
        newNote.setVisible(false);

        pane.getChildren().addAll(name, spacer, value, slash, total, newNote);

    }
    public void updateGraphic(){

    }

    public void updateCell(TreeCell<String> cell){

        if(cell == null) return;
        if(this.cell != null) this.cell.selectedProperty().removeListener(selectedListener);
        this.cell = cell;

        cell.setGraphic(pane);
        cell.setStyle(null);
        cell.setStyle("-fx-padding: 6 6 6 2;");
        //cell.setContextMenu(menu);
        cell.setOnMouseEntered(mouseEnteredEvent);
        cell.setOnMouseExited(mouseExitedEvent);

        cell.selectedProperty().addListener(selectedListener);

    }

    public void makeSum(){

        double value = 0;
        double total = 0;

        for(int i = 0; i < getChildren().size(); i++){
            NoteTreeItem children = (NoteTreeItem) getChildren().get(i);

            total += children.getCore().getTotal();
            if(children.getCore().getValue() > 0) value += children.getCore().getValue();
        }

        core.setValue(value);
        core.setTotal(total);

        if(getParent() != null){
            ((NoteTreeItem) getParent()).makeSum();
        }
    }

    public boolean hasSubNote(){
        return getChildren().size() != 0;
    }

    public NoteElement getCore() {
        return core;
    }
    public void setCore(NoteElement core) {
        this.core = core;
    }
    public TreeCell<String> getCell() {
        return cell;
    }
}
