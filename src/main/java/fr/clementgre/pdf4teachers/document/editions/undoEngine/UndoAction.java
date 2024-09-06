/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.undoEngine;

public abstract class UndoAction{
    
    private UType undoType;
    
    public UndoAction(UType undoType){
        this.undoType = undoType;
    }
    
    // Return false if undo is impossible (link with the value was loss for example).
    public abstract boolean undoAndInvert();
    
    public abstract String toString();
    
    
    public UType getUndoType(){
        return undoType;
    }
    
    public void setUndoType(UType undoType){
        this.undoType = undoType;
    }
    
}
