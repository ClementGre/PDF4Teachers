/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.utils.svg.SVGUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

public class VectorElementRenderer{

    private final PDDocument doc;
    
    public VectorElementRenderer(PDDocument doc){
        this.doc = doc;
    }
    
    public void renderElement(VectorElement element, PDPageContentStream contentStream, PDPage page, float pageWidth, float pageHeight, float pageRealWidth, float pageRealHeight, float startX, float startY) throws Exception{
    
        float elementWidth = element.getRealWidth() / Element.GRID_WIDTH * pageWidth;
        float elementHeight = element.getRealHeight() / Element.GRID_HEIGHT * pageHeight;
        double graphicsWidth = elementWidth + element.getClipPaddingScaled(pageWidth)*2;
        double graphicsHeight = elementHeight + element.getClipPaddingScaled(pageWidth)*2;
        
        
        if(element.getRepeatMode() == GraphicElement.RepeatMode.CROP){
            if(graphicsWidth > graphicsHeight * element.getRatio()){ // Crop Y
                elementHeight = (float) (graphicsWidth / element.getRatio());
            }else{ // Crop X
                elementWidth = (float) (graphicsHeight * element.getRatio());
            }
        }
    
        String path;
        if(element.getRepeatMode() == GraphicElement.RepeatMode.MULTIPLY){
            path = element.getRepeatedPathScaled(elementWidth, elementHeight, (float) element.getSVGPaddingScaled(pageWidth), pageWidth);
        }else{
            path = element.getScaledPathScaled(elementWidth, elementHeight, (float) element.getSVGPaddingScaled(pageWidth), pageWidth);
        }
    
        PdfBoxGraphics2D g = new PdfBoxGraphics2D(doc, (int) Math.ceil(graphicsWidth), (int) Math.ceil(graphicsHeight));
        try{
            AffineTransform originalTransform = g.getTransform();
            g.transform(AffineTransform.getTranslateInstance(element.getClipPaddingScaled(pageWidth)+element.getSVGPaddingScaled(pageWidth),
                    element.getClipPaddingScaled(pageWidth)+element.getSVGPaddingScaled(pageWidth)));
    
            Shape shape = SVGUtils.convertToAwtShape(path);
    
            // Fill
            if(element.isDoFill()){
                g.setColor(StyleManager.fxColorToAWT(element.getFill()));
                g.fill(shape);
            }
            
            // Stroke
            if(element.getStrokeWidth() > 0){
                g.setStroke(new BasicStroke((float) element.getStrokeWidthScaled(pageWidth), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, Math.max(1, (float) element.getStrokeWidth())));
                g.setColor(StyleManager.fxColorToAWT(element.getStroke()));
                g.draw(shape);
            }
            
            // Restore original transform
            g.setTransform(originalTransform);
        }finally{
            g.dispose();
        }
    
        PDFormXObject xForm = g.getXFormObject();
    
        float bottomMargin = pageRealHeight - pageHeight - startY;
        AffineTransform transform = AffineTransform.getTranslateInstance(
                startX + (element.getRealX() / Element.GRID_WIDTH * pageWidth) - element.getClipPaddingScaled(pageWidth),
                bottomMargin + pageRealHeight - xForm.getBBox().getHeight() + element.getClipPaddingScaled(pageWidth) - (element.getRealY() / Element.GRID_HEIGHT * pageHeight));
        xForm.setMatrix(transform);
        
        contentStream.drawForm(xForm);
    }
}
