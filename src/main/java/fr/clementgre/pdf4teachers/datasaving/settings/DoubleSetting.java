package fr.clementgre.pdf4teachers.datasaving.settings;

import fr.clementgre.pdf4teachers.components.SliderWithoutPopup;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToDoubleConverter;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToIntConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ToggleSwitch;

public class DoubleSetting extends Setting<Double>{
    
    private DoubleProperty value;
    
    private double min;
    private double max;
    private double step;
    private boolean disableInMinus1;
    private boolean hasSlider;
    
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
    private SliderWithoutPopup slider = null;
    private Spinner<Double> spinner = null;
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
            
            slider.setMaxWidth(80);
            
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
            spinner.getValueFactory().setConverter(new StringToDoubleConverter(getValueOrStep()));
            ((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner.getValueFactory()).setAmountToStepBy(step);
            
            spinner.setMaxWidth(80);
            root.getChildren().addAll(spinner);
        }
        
        if(disableInMinus1){
            toggle = new ToggleSwitch();
            toggle.setSelected(getValue() != -1);
            ChangeListener<Boolean> listener = (observable, oldValue, newValue) -> {
                if(!newValue) setValue(-1d);
                
                if(slider != null) slider.setDisable(!newValue);
                else if(spinner != null) spinner.setDisable(!newValue);
            };
            listener.changed(null, getValue() == -1, getValue() != -1);
            toggle.selectedProperty().addListener(listener);
            
            root.getChildren().add(toggle);
        }
        
        return root;
    }
    
    public double getValueOrStep(){
        if(getValue() == -1 && disableInMinus1) return step;
        else return getValue();
    }
    public String getValueOrEmpty(){
        if(getValue() == -1 && disableInMinus1) return "";
        else return MainWindow.twoDigFormat.format(getValue());
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
