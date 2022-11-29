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
import fr.clementgre.pdf4teachers.utils.MathUtils;

import java.lang.ref.WeakReference;

public class PageMoveUndoAction extends UndoAction {
    
    private final WeakReference<PageRenderer> page;
    // Last page index
    private int index;
    
    public PageMoveUndoAction(UType undoType, PageRenderer page, int index){
        super(undoType);
        this.page = new WeakReference<>(page);
        this.index = index;
    }
    @Override
    public boolean undoAndInvert(){
        PageRenderer page = this.page.get();
        if(page != null && MainWindow.mainScreen.hasDocument(false)){
            
            // invert
            int oldIndex = index;
            index = page.getPage();
            
            if(oldIndex == index) {
                return false;
            }
            
            MainWindow.mainScreen.document.pdfPagesRender.editor.movePageByIndex(page, MathUtils.clamp(oldIndex, 0, MainWindow.mainScreen.document.getPagesNumber()-1));
            return true;
        }
        return false;
    }
    
    @Override
    public String toString(){
        if(page.get() == null) {
            return null;
        }
        
        return TR.tr("actions.movePage", page.get().getPage()+1);
    }
}