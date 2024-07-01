/*
 * Copyright (c) 2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.margin;

import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;

public record MarginEngine(double marginTop, double marginRight, double marginBottom, double marginLeft,
                           boolean isMarginKindAbsolute,
                           Integer... pages) {
    
    public void apply(){
        int count = pages.length;
        for(int page : pages){
            boolean res = applyOnPage(page, page == pages[0], page == pages[count - 1]);
            if(!res) break;
        }
    }
    
    private boolean applyOnPage(int page, boolean isFirstCall, boolean latest){
        return MainWindow.mainScreen.document.pdfPagesRender.editor.setPageMargin(page,
                (float) marginTop, (float) marginRight, (float) marginBottom, (float) marginLeft, latest, isMarginKindAbsolute,
                isFirstCall ? UType.PAGE : UType.PAGE_NO_COUNT_BEFORE);
    }
    
}
