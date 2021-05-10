package fr.clementgre.pdf4teachers.utils.interfaces;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.util.StringConverter;

public class StringToDoubleConverter extends StringConverter<Double>{
    
    private double lastValue;
    
    public StringToDoubleConverter(double defaultValue){
        lastValue = defaultValue;
    }
    
    @Override
    public String toString(Double value){
        lastValue = value;
        return MainWindow.fourDigFormat.format(value);
    }
    
    @Override
    public Double fromString(String string){
        try{
            lastValue = Double.parseDouble(string.replace(",", "."));
            return lastValue;
        }catch(NumberFormatException e){
            e.printStackTrace();
            return lastValue;
        }
    }
    
}
