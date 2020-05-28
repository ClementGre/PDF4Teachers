package fr.themsou.utils.sort;

import fr.themsou.utils.Builders;
import fr.themsou.utils.style.StyleManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.HashMap;
import java.util.Map;

public class SortManager {

    private HashMap<Button, BooleanProperty> buttons = new HashMap<>();
    private ObjectPropertyBase<Button> selectedButton = new SimpleObjectProperty<>();

    public String selectedColor = "#0078d7";

    private SortEvent updateSort;

    public SortManager(SortEvent updateSort, String selectedColor){
        if(selectedColor != null) this.selectedColor = selectedColor;
        this.updateSort = updateSort;
    }

    public void setup(GridPane parent, String selectedButtonName, String... buttonsName){

        int row = 0;
        for(String buttonName : buttonsName){

            if(buttonName.equals("\n")){
                row++; continue;
            }

            Button button = new Button(buttonName);
            button.setGraphic(Builders.buildImage(getClass().getResource("/img/Sort/up.png")+"", 0, 0));
            button.setAlignment(Pos.CENTER_LEFT);
            button.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(button, Priority.ALWAYS);
            BooleanProperty order = new SimpleBooleanProperty(true);
            buttons.put(button, order);
            parent.addRow(row, button);

            if(selectedButtonName.equals(buttonName)){
                selectedButton.set(button);
                button.setStyle("-fx-background-color: " + selectedColor + ";");
            }else button.setStyle("-fx-background-color: " + StyleManager.getHexAccentColor() + ";");

            // Image de l'ordre
            order.addListener(new ChangeListener<>() {
                @Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean lastOrder, Boolean newOrder) {
                    button.setGraphic(Builders.buildImage(getClass().getResource(newOrder ? "/img/Sort/up.png" : "/img/Sort/down.png") + "", 0, 0));
                }
            });

            // Change selectedButton lors du clic ET update l'ordre
            button.setOnAction(actionEvent -> {
                if(selectedButton.get() == button){
                    order.set(!order.get());
                    updateSort.call(button.getText(), order.get());
                }else selectedButton.set(button);
            });
        }
        if(selectedButton.get() == null){
            selectedButton.set(buttons.keySet().iterator().next());
            buttons.keySet().iterator().next().setStyle("-fx-background-color: " + selectedColor);
        }

        // Couleurs des boutons
        selectedButton.addListener((observableValue, lastSelected, newSelected) -> {
            lastSelected.setStyle("-fx-background-color: " + StyleManager.getHexAccentColor() + ";");
            newSelected.setStyle("-fx-background-color: " + selectedColor + ";");
            updateSort.call(newSelected.getText(), buttons.get(newSelected).get());
        });
    }
    public void updateGraphics(){
        for(Button button : buttons.keySet())
            if(button != selectedButton.get()) button.setStyle("-fx-background-color: " + StyleManager.getHexAccentColor() + ";");
    }

    public void simulateCall(){
        updateSort.call(selectedButton.get().getText(), buttons.get(selectedButton.get()).get());
    }

    public Button getSelectedButton() {
        return selectedButton.get();
    }
}