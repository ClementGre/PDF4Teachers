package fr.clementgre.pdf4teachers.document.editions.undoEngine;

import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;

public class ResizeUndoAction extends MoveUndoAction{
    
    private int realWidth;
    private int realHeight;
    
    public ResizeUndoAction(UType undoType, GraphicElement element){
        super(undoType, element);
        this.realWidth = element.getRealWidth();
        this.realHeight = element.getRealHeight();
    }
    
    @Override
    public boolean undoAndInvert(){
        super.undoAndInvert();
    
        // this.element is always a GraphicElement. This is just a shortcut for check != null + cast.
        if(this.element.get() instanceof GraphicElement element){
        
            int oldRealWidth = element.getRealWidth();
            int oldRealHeight = element.getRealHeight();
        
            element.setRealWidth(realWidth);
            element.setRealHeight(realHeight);
        
            // invert
            realWidth = oldRealWidth;
            realHeight = oldRealHeight;
            return true;
        }
        return false;
    }
}
