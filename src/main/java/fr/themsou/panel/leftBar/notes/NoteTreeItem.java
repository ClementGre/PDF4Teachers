package fr.themsou.panel.leftBar.notes;

import fr.themsou.utils.Builders;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class NoteTreeItem extends TreeItem {

    String name = "Nouvelle note";
    DoubleProperty total = new SimpleDoubleProperty(-1);
    DoubleProperty value = new SimpleDoubleProperty(-1);

    TreeCell<String> cell;
    HBox pane;

    Text nameText;
    Button newNote;
    EventHandler<MouseEvent> mouseEnteredEvent;
    EventHandler<MouseEvent> mouseExitedEvent;
    ChangeListener<Boolean> focusedListener;

    TextArea nameField = new TextArea("Nouvelle note");
    TextArea noteField = new TextArea("5");
    TextArea totalField = new TextArea("20");

    public NoteTreeItem(){
        setupGraphic();
        setupEvents();
    }

    public void setupEvents(){

        focusedListener = (observable, oldValue, newValue) -> {
            if(newValue){
                newNote.setVisible(true);
                newNote.setStyle("-fx-background-color: #0078d7");

            }else if(oldValue){
                newNote.setVisible(false);
                newNote.setStyle(null);

            }
        };

        mouseEnteredEvent = event -> {
            if(!cell.isFocused()) newNote.setVisible(true);
        };

        mouseExitedEvent = event -> {
            if(!cell.isFocused()) newNote.setVisible(false);
        };

        newNote.setOnAction(event -> {
            getChildren().add(new NoteTreeItem());
        });

    }

    public void setupGraphic(){

        pane = new HBox();
        pane.setAlignment(Pos.CENTER);
        pane.setPrefHeight(18);
        pane.setStyle("-fx-padding: -6 -6 -6 0;"); // top - right - bottom - left

        //nameText = new Text(name);
        //nameText.setFont(new Font(14));


        nameField.setBorder(null);
        nameField.setPrefRowCount(1);
        nameField.setPrefHeight(30);


        noteField.setBorder(null);
        noteField.setPrefWidth(25);

        totalField.setBorder(null);
        totalField.setPrefWidth(25);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        newNote = new Button();
        newNote.setGraphic(Builders.buildImage(getClass().getResource("/img/more.png")+"", 0, 0));
        Builders.setPosition(newNote, 0, 0, 30, 30, true);
        newNote.setVisible(false);

        pane.getChildren().addAll(nameField, noteField, new Text("/"), totalField, spacer, newNote);

    }
    public void updateGraphic(){
        nameText.setText(name);
    }

    public void updateCell(TreeCell<String> cell){

        if(cell == null) return;
        if(this.cell != null) this.cell.focusedProperty().removeListener(focusedListener);
        this.cell = cell;

        cell.setGraphic(pane);
        cell.setStyle(null);
        cell.setStyle("-fx-padding: 6 6 6 2;");
        //cell.setContextMenu(menu);
        cell.setOnMouseEntered(mouseEnteredEvent);
        cell.setOnMouseExited(mouseExitedEvent);

        cell.focusedProperty().addListener(focusedListener);

    }

    public boolean hasSubNote(){
        return getChildren().size() != 0;
    }
}
