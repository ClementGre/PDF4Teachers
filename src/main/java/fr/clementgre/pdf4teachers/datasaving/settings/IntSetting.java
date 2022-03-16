/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.datasaving.settings;

import fr.clementgre.pdf4teachers.components.SliderWithoutPopup;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToIntConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
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
            
            slider.setPrefWidth(90);
            slider.setMinWidth(90);
            root.getChildren().add(slider);
            
            Label valueDisplay = new Label(getValueOrStep()+"");
            valueDisplay.setMinWidth(25);
            valueDisplay.setStyle("-fx-wrap-text: false;");
            valueDisplay.setTextOverrun(OverrunStyle.CLIP);
            valueDisplay.textProperty().bind(Bindings.createStringBinding(() -> getValueOrStep()+"", valueProperty()));
            if(disableInMinus1) valueDisplay.disableProperty().bind(valueProperty().isEqualTo(-1));
            root.getChildren().setAll(valueDisplay, slider);
        }else{
            spinner = new Spinner<>(min, max, getValueOrStep());
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(!spinner.isDisable()) setValue(newValue);
            });
            spinner.setEditable(true);
            spinner.getValueFactory().setConverter(new StringToIntConverter(getValueOrStep()));
            ((SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory()).setAmountToStepBy(step);
            
            spinner.setMinWidth(90);
            spinner.setPrefWidth(90);
            root.getChildren().setAll(spinner);
        }
        
        if(disableInMinus1){
            toggle = new ToggleSwitch();
            toggle.setSelected(getValue() != -1);
            ChangeListener<Boolean> selectedListener = (observable, oldValue, newValue) -> {
                setValue(-1);
                if(newValue){
                    if(slider != null) setValue((int) slider.getValue());
                    else if(spinner != null) setValue(spinner.getValue());
                }
                
                if(slider != null) slider.setDisable(!newValue);
                else if(spinner != null) spinner.setDisable(!newValue);
            };
            selectedListener.changed(null, getValue() == -1, getValue() != -1);
            toggle.selectedProperty().addListener(selectedListener);
            
            root.getChildren().add(toggle);
        }
        
        return root;
    }
    
    public int getValueOrStep(){
        if(getValue() == -1 && disableInMinus1){
            if(slider != null && slider.getValue() != -1) return (int) slider.getValue(); // For slider, return selected value even when disabled
            return step;
        }
        return getValue();
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
