/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.undoEngine;

public enum UType{
    // Regular actions are undone one by one, but can be followed/preceded by NO_COUNT actions.
    ELEMENT,
    PAGE,
    // NO_COUNT actions are executed BEFORE a regular action.
    ELEMENT_NO_COUNT_BEFORE,
    PAGE_NO_COUNT_BEFORE,
    // NO_COUNT_AFTER actions are executed AFTER a regular action.
    ELEMENT_NO_COUNT_AFTER,
    PAGE_NO_COUNT_AFTER,
    // Ignored action that will not even exist in the undo stack.
    NO_UNDO;
    
    public boolean isPage(){
        return this == PAGE || this == PAGE_NO_COUNT_BEFORE || this == PAGE_NO_COUNT_AFTER;
    }
    public boolean isElement(){
        return this == ELEMENT || this == ELEMENT_NO_COUNT_BEFORE || this == ELEMENT_NO_COUNT_AFTER;
    }
    public boolean isNoCountBefore(){
        return this == ELEMENT_NO_COUNT_BEFORE || this == PAGE_NO_COUNT_BEFORE;
    }
    public boolean isNoCountAfter(){
        return this == ELEMENT_NO_COUNT_AFTER || this == PAGE_NO_COUNT_AFTER;
    }
    
    public UType toNoCountBefore(){
        if(this.isElement()) return ELEMENT_NO_COUNT_BEFORE;
        if(this.isPage()) return PAGE_NO_COUNT_BEFORE;
        return NO_UNDO;
    }
    public UType toNoCountAfter(){
        if(this.isElement()) return ELEMENT_NO_COUNT_AFTER;
        if(this.isPage()) return PAGE_NO_COUNT_AFTER;
        return NO_UNDO;
    }
    public UType toRegular(){
        if(this.isElement()) return ELEMENT;
        if(this.isPage()) return PAGE;
        return NO_UNDO;
    }
}
