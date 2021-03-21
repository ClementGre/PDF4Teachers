package fr.clementgre.pdf4teachers.utils.interfaces;

import javafx.util.StringConverter;

public class StringToIntConverter extends StringConverter<Integer>{
    
    private int lastValue;
    
    public StringToIntConverter(int defaultValue){
        lastValue = defaultValue;
    }
    
    @Override
    public String toString(Integer integer){
        lastValue = integer;
        return integer.toString();
    }
    
    @Override
    public Integer fromString(String string){
        try{
            lastValue = Integer.parseInt(string);
            return lastValue;
        }catch(NumberFormatException e){
            return lastValue;
        }
    }
    
}
