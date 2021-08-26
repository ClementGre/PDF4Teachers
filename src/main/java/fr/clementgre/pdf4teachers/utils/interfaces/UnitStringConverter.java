/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.interfaces;

import javafx.util.StringConverter;

public class UnitStringConverter extends StringConverter<Double>{
    
    private final String unit;
    
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
