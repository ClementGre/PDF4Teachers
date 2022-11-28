/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components;

import fr.clementgre.pdf4teachers.Main;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import org.controlsfx.control.SearchableComboBox;

public class ScaledSearchableComboBox<T> extends SearchableComboBox<T> {
    
    // Bind by default
    public ScaledSearchableComboBox(){
        setup(true);
    }
    
    public ScaledSearchableComboBox(boolean bind){
        setup(bind);
    }
    
    public ScaledSearchableComboBox(ObservableList<T> items, boolean bind){
        super(items);
        setup(bind);
    }
    
    private void setup(boolean bind){
    
        // Remove border of children (inner combobox & textfield)
        getChildrenUnmodifiable().addListener((ListChangeListener<Node>) c -> {
            while(c.next()){
                c.getAddedSubList().forEach(n -> n.setStyle("-fx-background-insets: 0, 0, 0; -fx-padding: 0"));
            }
        });
        
        
        if(bind){
            // TODO: fix dynamic upscale issues.
            Main.settings.zoom.valueProperty()
                    .addListener((o, oldValue, newValue) -> setStyle("-fx-font-size: " + 12 * Main.settings.zoom.getValue()));
        }
        
        setCellFactory(new Callback<>() {
            @Override
            public ListCell<T> call(ListView<T> param){
                return new ListCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty){
                        super.updateItem(item, empty);
                        if(empty || item == null){
                            setText(null);
                            setStyle(null);
                        }else{
                            setText(getConverter().toString(item));
                            setStyle("-fx-font-size: " + 12 * Main.settings.zoom.getValue());
                        }
                    }
                };
            }
        });
    }
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new ScaledSearchableComboBoxSkin<>(this);
    }
}
