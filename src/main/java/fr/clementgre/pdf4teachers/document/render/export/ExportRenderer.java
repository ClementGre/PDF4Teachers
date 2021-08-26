/*
 * Copyright (c) 2019-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.*;
import javafx.scene.paint.Color;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.util.Matrix;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;

public class ExportRenderer {
    
    public void exportFile(File pdfFile, File toFile, int imagesDPI, boolean textElements, boolean gradesElements, boolean drawElements) throws Exception{
        
        File editFile = Edition.getEditFile(pdfFile);
        
        PDDocument doc = PDDocument.load(pdfFile);
        
        if(doc.isEncrypted()){
            try{
                doc.setAllSecurityToBeRemoved(true);
            }catch(Exception e){
                doc.close();
                throw new Exception("The document is encrypted, and we can't decrypt it.", e);
            }
        }
        
        new PDStream(doc, new FileInputStream(pdfFile), COSName.FLATE_DECODE);
        doc.getDocumentInformation().setModificationDate(Calendar.getInstance());
        
        TextElementRenderer textElementRenderer = new TextElementRenderer(doc);
        GradeElementRenderer gradeElementRenderer = new GradeElementRenderer(doc);
        ImageElementRenderer imageElementRenderer = new ImageElementRenderer(doc, imagesDPI);
        VectorElementRenderer vectorElementRenderer = new VectorElementRenderer(doc);
        
        Element[] elements = Edition.simpleLoad(editFile);
        for(int pageNumber = 0; pageNumber < doc.getNumberOfPages(); pageNumber++){
            
            PDPage page = doc.getPage(pageNumber);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
            page.setBleedBox(page.getCropBox());
            
            float startX = page.getBleedBox().getLowerLeftX();
            float startY = page.getBleedBox().getLowerLeftY();
            float pageRealHeight = page.getBleedBox().getHeight();
            float pageRealWidth = page.getBleedBox().getWidth();
            float pageHeight = page.getCropBox().getHeight();
            float pageWidth = page.getCropBox().getWidth();
            // ROTATE PAGES ADAPT
            if(page.getRotation() == 90 || page.getRotation() == 270){
                startY = page.getBleedBox().getLowerLeftX();
                startX = page.getBleedBox().getLowerLeftY();
                
                pageRealHeight = page.getBleedBox().getWidth();
                pageRealWidth = page.getBleedBox().getHeight();
                
                pageHeight = page.getCropBox().getWidth();
                pageWidth = page.getCropBox().getHeight();
            }
            
            // ROTATE PAGES ADAPT
            switch(page.getRotation()){
                case 90 -> contentStream.transform(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), pageRealHeight, 0));
                case 180 -> contentStream.transform(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), pageRealWidth, pageRealHeight));
                case 270 -> contentStream.transform(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), 0, pageRealWidth));
            }
            
            for(Element element : elements){
                
                if(element.getPageNumber() != pageNumber) continue;
                
                if(element instanceof TextElement tElement){
                    if(textElements)
                        textElementRenderer.renderElement(tElement, contentStream, page, pageWidth, pageHeight, pageRealWidth, pageRealHeight, startX, startY);
                }else if(element instanceof GradeElement gElement){
                    if(gradesElements)
                        gradeElementRenderer.renderElement(gElement, contentStream, page, pageWidth, pageHeight, pageRealWidth, pageRealHeight, startX, startY);
                }else if(element instanceof ImageElement gElement){
                    if(drawElements)
                        imageElementRenderer.renderElement(gElement, contentStream, page, pageWidth, pageHeight, pageRealWidth, pageRealHeight, startX, startY);
                }else if(element instanceof VectorElement gElement){
                    if(drawElements)
                        vectorElementRenderer.renderElement(gElement, contentStream, page, pageWidth, pageHeight, pageRealWidth, pageRealHeight, startX, startY);
                }
            }
            
            contentStream.close();
        }
        
        doc.save(toFile);
        doc.close();
    }
    
    public static PDColor toPDColor(Color color){
        final float[] components = new float[]{(float) color.getRed(), (float) color.getGreen(), (float) color.getBlue()};
        return new PDColor(components, PDDeviceRGB.INSTANCE);
    }
}
