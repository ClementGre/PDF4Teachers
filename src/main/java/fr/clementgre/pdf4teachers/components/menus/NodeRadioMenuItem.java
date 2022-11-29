/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components.menus;

import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class NodeRadioMenuItem extends NodeMenuItem {
    
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final ImageView SELECTED_IMAGE = ImageUtils.buildImage(NodeRadioMenuItem.class.getResource("/img/MenuBar/yes.png") + "", 16, 16);
    private final ImageView NONSELECTED_IMAGE = ImageUtils.buildImage(NodeRadioMenuItem.class.getResource("/img/MenuBar/no.png") + "", 16, 16);
    
    private final boolean autoUpdate;
    
    public NodeRadioMenuItem(String text, boolean autoUpdate, boolean definitiveObject){
        this(new HBox(), text, autoUpdate, false, definitiveObject);
    }
    public NodeRadioMenuItem(String text, boolean autoUpdate, boolean hideOnClick, boolean definitiveObject){
        this(new HBox(), text, autoUpdate, hideOnClick, definitiveObject);
    }
    // When definitiveObject == true, the scale is bound to the Settings zoom property.
    // Temporary objects must not be bound to prevent leaks (It's not a binding but a listener).
    public NodeRadioMenuItem(HBox node, String text, boolean autoUpdate, boolean hideOnClick, boolean definitiveObject){
        super(node, text, hideOnClick, definitiveObject);
        this.autoUpdate = autoUpdate;
        setup();
    }
    
    private void setup(){
        
        selected.addListener((ObservableValue<? extends Boolean> observable, Boolean oldSelected, Boolean selected) -> {
            if(selected) {
                setLeftData(SELECTED_IMAGE);
            } else {
                setLeftData(NONSELECTED_IMAGE);
            }
        });
        
        if(autoUpdate){
            getNode().setOnMouseClicked((e) -> {
                setSelected(!isSelected());
            });
        }
        
        if(isSelected()) {
            setLeftData(SELECTED_IMAGE);
        } else {
            setLeftData(NONSELECTED_IMAGE);
        }
        
    }
    
    public boolean isSelected(){
        return selected.get();
    }
    
    public BooleanProperty selectedProperty(){
        return selected;
    }
    
    public void setSelected(boolean selected){
        this.selected.set(selected);
    }
}
