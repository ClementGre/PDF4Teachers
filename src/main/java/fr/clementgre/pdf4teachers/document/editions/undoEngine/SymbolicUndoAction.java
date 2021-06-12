package fr.clementgre.pdf4teachers.document.editions.undoEngine;

public class SymbolicUndoAction extends UndoAction{
    
    public SymbolicUndoAction(){
        super(UType.UNDO);
    }
    
    @Override
    public boolean undoAndInvert(){
        return true;
    }
}
