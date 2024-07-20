/*
 * Copyright (c) 2019-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.*;
import javafx.scene.paint.Color;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
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
        
        PDDocument doc = Loader.loadPDF(new RandomAccessReadBufferedFile(pdfFile));
        
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
            
            float startX = page.getCropBox().getLowerLeftX();
            float startY = page.getCropBox().getLowerLeftY();
            float pageHeight = page.getCropBox().getHeight();
            float pageWidth = page.getCropBox().getWidth();
            float pageRealHeight = page.getCropBox().getHeight();
            float pageRealWidth = page.getCropBox().getWidth();
            // ROTATE PAGES ADAPT
            if(page.getRotation() == 90 || page.getRotation() == 270){
                startY = page.getCropBox().getLowerLeftX();
                startX = page.getCropBox().getLowerLeftY();
                
                pageHeight = page.getCropBox().getWidth();
                pageWidth = page.getCropBox().getHeight();
                pageRealHeight = page.getCropBox().getWidth();
                pageRealWidth = page.getCropBox().getHeight();
            }
            
            // ROTATE PAGES ADAPT
            Matrix csTransform = switch(page.getRotation()){
                case 90 -> Matrix.getRotateInstance(Math.PI / 2.0, startY + pageHeight, startX);
                case 180 -> Matrix.getRotateInstance(Math.PI, pageWidth + startX, pageHeight + startY);
                case 270 -> Matrix.getRotateInstance(Math.PI * 1.5, startY, startX + pageWidth);
                default -> Matrix.getTranslateInstance(startX, startY);
            };
            
            contentStream.transform(csTransform);
            PageSpecs pageSpecs = new PageSpecs(pageRealWidth, pageRealHeight, csTransform);
            
            for (Element element : elements) {
                if (element.getPageNumber() != pageNumber) continue;
                
                boolean renderSuccess = switch (element) {
                    case TextElement tElement when textElements ->
                            textElementRenderer.renderElement(tElement, contentStream, page, pageSpecs);
                    case GradeElement gElement when gradesElements ->
                            gradeElementRenderer.renderElement(gElement, contentStream, page, pageSpecs);
                    case ImageElement iElement when drawElements -> {
                        imageElementRenderer.renderElement(iElement, contentStream, page, pageRealWidth, pageRealHeight);
                        yield true;
                    }
                    case VectorElement vElement when drawElements -> {
                        vectorElementRenderer.renderElement(vElement, contentStream, page, pageRealWidth, pageRealHeight);
                        yield true;
                    }
                    case SkillTableElement sElement when skillElements ->
                            skillTableElementRenderer.renderElement(sElement, contentStream, page, pageSpecs);
                    default -> true;
                };
                
                if (!renderSuccess) {
                    doc.close();
                    return false;
                }
            }
            
            contentStream.close();
        }
        
        doc.save(toFile);
        doc.close();
        return true;
    }
    
    public static PDColor toPDColor(Color color){
        final float[] components = {(float) color.getRed(), (float) color.getGreen(), (float) color.getBlue()};
        return new PDColor(components, PDDeviceRGB.INSTANCE);
    }
    public static PDColor toPDColor(java.awt.Color color){
        final float[] components = {(float) color.getRed() / 255f, (float) color.getGreen() / 255f, (float) color.getBlue() / 255f};
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
