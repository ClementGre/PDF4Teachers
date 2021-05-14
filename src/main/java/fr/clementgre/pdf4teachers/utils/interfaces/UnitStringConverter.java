package fr.clementgre.pdf4teachers.utils.interfaces;

import javafx.util.StringConverter;

public class UnitStringConverter extends StringConverter<Double>{
    
    private String unit;
    
    public UnitStringConverter(String unit){
        this.unit = unit;
    }
    
    @Override
    public String toString(Double object){
        return object.toString() + " " + unit;
    }
    
    @Override
    public Double fromString(String string){
        return Double.valueOf(string);
    }
}
