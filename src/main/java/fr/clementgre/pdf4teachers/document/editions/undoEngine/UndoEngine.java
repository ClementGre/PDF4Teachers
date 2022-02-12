/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.undoEngine;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.beans.property.Property;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.ArrayList;

public class UndoEngine{
    
    public static final KeyCombination KEY_COMB_UNDO = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination KEY_COMB_REDO = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN);
    private final static int MAX_STACK_LENGTH = 200;
    
    private ArrayList<UndoAction> undoList = new ArrayList<>();
    private ArrayList<UndoAction> redoList = new ArrayList<>();
    
    // Prevent adding actions while reversing an action.
    public static boolean isUndoingThings = false;
    
    // When false, only NO_COUNT actions before the classic undo action will be processed.
    // If true, the NO_COUNT actions that are after the classic undo action, will be processed at the same time
    // This boolean is false for Pages UndoEngine
    private final boolean doProcessNoCountRightAndLeft;
    public UndoEngine(boolean doProcessNoCountRightAndLeft){
        this.doProcessNoCountRightAndLeft = doProcessNoCountRightAndLeft;
    }
    
    public void registerNewAction(UndoAction action){
        if(action.getUndoType() == UType.NO_UNDO || isUndoingThings) return;
    
        //System.out.println("Adding action to undo stack: " + action);
        undoList.add(0, action);
        checkUndoStackLength();
    
        // Since actions are not dependant each other, clearing redo stack is not necessarily necessary.
        // But, it can be easier to the user, even if he will not be able to redo after editing.
        redoList.clear();
    }
    
    public void undo(){
        //System.out.println("Execute UNDO (stack has " + undoList.size() + " actions)");
        isUndoingThings = true;
        
        while(!undoLastAction());
        
        // After having undoing last action, undo all next NO_COUNT actions
        // Do not process next NO_COUNT actions if doProcessNoCountRightAndLeft is true
        while(doProcessNoCountRightAndLeft && undoList.size() != 0 && undoList.get(0).getUndoType() == UType.NO_COUNT){
            undoLastAction();
        }
        
        checkRedoStackLength();
        isUndoingThings = false;
    }
    
    public void redo(){
        //System.out.println("Execute REDO (stack has " + redoList.size() + " actions)");
        isUndoingThings = true;
        
        // doProcessNoCountRightAndLeft has no interest in redo because the first action in the list will always be a classic action
        while(!redoLastAction());
    
        // After having undoing last action, undo all next NO_COUNT actions
        while(redoList.size() != 0 && redoList.get(0).getUndoType() == UType.NO_COUNT){
            redoLastAction();
        }
        
        checkUndoStackLength();
        isUndoingThings = false;
    }
    private boolean undoLastAction(){
        if(undoList.size() == 0) return true;
    
        UndoAction action = undoList.get(0);
        undoList.remove(0);
        boolean completed = action.undoAndInvert();
        redoList.add(0, action);
    
        return completed && action.getUndoType() == UType.UNDO;
    }
    private boolean redoLastAction(){
        if(redoList.size() == 0) return true;
    
        UndoAction action = redoList.get(0);
        redoList.remove(0);
        boolean completed = action.undoAndInvert();
        undoList.add(0, action);
    
        return completed && action.getUndoType() == UType.UNDO;
    }
    
    
    
    private boolean computeAction(ArrayList<UndoAction> redoList, ArrayList<UndoAction> undoList){
        if(redoList.size() == 0) return true;
        
        UndoAction action = redoList.get(0);
        boolean completed = action.undoAndInvert();
        undoList.add(0, action);
        redoList.remove(0);
        
        return completed && action.getUndoType() == UType.UNDO;
    }
    public UndoAction getUndoNextAction(){
        if(undoList.size() > 0){
            return undoList.get(0);
        }
        return null;
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
            undoList = new ArrayList<>(undoList.stream().limit(MAX_STACK_LENGTH).toList());
        }
    }
    private void checkRedoStackLength(){
        if(redoList.size() > MAX_STACK_LENGTH){
            redoList = new ArrayList<>(redoList.stream().limit(MAX_STACK_LENGTH).toList());
        }
    }
}
