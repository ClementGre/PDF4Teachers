package fr.clementgre.pdf4teachers.datasaving.settings;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.HBox;

public class StringSetting extends Setting<String>{
    
    private final StringProperty value;
    
    public StringSetting(String value, boolean hasEditPane, String icon, String path, String title, String description){
        super(hasEditPane, icon, path, title, description);
        this.value = new SimpleStringProperty(value);
        
    }
    
    @Override
    public HBox getDefaultEditPane(){
        throw new RuntimeException("This is not yet implemented...");
//        TextField field = new TextField(getValue());
//        return new HBox(field);
    }
    
    public StringProperty valueProperty(){
        return value;
    }
    
    @Override
    public String getValue(){
        return value.get();
    }
    
    @Override
    public void setValue(String value){
        this.value.set(value);
    }
}
