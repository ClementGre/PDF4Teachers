/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.datasaving.settings;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;


public class ToggleGroupSetting extends Setting<Integer> {
    
    private final IntegerProperty value;
    private final Map<Integer, String> valuesNames;
    private final boolean zeroAsNothing;
    
    public ToggleGroupSetting(int value, Map<Integer, String> valuesNames, boolean zeroAsNothing, boolean hasEditPane, String icon, String path, String title, String description){
        super(hasEditPane, icon, path, title, description);
        this.value = new SimpleIntegerProperty(value);
        this.valuesNames = valuesNames;
        this.zeroAsNothing = zeroAsNothing;
    }
    
    @Override
    public HBox getDefaultEditPane(){
        HBox box = new HBox();
        ToggleGroup group = new ToggleGroup();
        
        valuesNames.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach(valueName -> {
            
            ToggleButton toggle = new ToggleButton(valueName.getValue());
            if(Objects.equals(getValue(), valueName.getKey())) {
                toggle.setSelected(true);
            }
            toggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    setValue(valueName.getKey());
                }
            });
            toggle.setToggleGroup(group);
            box.getChildren().add(toggle);
            
        });
        
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null){
                if(zeroAsNothing) {
                    setValue(0);
                } else{
                    // Select a toggle that is not the one disabled
                    if(group.getToggles().get(0) != oldValue) {
                        group.getToggles().get(0).setSelected(true);
                    } else {
                        group.getToggles().get(1).setSelected(true);
                    }
                }
            }
        });
        
        return new HBox(box);
    }
    
    public IntegerProperty valueProperty(){
        return value;
    }
    
    @Override
    public Integer getValue(){
        return value.get();
    }
    
    @Override
    public void setValue(Integer value){
        this.value.setValue(value);
    }
    
    public Map<Integer, String> getValuesNames(){
        return valuesNames;
    }
}
