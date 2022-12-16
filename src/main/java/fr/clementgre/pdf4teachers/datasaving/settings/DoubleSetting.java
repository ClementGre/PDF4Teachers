/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.datasaving.settings;

import fr.clementgre.pdf4teachers.components.SliderWithoutPopup;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToDoubleConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ToggleSwitch;

public class DoubleSetting extends Setting<Double> {
    
    private final DoubleProperty value;
    
    private final double min;
    private final double max;
    private final double step;
    private final boolean disableInMinus1;
    private final boolean hasSlider;
    
    public DoubleSetting(Double value, boolean hasEditPane, double min, double max, double step, boolean disableInMinus1, boolean hasSlider, String icon, String path, String title, String description){
        super(hasEditPane, icon, path, title, description);
        this.value = new SimpleDoubleProperty(value);
        this.min = min;
        this.max = max;
        this.step = step;
        this.disableInMinus1 = disableInMinus1;
        this.hasSlider = hasSlider;
    }
    
    private ToggleSwitch toggle;
    private SliderWithoutPopup slider;
    private Spinner<Double> spinner;
    @Override
    public HBox getDefaultEditPane(){
        HBox root = new HBox();
        
        if(hasSlider){
            slider = new SliderWithoutPopup(min, max, getValueOrStep());
            slider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(!slider.isDisable()) setValue(newValue.doubleValue());
            });
            slider.setMinorTickCount(0);
            slider.setMajorTickUnit(step);
            
            slider.setPrefWidth(90);
            slider.setMinWidth(90);
            
            Label valueDisplay = new Label(getValueOrStep()+"");
            valueDisplay.setMinWidth(25);
            valueDisplay.setStyle("-fx-wrap-text: false;");
            valueDisplay.setTextOverrun(OverrunStyle.CLIP);
            valueDisplay.textProperty().bind(Bindings.createStringBinding(() -> getValueOrStep()+"", valueProperty()));
            if(disableInMinus1) valueDisplay.disableProperty().bind(valueProperty().isEqualTo(-1));
            root.getChildren().addAll(valueDisplay, slider);
        }else{
            spinner = new Spinner<>(min, max, getValueOrStep());
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(!spinner.isDisable()) setValue(newValue);
            });
            spinner.setEditable(true);
            spinner.getValueFactory().setConverter(new StringToDoubleConverter(getValueOrStep()));
            ((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner.getValueFactory()).setAmountToStepBy(step);
            
            spinner.setPrefWidth(90);
            spinner.setMinWidth(90);
            root.getChildren().addAll(spinner);
        }
        
        if(disableInMinus1){
            toggle = new ToggleSwitch();
            toggle.setSelected(getValue() != -1);
            ChangeListener<Boolean> selectedListener = (observable, oldValue, newValue) -> {
                setValue(-1d); // force update
                if(newValue){
                    if(slider != null) setValue(slider.getValue());
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
    
    public double getValueOrStep(){
        if(getValue() == -1 && disableInMinus1){
            if(slider != null && slider.getValue() != -1) return slider.getValue(); // For slider, return selected value even when disabled
            return step;
        }
        return getValue();
    }
    
    public DoubleProperty valueProperty(){
        return value;
    }
    
    @Override
    public Double getValue(){
        return value.get();
    }
    
    @Override
    public void setValue(Double value){
        this.value.setValue(value);
    }
    
}
