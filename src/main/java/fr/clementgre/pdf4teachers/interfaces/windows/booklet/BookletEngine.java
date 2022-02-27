/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.booklet;

import fr.clementgre.pdf4teachers.document.Document;
import fr.clementgre.pdf4teachers.document.render.display.PDFPagesEditor;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.application.Platform;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BookletEngine {
    
    private final boolean makeBooklet;
    private final boolean reorganisePages;
    private final boolean tookPages4by4;
    private final boolean invertOrder;
    public BookletEngine(boolean makeBooklet, boolean reorganisePages, boolean tookPages4by4, boolean invertOrder){
        this.makeBooklet = makeBooklet;
        this.reorganisePages = reorganisePages;
        this.tookPages4by4 = tookPages4by4;
        this.invertOrder = invertOrder;
    }
    
    public void convert(Document document){
        if(!makeBooklet) disassemble(document);
        else assemble(document);
    }
    
    public void assemble(Document document){
    
    }
    
    public void disassemble(Document document){
    
        /// SCINDER / délivreter
        /* ----- 4 BY 4 :
         * 4 | 1
         * 2 | 3
         * 8 | 5
         * 6 | 7
         * ----- All booklet :
         * 8 | 1
         * 2 | 7
         * 6 | 3
         * 4 | 5
         */
    
        PDFPagesEditor editor = document.pdfPagesRender.editor;
        editor.markAsEdited();
        List<PageRenderer> savedSelectedPages = editor.saveSelectedPages();
    
        // Invert pages order if needed
        if(invertOrder){
            int pagesCount = editor.getDocument().getNumberOfPages();
            for(int i = 0; i < pagesCount; i++){
                editor.movePageByIndex(document.getPage(0), pagesCount-1-i);
            }
        }
    
        // Create pages copy
        int oldNumPages = editor.getDocument().getNumberOfPages();
        for(int i = 0; i < oldNumPages; i++){
            PDPage oldPage = editor.getDocument().getPage(i);
            COSDictionary newPageDict = new COSDictionary(oldPage.getCOSObject());
            PDPage page = new PDPage(newPageDict);
        
            PDRectangle bounds = oldPage.getCropBox();
            bounds.setUpperRightX(bounds.getUpperRightX() - bounds.getWidth()/2);
            oldPage.setCropBox(bounds);
        
            bounds = page.getCropBox();
            bounds.setLowerLeftX(bounds.getLowerLeftX() + bounds.getWidth()/2);
            page.setCropBox(bounds);
        
            PageRenderer pageRenderer = new PageRenderer(document.totalPages);
            editor.getDocument().addPage(page);
        
            // add page
            document.getPages().add(pageRenderer);
            MainWindow.mainScreen.addPage(pageRenderer);
            document.totalPages++;
        
            pageRenderer.removeRender();
            Platform.runLater(pageRenderer::updateRender);
        }
        editor.restoreSelectedPages(savedSelectedPages);
    
        // Move pages
        LinkedHashMap<PageRenderer, Integer> pagesToMove = new LinkedHashMap<>();
        if(reorganisePages){
            if(tookPages4by4){
                int newIndex = 0;
                for(int oldIndex = 0; oldIndex < oldNumPages; oldIndex += 2){
                    pagesToMove.put(document.getPage(oldNumPages + oldIndex)    , newIndex);
                    pagesToMove.put(document.getPage(oldIndex + 1)              , newIndex+1);
                    pagesToMove.put(document.getPage(oldNumPages + oldIndex + 1), newIndex+2);
                    pagesToMove.put(document.getPage(oldIndex)                  , newIndex+3);
                    newIndex += 4;
                }
            }else{
                // Old left page = odd indice
                // Old front page = indice or indice -1 multiple of 4
                int maxIndex = editor.getDocument().getNumberOfPages()-1;
                for(int oldIndex = 0; oldIndex < oldNumPages; oldIndex++){ // Pages are iterating 2 by 2 because clones have been added at the end.
                    boolean front = oldIndex % 2 == 0; // Here pages are always left pages and front if indice is even
                    int leftIndex;
                    int rightIndex;
                    if(front){
                        leftIndex = maxIndex - oldIndex;
                        rightIndex = oldIndex;
                    }else{
                        leftIndex = oldIndex;
                        rightIndex = maxIndex - oldIndex;
                    }
                    pagesToMove.put(document.getPage(oldIndex), leftIndex);
                    pagesToMove.put(document.getPage(oldNumPages + oldIndex), rightIndex);
                }
            }
            for(Map.Entry<PageRenderer, Integer> toMove : pagesToMove.entrySet()){
                editor.movePageByIndex(toMove.getKey(), toMove.getValue());
            }
        }else{
            for(int i = 0; i < oldNumPages; i++){
                pagesToMove.put(document.getPage(i+oldNumPages), 2*i+1);
            }
        }
        // Really move pages
        for(Map.Entry<PageRenderer, Integer> toMove : pagesToMove.entrySet()){
            editor.movePageByIndex(toMove.getKey(), toMove.getValue());
        }
    
    
        editor.markAsEdited();
        // Update coordinates & render of the pages
        document.getPage(0).updatePosition(PageRenderer.getPageMargin(), true);
        document.updateShowsStatus();
        for(PageRenderer page : document.getPages()){
            page.removeRender();
            Platform.runLater(page::updateShowStatus);
        }
    
    }
}
