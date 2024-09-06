/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.undoEngine;

import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.ArrayList;

public class UndoEngine {
    
    public static final KeyCombination KEY_COMB_UNDO = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination KEY_COMB_REDO = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN);
    private static final int MAX_STACK_LENGTH = 300;
    
    private ArrayList<UndoAction> undoList = new ArrayList<>();
    private ArrayList<UndoAction> redoList = new ArrayList<>();
    
    private static final boolean VERBOSE = false;
    // Prevent adding actions while reversing an action.
    private boolean isUndoingThings;
    private UType overrideUndoType = null;
    public void registerNewAction(UndoAction action){
        if(overrideUndoType != null){
            if(VERBOSE && overrideUndoType != action.getUndoType())
                Log.t("Replacing action " + action + " UType from " + action.getUndoType() + " to " + overrideUndoType);
            action.setUndoType(overrideUndoType);
        }
        if(action.getUndoType() == UType.NO_UNDO || isUndoingThings) return;
        
        if(VERBOSE) Log.t("Adding action to undo stack: " + action + " [" + action.getUndoType() + "]");
        undoList.addFirst(action);
        checkUndoStackLength();
        
        redoList.clear();
    }
    private UType getFirstUndoType(){
        if(undoList.isEmpty()) return UType.NO_UNDO;
        return undoList.getFirst().getUndoType();
    }
    private UType getFirstRedoType(){
        if(redoList.isEmpty()) return UType.NO_UNDO;
        return redoList.getFirst().getUndoType();
    }
    
    
    public void undo(){
        if(VERBOSE) Log.t("Execute UNDO (stack has " + undoList.size() + " actions)");
        if(undoList.isEmpty()) return;
        
        isUndoingThings = true;
        
        UType uType = undoList.getFirst().getUndoType();
        UType beforeType = uType.toNoCountBefore();
        UType regularType = uType.toRegular();
        UType afterType = uType.toNoCountAfter();
        
        // Undo all the before types
        while(getFirstUndoType() == beforeType) undoLastAction();
        // Undo all the regular types while completed is false
        while(getFirstUndoType() == regularType && !undoLastAction()) ;
        // Undo all the after types
        while(getFirstUndoType() == afterType) undoLastAction();
        
        checkRedoStackLength();
        isUndoingThings = false;
    }
    
    public void redo(){
        if(VERBOSE) Log.t("Execute REDO (stack has " + redoList.size() + " actions)");
        if(redoList.isEmpty()) return;
        isUndoingThings = true;
        
        UType uType = redoList.getFirst().getUndoType();
        UType beforeType = uType.toNoCountBefore();
        UType regularType = uType.toRegular();
        UType afterType = uType.toNoCountAfter();
        
        // Redo all the after types
        while(getFirstRedoType() == afterType) redoLastAction();
        // Redo all the regular types while completed is false
        while(getFirstRedoType() == regularType && !redoLastAction()) ;
        // Redo all the before types
        while(getFirstRedoType() == beforeType) redoLastAction();
        
        checkUndoStackLength();
        isUndoingThings = false;
    }
    private boolean undoLastAction(){
        UndoAction action = undoList.getFirst();
        
        if(VERBOSE) Log.t("Undoing: " + action + " [" + action.getUndoType() + "]");
        undoList.removeFirst();
        boolean completed = action.undoAndInvert();
        redoList.addFirst(action);
        
        return completed;
    }
    private boolean redoLastAction(){
        UndoAction action = redoList.getFirst();
        
        if(VERBOSE) Log.t("Redoing: " + action + " [" + action.getUndoType() + "]");
        redoList.removeFirst();
        boolean completed = action.undoAndInvert();
        undoList.addFirst(action);
        
        return completed;
    }
    
    
    public UndoAction getUndoNextAction(){
        if(!undoList.isEmpty()){
            return undoList.getFirst();
        }
        return null;
    }
    public String getUndoNextName(){
        if(!undoList.isEmpty()){
            return undoList.getFirst().toString();
        }
        return null;
    }
    public String getRedoNextName(){
        if(!redoList.isEmpty()){
            return redoList.getFirst().toString();
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
    public UType getOverrideUndoType(){
        return overrideUndoType;
    }
    public void setOverrideUndoType(UType overrideUndoType){
        this.overrideUndoType = overrideUndoType;
    }
    public boolean isUndoingThings(){
        return isUndoingThings;
    }
    public void lock(){
        setOverrideUndoType(UType.NO_UNDO);
    }
    public void unlock(){
        setOverrideUndoType(null);
    }
    public boolean isLocked(){
        return getOverrideUndoType() == UType.NO_UNDO;
    }
}
