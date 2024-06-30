/*
 * Copyright (c) 2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.margin;

import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;

public record MarginEngine(double marginTop, double marginRight, double marginBottom, double marginLeft,
                           boolean isMarginOnSelectedPages, boolean isMarginKindAbsolute) {
    
    public void apply(){
        if(isMarginOnSelectedPages){
            int count = MainWindow.mainScreen.document.getSelectedPages().size();
            int i = 0;
            for(int page : MainWindow.mainScreen.document.getSelectedPages()){
                i++;
                boolean res = applyOnPage(page, i == 1, i == count);
                if(!res) break;
            }
        }else{
            int count = MainWindow.mainScreen.document.getPages().size();
            for(PageRenderer page : MainWindow.mainScreen.document.getPages()){
                boolean res = applyOnPage(page.getPage(), page.getPage() == 0, page.getPage() == count - 1);
                if(!res) break;
            }
        }
    }
    
    private boolean applyOnPage(int page, boolean isFirstCall, boolean latest){
        return MainWindow.mainScreen.document.pdfPagesRender.editor.setPageMargin(page,
                (float) marginTop, (float) marginRight, (float) marginBottom, (float) marginLeft, latest, isMarginKindAbsolute,
                isFirstCall ? UType.UNDO : UType.NO_COUNT);
    }
    
}
