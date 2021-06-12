package fr.clementgre.pdf4teachers.document.render.undoEngine;

public abstract class UndoAction{
    
    private UType undoType;
    
    public UndoAction(UType undoType){
        this.undoType = undoType;
    }
    
    // Return false if undo is impossible (link with the value was loss for example).
    public abstract boolean undoAndInvert();
    
    
    public UType getUndoType(){
        return undoType;
    }
    
}
