/*
 * Copyright (c) 2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.undoEngine.pages;

import fr.clementgre.pdf4teachers.document.Document;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UndoAction;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.lang.ref.WeakReference;

public class PagesCropUndoAction extends UndoAction {
    
    private final WeakReference<PageRenderer> page;
    
    private PDRectangle oldCropBox;
    private PDRectangle oldMediaBox;
    
    private boolean isInverted = false;
    
    public PagesCropUndoAction(UType undoType, WeakReference<PageRenderer> page, PDRectangle oldCropBox, PDRectangle oldMediaBox){
        super(undoType);
        this.page = page;
        this.oldCropBox = oldCropBox;
        this.oldMediaBox = oldMediaBox;
    }
    
    @Override
    public boolean undoAndInvert(){
        PageRenderer pageRenderer = this.page.get();
        
        if(pageRenderer != null && MainWindow.mainScreen.hasDocument(false)){
            Document document = MainWindow.mainScreen.document;
            PDPage page = document.pdfPagesRender.getDocument().getPage(pageRenderer.getPage());
            
            PDRectangle currentCropBox = page.getCropBox();
            PDRectangle currentMediaBox = page.getCropBox();
            
            if(isInverted){
                page.setCropBox(oldCropBox);
                page.setMediaBox(oldMediaBox);
            }else{
                page.setMediaBox(oldMediaBox);
                page.setCropBox(oldCropBox);
            }
            
            document.pdfPagesRender.editor.markAsEdited();
            document.getPage(0).updatePosition(PageRenderer.getPageMargin(), true);
            document.updateShowsStatus();
            document.updateBackgrounds();
            
            // Elements will be moved and resized by NO_COUNT_BEFORE actions
            
            // invert
            oldCropBox = currentCropBox;
            oldMediaBox = currentMediaBox;
            
            isInverted = !isInverted;
            
            return true;
        }
        return false;
    }
    
    @Override
    public String toString(){
        if(page.get() == null) return null;
        
        return TR.tr("actions.cropPage", page.get().getPage() + 1);
    }
    
}
