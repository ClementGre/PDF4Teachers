package fr.clementgre.pdf4teachers.utils.interfaces;

import javafx.util.StringConverter;

public class StringToDoubleConverter extends StringConverter<Double>{
    
    private double lastValue;
    
    public StringToDoubleConverter(double defaultValue){
        lastValue = defaultValue;
    }
    
    @Override
    public String toString(Double integer){
        lastValue = integer;
        return integer.toString();
    }
    
    @Override
    public Double fromString(String string){
        try{
            lastValue = Double.parseDouble(string);
            return lastValue;
        }catch(NumberFormatException e){
            return lastValue;
        }
    }
    
}
