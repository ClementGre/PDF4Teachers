package fr.clementgre.pdf4teachers.document.editions.undoEngine;

import java.util.ArrayList;

public class UndoEngine{
    
    private final static int MAX_STACK_LENGTH = 200;
    
    private final ArrayList<UndoAction> undoList = new ArrayList<>();
    private final ArrayList<UndoAction> redoList = new ArrayList<>();
    
    public void registerNewAction(UndoAction action){
        if(action.getUndoType() == UType.NO_UNDO) return;
        
        undoList.add(0, action);
        checkUndoStackLength();
    
        // Since actions are not dependant each other, clearing redo stack is not necessarily necessary.
        // But, it can be easier to the user, even if he will not be able to redo after editing.
        redoList.clear();
        
    }
    
    public void undo(){
        System.out.println("Execute UNDO (stack has " + undoList.size() + " actions)");
        if(undoList.size() > 0){
            undoList.get(0).undoAndInvert();
            redoList.add(0, undoList.get(0));
            checkRedoStackLength();
            undoList.remove(0);
        }
    }
    public void redo(){
        System.out.println("Execute REDO (stack has " + redoList.size() + " actions)");
        if(redoList.size() > 0){
            redoList.get(0).undoAndInvert();
            undoList.add(0, redoList.get(0));
            checkUndoStackLength();
            redoList.remove(0);
        }
    }
    
    public String getUndoNextName(){
        if(undoList.size() > 0){
            return undoList.get(0).toString();
        }
        return null;
    }
    public String getRedoNextName(){
        if(redoList.size() > 0){
            return redoList.get(0).toString();
        }
        return null;
    }
    
    private void checkUndoStackLength(){
        if(undoList.size() > MAX_STACK_LENGTH){
            undoList.clear();
            undoList.addAll(undoList.subList(0, MAX_STACK_LENGTH));
        }
    }
    private void checkRedoStackLength(){
        if(redoList.size() > MAX_STACK_LENGTH){
            redoList.clear();
            redoList.addAll(redoList.subList(0, MAX_STACK_LENGTH));
        }
    }
}
