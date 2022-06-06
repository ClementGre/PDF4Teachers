/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components.autocompletiontextfield;

import fr.clementgre.pdf4teachers.Main;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class AutoCompletePopupSkin<T> implements Skin<AutoCompletePopup<T>> {
    
    private final AutoCompletePopup<T> control;
    private final ListView<T> suggestionList;
    
    public AutoCompletePopupSkin(AutoCompletePopup<T> control) {
        this(control, control.getConverter());
    }
 
    public AutoCompletePopupSkin(AutoCompletePopup<T> control, StringConverter<T> displayConverter) {
        this.control = control;
        suggestionList = new ListView<>(control.getSuggestions());
        
        suggestionList.getStyleClass().add(AutoCompletePopup.DEFAULT_STYLE_CLASS);
    
        DoubleProperty cellHeight = new SimpleDoubleProperty(20);
        suggestionList.setCellFactory(new Callback<>() {
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
                            setText(displayConverter.toString(item));
                            setStyle("-fx-font-size: " + 12 * Main.settings.zoom.getValue());
                            if(getHeight() > 0) cellHeight.set(getHeight()+2);
                        }
                    }
                };
            }
        });
        
        /*suggestionList.getStylesheets().add(org.controlsfx.control.textfield.AutoCompletionBinding.class
                .getResource("autocompletion.css").toExternalForm()); //$NON-NLS-1$*/
        /**
         * Here we bind the prefHeightProperty to the minimum height between the
         * max visible rows and the current items list. We also add an arbitrary
         * 5 number because when we have only one item we have the vertical
         * scrollBar showing for no reason.
         */
        suggestionList.prefHeightProperty().bind(
                Bindings.min(control.visibleRowCountProperty(), Bindings.size(suggestionList.getItems()))
                        .multiply(cellHeight).add(15));
        
        
        //Allowing the user to control ListView width.
        suggestionList.prefWidthProperty().bind(control.prefWidthProperty());
        suggestionList.maxWidthProperty().bind(control.maxWidthProperty());
        suggestionList.minWidthProperty().bind(control.minWidthProperty());
        registerEventListener();
    
        
        // JMetro
        //StyleManager.putStyle(suggestionList, Style.DEFAULT);
    }
    
    private void registerEventListener(){
        suggestionList.setOnMouseClicked(me -> {
            if (me.getButton() == MouseButton.PRIMARY){
                onSuggestionChoosen(suggestionList.getSelectionModel().getSelectedItem());
            }
        });
        
        
        suggestionList.setOnKeyPressed(ke -> {
            switch (ke.getCode()) {
                case TAB:
                case ENTER:
                    onSuggestionChoosen(suggestionList.getSelectionModel().getSelectedItem());
                    break;
                case ESCAPE:
                    if (control.isHideOnEscape()) {
                        control.hide();
                    }
                    break;
                default:
                    break;
            }
        });
    }
    
    private void onSuggestionChoosen(T suggestion){
        if(suggestion != null) {
            Event.fireEvent(control, new AutoCompletePopup.SuggestionEvent<>(suggestion));
        }
    }
    
    
    @Override
    public Node getNode() {
        return suggestionList;
    }
    
    @Override
    public AutoCompletePopup<T> getSkinnable() {
        return control;
    }
    
    @Override
    public void dispose() {
    }
}

