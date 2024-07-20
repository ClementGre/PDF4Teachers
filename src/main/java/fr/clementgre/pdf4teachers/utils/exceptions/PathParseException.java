/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.exceptions;

import org.apache.batik.parser.ParseException;

public class PathParseException extends Exception{
    
    private final ParseException batikException;
    
    public PathParseException(ParseException batikException){
        super(batikException.getMessage(), batikException.getCause());
        this.batikException = batikException;
    }
    
    public ParseException getBatikException(){
        return batikException;
    }
    
    @Override
    public String getMessage(){
        return "Unable to parse vector path [Error message: " + batikException.getMessage() + "]";
    }
}
