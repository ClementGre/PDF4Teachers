/*
 * Copyright (c) 2022-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.booklet;

import fr.clementgre.pdf4teachers.document.Document;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.render.display.PDFPagesEditor;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlreadyExistDialogManager;
import javafx.application.Platform;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record BookletEngine(boolean makeBooklet, boolean reorganisePages, boolean tookPages4by4, boolean invertOrder, String copyName) {
    
    public void convert(Document document) throws IOException{
        if(copyName != null){
            File target = new File(document.getFile().getParentFile(), copyName);
            
            if(target.exists()){
                var result = new AlreadyExistDialogManager(false).showAndWait(target);
                if(result == AlreadyExistDialogManager.ResultType.RENAME) target = AlreadyExistDialogManager.rename(target);
                if(result == AlreadyExistDialogManager.ResultType.STOP) return;
                // ResultType.SKIP is never returned for non-recursive operations, and ResultType.OVERWRITE leads to nothing.
            }
            FilesUtils.copyFileUsingStream(document.getFile().toPath(), target.toPath());
        }
        
        if(!makeBooklet) disassemble(document);
        else assemble(document);
    }
    
    private record MergedPage(PDPage newPage, PageRenderer left, PageRenderer right){}
    
    
    public void assemble(Document document) throws IOException{
        
        /// FUSIONNER / livreter
        /* ----- 4 BY 4 :
         * 4 | 1
         * 2 | 3
         * 8 | 5
         * 6 | 7
         * ----- All booklet :
         * 8 | 1        * 7 | 0
         * 2 | 7        * 1 | 6
         * 6 | 3        * 5 | 2
         * 4 | 5        * 3 | 4
         */
        
        PDFPagesEditor editor = document.pdfPagesRender.editor;
        document.clearSelectedPages();
        
        // Reordering pages
        if(reorganisePages){
            int mumPages = editor.getDocument().getNumberOfPages();
            LinkedHashMap<PageRenderer, Integer> pagesToMove = new LinkedHashMap<>();
            
            if(tookPages4by4){
                for(int index = 0; index < mumPages; index += 4){
                    pagesToMove.put(document.getPage(index    ), index + 1);
                    pagesToMove.put(document.getPage(index + 1), index + 2);
                    pagesToMove.put(document.getPage(index + 2), index + 3);
                    pagesToMove.put(document.getPage(index + 3), index    );
                }
            }else{
                for(int oldIndex = 0; oldIndex < mumPages/2; oldIndex++){
                    if(oldIndex % 2 == 0){
                        pagesToMove.put(document.getPage(mumPages-1-oldIndex), oldIndex*2  );
                        pagesToMove.put(document.getPage(oldIndex)         , oldIndex*2+1);
                    }else{
                        pagesToMove.put(document.getPage(oldIndex)         , oldIndex*2  );
                        pagesToMove.put(document.getPage(mumPages-1-oldIndex), oldIndex*2+1);
                    }
                    
                }
            }
            // Really move pages
            for(Map.Entry<PageRenderer, Integer> toMove : pagesToMove.entrySet()){
                editor.movePageByIndex(toMove.getKey(), toMove.getValue());
            }
        }
        
        // Merging pages 2 by 2
        // Source algorithm for merging pages from https://stackoverflow.com/questions/12093408/pdfbox-merge-2-portrait-pages-onto-a-single-side-by-side-landscape-page
        // Edited by Clément Grennerat
        ArrayList<MergedPage> newPages = new ArrayList<>();
        int oldNumPages = editor.getDocument().getNumberOfPages();
        for(int i = 0; i < oldNumPages; i+=2){
            // Create output PDF frame
            PDPage leftPage = editor.getDocument().getPage(i);
            PDPage rightPage = editor.getDocument().getPage(i + 1);
            
            PDRectangle leftFrame = leftPage.getCropBox();
            PDRectangle rightFrame = rightPage.getCropBox();
            
            if(leftPage.getRotation() == 90 || leftPage.getRotation() == 270)
                leftFrame = new PDRectangle(leftFrame.getLowerLeftY(), leftFrame.getLowerLeftX(), leftFrame.getHeight(), leftFrame.getWidth());
            if(rightPage.getRotation() == 90 || rightPage.getRotation() == 270)
                rightFrame = new PDRectangle(rightFrame.getLowerLeftY(), rightFrame.getLowerLeftX(), rightFrame.getHeight(), rightFrame.getWidth());
            
            PDRectangle outPdfFrame = new PDRectangle(leftFrame.getWidth()+rightFrame.getWidth(), Math.max(leftFrame.getHeight(), rightFrame.getHeight()));
            
            // Create output page with calculated frame and add it to the document
            COSDictionary dict = new COSDictionary();
            dict.setItem(COSName.TYPE, COSName.PAGE);
            dict.setItem(COSName.MEDIA_BOX, outPdfFrame);
            dict.setItem(COSName.CROP_BOX, outPdfFrame);
            dict.setItem(COSName.ART_BOX, outPdfFrame);
            PDPage newPage = new PDPage(dict);
            editor.getDocument().addPage(newPage);
            
            // Source PDF pages has to be imported as form XObjects to be able to insert them at a specific point in the output page
            LayerUtility layerUtility = new LayerUtility(editor.getDocument());
            
            PDFormXObject formPdf1 = generatePageForm(layerUtility, editor.getDocument(), leftPage, leftFrame, 0, outPdfFrame.getHeight());
            PDFormXObject formPdf2 = generatePageForm(layerUtility, editor.getDocument(), rightPage, rightFrame, leftFrame.getWidth(), outPdfFrame.getHeight());
            
            layerUtility.appendFormAsLayer(newPage, formPdf1, new AffineTransform(), "left-" + 2 * i + "-" + new Random().nextInt(99999));
            layerUtility.appendFormAsLayer(newPage, formPdf2, new AffineTransform(), "right-" + (2 * i + 1) + "-" + new Random().nextInt(99999));
            
            newPages.add(new MergedPage(newPage, document.getPage(i), document.getPage(i+1)));
        }
    
        // Adding new pages and removing old pages
        for(MergedPage pages : newPages){
            PageRenderer pageRenderer = new PageRenderer(document.numberOfPages);
            
            // add page
            document.getPages().add(pageRenderer);
            MainWindow.mainScreen.addPage(pageRenderer);
            document.numberOfPages++;
            
            // Move elements to new page
            while(!pages.left.getElements().isEmpty()){
                Element element = pages.left.getElements().get(0);
                element.switchPage(pageRenderer.getPage());
                element.setRealX(element.getRealX()/2);
                Platform.runLater(() -> {
                    element.size(.5);
                    Platform.runLater(() -> element.checkLocation(false));
                });
    
            }
            while(!pages.right.getElements().isEmpty()){
                Element element = pages.right.getElements().get(0);
                element.switchPage(pageRenderer.getPage());
                element.setRealX((int) (Element.GRID_WIDTH/2d + element.getRealX()/2d));
                Platform.runLater(() -> {
                    element.size(.5);
                    Platform.runLater(() -> element.checkLocation(false));
                });
            }
        }
        for(int i = 0; i < oldNumPages; i++){
            document.getPage(0).quitVectorEditMode();
            editor.deletePageUtil(document.getPage(0));
        }
    
        // Update coordinates & render of the pages
        editor.markAsEdited();
        document.getPage(0).updatePosition(PageRenderer.getPageMargin(), true);
        document.updateShowsStatus();
        for(PageRenderer page : document.getPages()){
            page.removeRender();
            Platform.runLater(page::updateShowStatus);
        }
    
        Edition.setUnsave("Assemble booklet");
        document.edition.save(false);
    }
    
    private PDFormXObject generatePageForm(LayerUtility layerUtility, PDDocument doc, PDPage page, PDRectangle rotatedCB, double trx, double availableHeight) throws IOException{
        PDFormXObject form = layerUtility.importPageAsForm(doc, page);
        int rotation = page.getRotation();
        
        AffineTransform at = new AffineTransform();
        at.translate(trx, (availableHeight - rotatedCB.getHeight()) / 2d);
        switch(rotation){
            case 90:
                at.translate(-rotatedCB.getLowerLeftX(), rotatedCB.getLowerLeftY());
                at.translate(0, rotatedCB.getHeight());
                at.rotate(-Math.PI / 2.0);
                break;
            case 180:
                at.translate(rotatedCB.getLowerLeftX(), rotatedCB.getLowerLeftY());
                at.translate(rotatedCB.getWidth(), rotatedCB.getHeight());
                at.rotate(-Math.PI);
                break;
            case 270:
                at.translate(rotatedCB.getLowerLeftX(), -rotatedCB.getLowerLeftY());
                at.translate(rotatedCB.getWidth(), 0);
                at.rotate(-Math.PI * 1.5);
                break;
            default:
                at.translate(-rotatedCB.getLowerLeftX(), -rotatedCB.getLowerLeftY());
        }
        form.setMatrix(at);
        
        return form;
    }
    
    private void cropPage(PDPage page, boolean leftPage){
        PDRectangle bounds = page.getCropBox();
        if(page.getRotation() == 90 || page.getRotation() == 270){
            leftPage = !leftPage;
        }
        if(page.getRotation() == 0 || page.getRotation() == 180){
            // Cut the page vertically because there is no rotation
            if(leftPage) bounds.setUpperRightX(bounds.getUpperRightX() - bounds.getWidth() / 2);
            else bounds.setLowerLeftX(bounds.getLowerLeftX() + bounds.getWidth() / 2);
        }else{
            // Cut the page horizontally because there is a rotation
            if(leftPage) bounds.setLowerLeftY(bounds.getLowerLeftY() + bounds.getHeight() / 2);
            else bounds.setUpperRightY(bounds.getUpperRightY() - bounds.getHeight() / 2);
        }
        page.setCropBox(bounds);
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
                editor.movePageByIndex(document.getPage(0), pagesCount - 1 - i);
            }
        }
    
        // Create pages copy for right pages
        int oldNumPages = editor.getDocument().getNumberOfPages();
        for(int i = 0; i < oldNumPages; i++){
            PDPage oldPage = editor.getDocument().getPage(i);
            COSDictionary newPageDict = new COSDictionary(oldPage.getCOSObject());
            PDPage page = new PDPage(newPageDict);
            
            cropPage(oldPage, true);
            cropPage(page, false);
    
            PageRenderer pageRenderer = new PageRenderer(document.numberOfPages);
            editor.getDocument().addPage(page);
    
            // add page
            document.getPages().add(pageRenderer);
            MainWindow.mainScreen.addPage(pageRenderer);
            document.numberOfPages++;
    
            pageRenderer.removeRender();
            Platform.runLater(pageRenderer::updateRender);
            
            // Move elements to new page
            for(Element element : new ArrayList<>(document.getPage(i).getElements())){
                if(element.getLayoutX() + element.getWidth()/2 > document.getPage(i).getWidth()/2){ // Move element to right page
                    element.switchPage(pageRenderer.getPage());
                    element.setRealX((int) ((element.getRealX() - Element.GRID_WIDTH/2d)*2));
                }else{
                    element.setRealX(element.getRealX()*2);
                }
                Platform.runLater(() -> {
                    element.size(2);
                    Platform.runLater(() -> element.checkLocation(false));
                });
            }
        }
        editor.restoreSelectedPages(savedSelectedPages);
    
        // Move pages
        LinkedHashMap<PageRenderer, Integer> pagesToMove = new LinkedHashMap<>();
        if(reorganisePages){
            if(tookPages4by4){
                int newIndex = 0;
                for(int oldIndex = 0; oldIndex < oldNumPages; oldIndex += 2){
                    pagesToMove.put(document.getPage(oldNumPages + oldIndex), newIndex);
                    pagesToMove.put(document.getPage(oldIndex + 1), newIndex + 1);
                    pagesToMove.put(document.getPage(oldNumPages + oldIndex + 1), newIndex + 2);
                    pagesToMove.put(document.getPage(oldIndex), newIndex + 3);
                    newIndex += 4;
                }
            }else{
                // Old left page = odd indice
                // Old front page = indice or indice -1 multiple of 4
                int maxIndex = editor.getDocument().getNumberOfPages() - 1;
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
            pagesToMove = IntStream.range(0, oldNumPages)
                    .boxed()
                    .collect(Collectors.toMap(i -> document.getPage(i + oldNumPages),
                                                                    i -> 2 * i + 1,
                                                                    (a, b) -> b,
                                                                    LinkedHashMap::new));
        }
        // Actually move pages
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
    
        Edition.setUnsave("Disassemble booklet");
        document.edition.save(false);
    
    }
}
