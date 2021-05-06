package fr.clementgre.pdf4teachers.datasaving.settings;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.NodeRadioMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.MenuBar;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class IntSetting extends Setting<Integer>{
    
    private IntegerProperty value;
    
    public IntSetting(Integer value, boolean hasEditPane, String icon, String path, String title, String description){
        super(hasEditPane, icon, path, title, description);
        this.value = new SimpleIntegerProperty(value);
    }
    
    @Override
    public HBox getDefaultEditPane(){
        return new HBox(new Spinner<Integer>());
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
