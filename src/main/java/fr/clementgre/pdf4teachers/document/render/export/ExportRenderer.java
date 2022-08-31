/*
 * Copyright (c) 2019-2022. Clément Grennerat
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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.util.Matrix;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;

public class ExportRenderer {
    
    public boolean exportFile(File pdfFile, File toFile, int imagesDPI, boolean textElements, boolean gradesElements, boolean drawElements, boolean skillElements) throws Exception{
        
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
        
        TextRenderer textRenderer = new TextRenderer(doc);
        TextElementRenderer textElementRenderer = new TextElementRenderer(doc, textRenderer);
        GradeElementRenderer gradeElementRenderer = new GradeElementRenderer(doc, textRenderer);
        ImageElementRenderer imageElementRenderer = new ImageElementRenderer(doc, imagesDPI);
        VectorElementRenderer vectorElementRenderer = new VectorElementRenderer(doc);
        SkillTableElementRenderer skillTableElementRenderer = new SkillTableElementRenderer(doc, textRenderer, imagesDPI);
        
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
            Matrix rotation = Matrix.getRotateInstance(0, 0, 0);
            if(page.getRotation() % 90 == 0){
                switch((page.getRotation() / 90) % 4){
                    case 1 -> rotation = Matrix.getRotateInstance(Math.toRadians(page.getRotation()), pageRealHeight, 0);
                    case 2 -> rotation = Matrix.getRotateInstance(Math.toRadians(page.getRotation()), pageRealWidth, pageRealHeight);
                    case 3 -> rotation = Matrix.getRotateInstance(Math.toRadians(page.getRotation()), 0, pageRealWidth);
                }
            }
            contentStream.transform(rotation);
            PageSpecs pageSpecs = new PageSpecs(pageWidth, pageHeight, pageRealWidth, pageRealHeight, startX, startY, rotation);
            
            for(Element element : elements){
                
                if(element.getPageNumber() != pageNumber) continue;
                
                if(element instanceof TextElement tElement){
                    if(textElements)
                        if(!textElementRenderer.renderElement(tElement, contentStream, page, pageSpecs)){
                            doc.close(); return false;
                        }
                }else if(element instanceof GradeElement gElement){
                    if(gradesElements)
                        if(!gradeElementRenderer.renderElement(gElement, contentStream, page, pageSpecs)){
                            doc.close(); return false;
                        }
                }else if(element instanceof ImageElement gElement){
                    if(drawElements)
                        imageElementRenderer.renderElement(gElement, contentStream, page, pageWidth, pageHeight, pageRealWidth, pageRealHeight, startX, startY);
                }else if(element instanceof VectorElement gElement){
                    if(drawElements)
                        vectorElementRenderer.renderElement(gElement, contentStream, page, pageWidth, pageHeight, pageRealWidth, pageRealHeight, startX, startY);
                }else if(element instanceof SkillTableElement gElement){
                    if(skillElements)
                        if(!skillTableElementRenderer.renderElement(gElement, contentStream, page, pageSpecs)){
                            doc.close(); return false;
                        }
                }
            }
            
            contentStream.close();
        }
        
        doc.save(toFile);
        doc.close();
        return true;
    }
    
    public static PDColor toPDColor(Color color){
        final float[] components = new float[]{(float) color.getRed(), (float) color.getGreen(), (float) color.getBlue()};
        return new PDColor(components, PDDeviceRGB.INSTANCE);
    }
    public static PDColor toPDColor(java.awt.Color color){
        final float[] components = new float[]{(float) color.getRed() / 255f, (float) color.getGreen() / 255f, (float) color.getBlue() / 255f};
        return new PDColor(components, PDDeviceRGB.INSTANCE);
    }
    
    public static PDRectangle transformRectangle(PDRectangle rectangle, Matrix matrix){
        Point2D.Float p0 = matrix.transformPoint(rectangle.getLowerLeftX(), rectangle.getLowerLeftY());
        Point2D.Float p1 = matrix.transformPoint(rectangle.getUpperRightX(), rectangle.getUpperRightY());
        PDRectangle newRectangle = new PDRectangle();
        newRectangle.setLowerLeftX(p0.x);
        newRectangle.setLowerLeftY(p0.y);
        newRectangle.setUpperRightX(p1.x);
        newRectangle.setUpperRightY(p1.y);
        return newRectangle;
    }
}
