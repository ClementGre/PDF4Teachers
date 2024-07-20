/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.undoEngine;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;

public class ResizeUndoAction extends MoveUndoAction{
    
    private int realWidth;
    private int realHeight;
    
    public ResizeUndoAction(UType undoType, GraphicElement element){
        super(undoType, element);
        realWidth = element.getRealWidth();
        realHeight = element.getRealHeight();
    }
    
    @Override
    public boolean undoAndInvert(){
        super.undoAndInvert();
    
        // this.element is always a GraphicElement. This is just a shortcut for check != null + cast.
        if(this.element.get() instanceof GraphicElement element){
        
            // Quit edit mode if enabled
            if(this.element.get() instanceof VectorElement vectorElement && vectorElement.isEditMode()) vectorElement.quitEditMode();
            
            int oldRealWidth = element.getRealWidth();
            int oldRealHeight = element.getRealHeight();
        
            element.setRealWidth(realWidth);
            element.setRealHeight(realHeight);
        
            // invert
            realWidth = oldRealWidth;
            realHeight = oldRealHeight;
    
            Edition.setUnsave("ResizeUndoAction");
            return true;
        }
        return false;
    }
    
    @Override
    public String toString(){
        if(element.get() == null) return null;
        
        return TR.tr("actions.resize") + " " + element.get().getElementName(false).toLowerCase();
    }
}
