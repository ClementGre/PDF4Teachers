package fr.clementgre.pdf4teachers.document.editions;

import fr.clementgre.pdf4teachers.document.render.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.undoEngine.UndoAction;

import java.util.ArrayList;

public class UndoEngine{
    
    private ArrayList<UndoAction> undoList = new ArrayList<>();
    private ArrayList<UndoAction> redoList = new ArrayList<>();
    
    public void registerNewAction(UndoAction action){
        if(action.getUndoType() == UType.NO_UNDO) return;
        
        undoList.add(0, action);
    }
    
    public void undo(){
        System.out.println("__UNDO__");
        if(undoList.size() > 0){
            undoList.get(0).undoAndInvert();
            redoList.add(0, undoList.get(0));
            undoList.remove(0);
        }
    }
    public void redo(){
        System.out.println("__REDO__");
        if(redoList.size() > 0){
            redoList.get(0).undoAndInvert();
            undoList.add(0, redoList.get(0));
            redoList.remove(0);
        }
    }
    
}
