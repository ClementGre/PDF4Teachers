/*
 * Copyright (c) 2019-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public record TextElementRenderer(PDDocument doc, TextRenderer textRenderer) {
    
    public void renderElement(TextElement element, PDPageContentStream contentStream, PDPage page, PageSpecs pageSpecs) throws IOException{
        
        ////////// LATEX RENDER
        
        if(element.isLatex()){
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(element.renderAwtLatex(), "png", bos);
            byte[] data = bos.toByteArray();
            
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, data, element.getLaTeXText());
            
            float bottomMargin = pageSpecs.realHeight() - pageSpecs.height() - pageSpecs.startY();
            contentStream.drawImage(pdImage,
                    pageSpecs.startX() + element.getRealX() / Element.GRID_WIDTH * pageSpecs.width(),
                    (float) (bottomMargin + pageSpecs.realHeight() - ((pdImage.getHeight() / TextElement.imageFactor) / 596.0 * pageSpecs.width()) - element.getRealY() / Element.GRID_HEIGHT * pageSpecs.height()),
                    (float) ((pdImage.getWidth() / TextElement.imageFactor) / 596.0 * pageSpecs.width()),
                    (float) ((pdImage.getHeight() / TextElement.imageFactor) / 596.0 * pageSpecs.width()));
            
            return;
        }
        ////////// TEXT RENDER
        
        float bottomMargin = pageSpecs.realHeight() - pageSpecs.height() - pageSpecs.startY();
        TextRenderer.TextSpecs textSpecs = new TextRenderer.TextSpecs(element.getBoundsHeight(), element.getBoundsWidth(), bottomMargin,
                element.getBaseLineY(), element.getRealX(), element.getRealY(), element.getText());
        
        // COLOR
        contentStream.setNonStrokingColor(element.getAwtColor());
        // FONT
        Map.Entry<String, String> fontEntry = textRenderer.setContentStreamFont(contentStream, element.getFont(), pageSpecs.width());
        // DRAW TEXT
        textRenderer.drawText(contentStream, fontEntry, textSpecs, pageSpecs);
        
        
        ////////// URL RENDER
        if(element.isURL()){
            final PDAnnotationLink txtLink = new PDAnnotationLink();
            txtLink.setColor(ExportRenderer.toPDColor(element.getColor()));
            
            // Border bottom
            final PDBorderStyleDictionary linkBorder = new PDBorderStyleDictionary();
            
            linkBorder.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
            linkBorder.setWidth(1);
            txtLink.setBorderStyle(linkBorder);
            
            // Border color
            final PDRectangle position = new PDRectangle();
            position.setLowerLeftX(pageSpecs.startX() + textSpecs.realX() / Element.GRID_WIDTH * pageSpecs.width());
            position.setLowerLeftY(bottomMargin + pageSpecs.realHeight() - textSpecs.boundsHeight() - textSpecs.realY() / Element.GRID_HEIGHT * pageSpecs.height());
            position.setUpperRightX(position.getLowerLeftX() + textSpecs.boundsWidth());
            position.setUpperRightY(position.getLowerLeftY() + textSpecs.boundsHeight());
            
            txtLink.setRectangle(position);
            page.getAnnotations().add(txtLink);
            
            PDActionURI action = new PDActionURI();
            action.setURI(element.getText());
            txtLink.setAction(action);
        }
        
    }
    
}
