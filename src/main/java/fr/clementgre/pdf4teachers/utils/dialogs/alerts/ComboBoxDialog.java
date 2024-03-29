/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs.alerts;

import fr.clementgre.pdf4teachers.components.ScaledComboBox;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ComboBoxDialog<T> extends CustomAlert{
    
    private final Label beforeText = new Label();
    private final ScaledComboBox<T> input = new ScaledComboBox<>(false);
    
    public ComboBoxDialog(String title, String header, String details){
        super(AlertType.CONFIRMATION, title, header, null);
        
        VBox box = new VBox();
        box.setPadding(new Insets(15));
        if(details != null){
            beforeText.setText(details);
            box.setSpacing(10);
            box.getChildren().addAll(beforeText, input);
        }else{
            box.getChildren().addAll(input);
        }
        
        getDialogPane().setContent(box);
        
        addOKButton(ButtonPosition.DEFAULT);
        addCancelButton(ButtonPosition.CLOSE);
    }
    
    public void setItems(ObservableList<T> item){
        input.setItems(item);
    }
    public void setSelected(T item){
        input.getSelectionModel().select(item);
    }
    public T getSelected(){
        return input.getSelectionModel().getSelectedItem();
    }
    public ComboBox<T> getComboBox(){
        return input;
    }
    
    public T execute(){
        if(getShowAndWaitIsDefaultButton()){
            return getSelected();
        }
        return null;
    }
    
}
