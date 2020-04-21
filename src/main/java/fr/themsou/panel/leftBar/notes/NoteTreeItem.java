package fr.themsou.panel.leftBar.notes;

import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
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

public class NoteTreeItem extends TreeItem {

    private NoteElement core;

    private TreeCell<String> cell;
    private HBox pane;

    private Text name = new Text();
    private Text note = new Text();

    private Button newNote;
    private EventHandler<MouseEvent> mouseEnteredEvent;
    private EventHandler<MouseEvent> mouseExitedEvent;
    private ChangeListener<Boolean> focusedListener;

    private TextArea nameField = new TextArea("Nouvelle note");
    private TextArea noteField = new TextArea("5");
    private TextArea totalField = new TextArea("20");

    public NoteTreeItem(NoteElement core){

        this.core = core;

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
            Main.lbNoteTab.newNoteElementAuto(this);
        });

    }

    public void setupGraphic(){

        pane = new HBox();
        pane.setAlignment(Pos.CENTER);
        pane.setPrefHeight(18);
        pane.setStyle("-fx-padding: -6 -6 -6 0;"); // top - right - bottom - left

        // TEXTS

        name.textProperty().bind(core.nameProperty());
        name.setFont(new Font(14));

        note.textProperty().bind(Bindings.createStringBinding(() -> {
            DecimalFormat format = new DecimalFormat("0.#");
            return (core.getValue() == -1 ? "?" : format.format(core.getValue())) + "/" + format.format(core.getTotal());
        }, core.valueProperty(), core.totalProperty()));

        note.setFont(new Font(14));
        HBox.setMargin(note, new Insets(0, 5, 0, 5));

        // FIELDS

        nameField.setBorder(null);
        nameField.setPrefRowCount(1);
        nameField.setPrefHeight(10);

        noteField.setBorder(null);
        noteField.setPrefWidth(25);

        totalField.setBorder(null);
        totalField.setPrefWidth(25);

        // OTHER

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        newNote = new Button();
        newNote.setGraphic(Builders.buildImage(getClass().getResource("/img/more.png")+"", 0, 0));
        Builders.setPosition(newNote, 0, 0, 30, 30, true);
        newNote.disableProperty().bind(LBNoteTab.lockRatingScale);
        newNote.setVisible(false);

        pane.getChildren().addAll(name, spacer, note, newNote);

    }
    public void updateGraphic(){

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
