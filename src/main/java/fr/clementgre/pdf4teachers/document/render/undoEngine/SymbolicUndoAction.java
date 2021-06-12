package fr.clementgre.pdf4teachers.document.render.undoEngine;

public class SymbolicUndoAction extends UndoAction{
    
    public SymbolicUndoAction(){
        super(UType.UNDO);
    }
    
    @Override
    public boolean undoAndInvert(){
        return true;
    }
}
