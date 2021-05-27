package fr.clementgre.pdf4teachers.datasaving.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ToggleSwitch;

public class BooleanSetting extends Setting<Boolean>{
    
    private final BooleanProperty value;
    
    public BooleanSetting(Boolean value, boolean hasEditPane, String icon, String path, String title, String description){
        super(hasEditPane, icon, path, title, description);
        this.value = new SimpleBooleanProperty(value);
    }
    
    @Override
    public HBox getDefaultEditPane(){
        ToggleSwitch toggle = new ToggleSwitch();
        
        toggle.setSelected(getValue());
        toggle.selectedProperty().addListener((observable, oldValue, newValue) -> setValue(newValue));
        
        return new HBox(toggle);
    }
    
    public BooleanProperty valueProperty(){
        return value;
    }
    
    @Override
    public Boolean getValue(){
        return value.get();
    }
    
    @Override
    public void setValue(Boolean value){
        this.value.setValue(value);
    }
}
