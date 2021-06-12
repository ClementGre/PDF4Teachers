package fr.clementgre.pdf4teachers.document.editions.undoEngine;

public abstract class UndoAction{
    
    private final UType undoType;
    
    public UndoAction(UType undoType){
        this.undoType = undoType;
    }
    
    // Return false if undo is impossible (link with the value was loss for example).
    public abstract boolean undoAndInvert();
    
    public abstract String toString();
    
    
    public UType getUndoType(){
        return undoType;
    }
    
}
