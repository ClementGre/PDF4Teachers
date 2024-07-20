/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.interfaces;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.util.StringConverter;

public class StringToDoubleConverter extends StringConverter<Double>{
    
    private double lastValue;
    private final boolean twoDecimals;
    
    public StringToDoubleConverter(double defaultValue){
        this.lastValue = defaultValue;
        this.twoDecimals = false;
    }
    public StringToDoubleConverter(double defaultValue, boolean twoDecimals){
        this.lastValue = defaultValue;
        this.twoDecimals = twoDecimals;
    }
    
    @Override
    public String toString(Double value){
        lastValue = value;
        if(twoDecimals) return MainWindow.twoDigFormat.format(value);
        return MainWindow.fourDigFormat.format(value);
    }
    
    @Override
    public Double fromString(String string){
        try{
            lastValue = Double.parseDouble(string.replace(",", "."));
            return lastValue;
        }catch(NumberFormatException e){
            return lastValue;
        }
    }
    
}
