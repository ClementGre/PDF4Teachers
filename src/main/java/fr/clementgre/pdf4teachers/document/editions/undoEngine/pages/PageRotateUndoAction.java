/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.undoEngine.pages;

import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UndoAction;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;

import java.lang.ref.WeakReference;

public class PageRotateUndoAction extends UndoAction {
    
    private final WeakReference<PageRenderer> page;
    // The last rotation type that has been processed, can just be inverted to do the opposite action.
    private boolean rightRotation;
    
    public PageRotateUndoAction(UType undoType, PageRenderer page, boolean rightRotation){
        super(undoType);
        this.page = new WeakReference<>(page);
        this.rightRotation = rightRotation;
    }
    @Override
    public boolean undoAndInvert(){
        PageRenderer page = this.page.get();
        if(page != null && MainWindow.mainScreen.hasDocument(false)){
    
            rightRotation = !rightRotation;
            
            MainWindow.mainScreen.document.pdfPagesRender.editor.rotatePage(page, rightRotation, UType.NO_UNDO, true);
            return true;
        }
        return false;
    }
    
    @Override
    public String toString(){
        if(page.get() == null) return null;
    
        return TR.tr("actions.rotatePage", page.get().getPage()+1);
    }
}
