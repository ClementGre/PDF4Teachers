/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.datasaving.settings;

import fr.clementgre.pdf4teachers.components.SliderWithoutPopup;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToIntConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ToggleSwitch;

public class IntSetting extends Setting<Integer> {
    
    private final IntegerProperty value;
    
    private final int min;
    private final int max;
    private final int step;
    private final boolean disableInMinus1;
    private final boolean hasSlider;
    
    public IntSetting(Integer value, boolean hasEditPane, int min, int max, int step, boolean disableInMinus1, boolean hasSlider, String icon, String path, String title, String description){
        super(hasEditPane, icon, path, title, description);
        this.value = new SimpleIntegerProperty(value);
        this.min = min;
        this.max = max;
        this.step = step;
        this.disableInMinus1 = disableInMinus1;
        this.hasSlider = hasSlider;
    }
    
    private ToggleSwitch toggle;
    private SliderWithoutPopup slider = null;
    private Spinner<Integer> spinner = null;
    @Override
    public HBox getDefaultEditPane(){
        HBox root = new HBox();
        
        if(hasSlider){
            slider = new SliderWithoutPopup(min, max, getValue() == -1 ? step : getValue());
            slider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(!slider.isDisable()) setValue(newValue.intValue());
            });
            slider.setMinorTickCount(0);
            slider.setMajorTickUnit(step);
            
            slider.setPrefWidth(80);
            slider.setMinWidth(80);
            root.getChildren().add(slider);
            
            Label valueDisplay = new Label(getValueOrEmpty());
            valueDisplay.textProperty().bind(Bindings.createStringBinding(this::getValueOrEmpty, valueProperty()));
            if(disableInMinus1) valueDisplay.visibleProperty().bind(valueProperty().isNotEqualTo(-1));
            root.getChildren().addAll(valueDisplay, slider);
        }else{
            spinner = new Spinner<>(min, max, getValueOrStep());
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(!spinner.isDisable()) setValue(newValue);
            });
            spinner.setEditable(true);
            spinner.getValueFactory().setConverter(new StringToIntConverter(getValueOrStep()));
            ((SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory()).setAmountToStepBy(step);
            
            spinner.setMinWidth(80);
            spinner.setPrefWidth(80);
            root.getChildren().addAll(spinner);
        }
        
        if(disableInMinus1){
            toggle = new ToggleSwitch();
            toggle.setSelected(getValue() != -1);
            ChangeListener<Boolean> listener = (observable, oldValue, newValue) -> {
                if(!newValue) setValue(-1);
                
                if(slider != null) slider.setDisable(!newValue);
                else if(spinner != null) spinner.setDisable(!newValue);
            };
            listener.changed(null, getValue() == -1, getValue() != -1);
            toggle.selectedProperty().addListener(listener);
            
            root.getChildren().add(toggle);
        }
        
        return root;
    }
    
    public int getValueOrStep(){
        if(getValue() == -1 && disableInMinus1) return step;
        else return getValue();
    }
    public String getValueOrEmpty(){
        if(getValue() == -1 && disableInMinus1) return "";
        else return MainWindow.fourDigFormat.format(getValue());
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
}
