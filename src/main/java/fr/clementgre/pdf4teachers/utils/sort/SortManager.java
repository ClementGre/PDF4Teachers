package fr.clementgre.pdf4teachers.utils.sort;

import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.HashMap;

public class SortManager{

    private HashMap<Button, BooleanProperty> buttons = new HashMap<>();
    private ObjectPropertyBase<Button> selectedButton = new SimpleObjectProperty<>();

    public String selectedColor = "#0078d7";
    private final String BUTTON_STYLE = "-fx-padding: 0 5";

    private final SortEvent updateSort;

    public SortManager(SortEvent updateSort, String selectedColor){
        if(selectedColor != null) this.selectedColor = selectedColor;
        this.updateSort = updateSort;
    }

    public void setup(GridPane parent, String selectedButtonName, String... buttonsName){

        int row = 0;
        for(String buttonName : buttonsName){

            if(buttonName.equals("\n")){
                row++;
                continue;
            }

            Button button = new Button(buttonName);
            button.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/Sort/up.png") + "", 0, 0, ImageUtils.defaultFullDarkColorAdjust));
            button.setAlignment(Pos.CENTER_LEFT);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setPrefHeight(26);
            GridPane.setHgrow(button, Priority.ALWAYS);
            BooleanProperty order = new SimpleBooleanProperty(true);
            buttons.put(button, order);
            parent.addRow(row, button);

            if(selectedButtonName.equals(buttonName)){
                selectedButton.set(button);
                button.setStyle(BUTTON_STYLE + "; -fx-background-color: " + selectedColor + ";");
            }else button.setStyle(BUTTON_STYLE + "; -fx-background-color: " + StyleManager.getHexAccentColor() + ";");

            // Image de l'ordre
            order.addListener(new ChangeListener<>(){
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean lastOrder, Boolean newOrder){
                    button.setGraphic(ImageUtils.buildImage(getClass().getResource(newOrder ? "/img/Sort/up.png" : "/img/Sort/down.png") + "", 0, 0, ImageUtils.defaultFullDarkColorAdjust));
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
            buttons.keySet().iterator().next().setStyle(BUTTON_STYLE + "; -fx-background-color: " + selectedColor);
        }

        // Couleurs des boutons
        selectedButton.addListener((observableValue, lastSelected, newSelected) -> {
            lastSelected.setStyle(BUTTON_STYLE + "; -fx-background-color: " + StyleManager.getHexAccentColor() + ";");
            newSelected.setStyle(BUTTON_STYLE + "; -fx-background-color: " + selectedColor + ";");
            updateSort.call(newSelected.getText(), buttons.get(newSelected).get());
        });
    }

    public void updateGraphics(){
        for(Button button : buttons.keySet())
            if(button != selectedButton.get())
                button.setStyle("-fx-background-color: " + StyleManager.getHexAccentColor() + ";");
    }

    public void simulateCall(){
        updateSort.call(selectedButton.get().getText(), buttons.get(selectedButton.get()).get());
    }
    public String getSortKey(){
        return selectedButton.get().getText();
    }
    public Button getSelectedButton(){
        return selectedButton.get();
    }
}